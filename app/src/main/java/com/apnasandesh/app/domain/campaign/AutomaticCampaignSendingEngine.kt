package com.apnasandesh.app.domain.campaign

import android.content.Context
import com.apnasandesh.app.core.common.DispatcherProvider
import com.apnasandesh.app.core.common.PrivacySafeLogger
import com.apnasandesh.app.core.database.CampaignDao
import com.apnasandesh.app.core.database.CampaignRecipientEntity
import com.apnasandesh.app.core.database.CustomerDao
import com.apnasandesh.app.data.campaign.CampaignRepository
import com.apnasandesh.app.domain.transport.TransportPolicyGate
import com.apnasandesh.app.domain.transport.TransportSubmissionOutcome
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

data class LiveCampaignEngineState(
    val campaignId: String = "",
    val campaignName: String = "",
    val status: String = "QUEUED", // QUEUED, RUNNING, PAUSED, COMPLETED, COMPLETED_WITH_ERRORS, STOPPED, FAILED
    val transportType: String = "DIRECT_SIM",
    val totalRecipients: Int = 0,
    val processedCount: Int = 0,
    val sentCount: Int = 0,
    val deliveredCount: Int = 0,
    val failedCount: Int = 0,
    val skippedCount: Int = 0,
    val remainingCount: Int = 0,
    val currentRecipientIndex: Int = 0,
    val currentRecipientName: String? = null,
    val currentRecipientPhone: String? = null,
    val currentRecipientMessage: String? = null,
    val sendingIntervalSeconds: Int = 3,
    val progressPercentage: Int = 0,
    val startTimeMillis: Long? = null,
    val completionTimeMillis: Long? = null,
    val statusMessage: String? = null,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false
)

@Singleton
class AutomaticCampaignSendingEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val campaignDao: CampaignDao,
    private val customerDao: CustomerDao,
    private val campaignRepository: CampaignRepository,
    private val policyGate: TransportPolicyGate,
    private val dispatchers: DispatcherProvider
) {
    private val engineScope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private var currentExecutionJob: Job? = null

    private val isPaused = AtomicBoolean(false)
    private val isStopped = AtomicBoolean(false)

    private val _liveEngineState = MutableStateFlow<LiveCampaignEngineState?>(null)
    val liveEngineState: StateFlow<LiveCampaignEngineState?> = _liveEngineState.asStateFlow()

    fun dismissStatusMessage() {
        _liveEngineState.value = _liveEngineState.value?.copy(statusMessage = null)
    }

    /**
     * Synchronizes live state with the current database state for a specific campaign.
     */
    suspend fun syncCampaignState(campaignId: String) = withContext(dispatchers.io) {
        val campaign = campaignDao.getCampaignById(campaignId) ?: return@withContext
        val recipients = campaignDao.getCampaignRecipientsSnapshot(campaignId)

        val total = campaign.totalRecipients.coerceAtLeast(recipients.size)
        val sent = recipients.count { it.state == "SENT" || it.state == "DELIVERED" }
        val delivered = recipients.count { it.state == "DELIVERED" }
        val failed = recipients.count { it.state == "FAILED" }
        val skipped = recipients.count { it.state == "CANCELLED" || it.state == "SUPPRESSED_OPT_OUT" }
        val processed = sent + failed + skipped
        val remaining = (total - processed).coerceAtLeast(0)

        val progress = if (total > 0) ((processed.toFloat() / total) * 100).toInt().coerceIn(0, 100) else 100
        val isFinished = campaign.status in setOf("COMPLETED", "COMPLETED_WITH_ERRORS", "STOPPED", "FAILED", "CANCELLED")

        _liveEngineState.value = LiveCampaignEngineState(
            campaignId = campaign.id,
            campaignName = campaign.name,
            status = campaign.status,
            transportType = campaign.transportType,
            totalRecipients = total,
            processedCount = processed,
            sentCount = sent,
            deliveredCount = delivered,
            failedCount = failed,
            skippedCount = skipped,
            remainingCount = remaining,
            currentRecipientIndex = processed.coerceAtMost(total),
            sendingIntervalSeconds = campaign.sendingIntervalSeconds,
            progressPercentage = progress,
            startTimeMillis = campaign.confirmedAtMillis,
            completionTimeMillis = campaign.completedAtMillis,
            isRunning = campaign.status in setOf("RUNNING", "IN_PROGRESS"),
            isPaused = campaign.status == "PAUSED",
            isFinished = isFinished
        )
    }

    /**
     * Starts or resumes automatic sequential campaign execution.
     */
    fun startCampaign(campaignId: String, intervalSeconds: Int? = null) {
        engineScope.launch {
            val campaign = campaignDao.getCampaignById(campaignId) ?: return@launch
            val resolvedInterval = intervalSeconds ?: campaign.sendingIntervalSeconds

            // Pre-flight channel policy and permission verification
            var effectiveTransport = campaign.transportType
            if (effectiveTransport == "DIRECT_SIM") {
                if (!policyGate.isDirectSmsPermissionDeclared()) {
                    campaignDao.updateCampaignTransportType(campaignId, "COMPOSER_HANDOFF")
                    effectiveTransport = "COMPOSER_HANDOFF"
                } else if (!policyGate.hasDirectSmsRuntimePermissions()) {
                    _liveEngineState.value = _liveEngineState.value?.copy(
                        statusMessage = "SEND_SMS permission has not been granted. Please grant SMS permissions to proceed.",
                        isRunning = false,
                        isPaused = true
                    )
                    campaignDao.updateCampaignStatus(campaignId, "PAUSED")
                    return@launch
                }
            }

            // Transition campaign state to RUNNING
            val startTime = campaign.confirmedAtMillis ?: System.currentTimeMillis()
            campaignDao.updateCampaignStatus(
                campaignId = campaignId,
                status = "RUNNING",
                confirmedAt = startTime
            )

            isPaused.set(false)
            isStopped.set(false)

            syncCampaignState(campaignId)

            currentExecutionJob?.cancel()
            currentExecutionJob = engineScope.launch {
                try {
                    while (isActive && !isStopped.get()) {
                        // Pause handling
                        while (isPaused.get() && !isStopped.get()) {
                            delay(500)
                        }
                        if (isStopped.get()) break

                        // Fetch next pending recipient
                        val pending = campaignDao.getPendingRecipients(campaignId)
                        if (pending.isEmpty()) {
                            // Queue exhausted
                            break
                        }

                        val recipient = pending.first()

                        // Real-time pre-send suppression check
                        if (customerDao.isPhoneSuppressed(recipient.normalizedPhoneSnapshot)) {
                            campaignDao.recordSendAttemptAndTransitionState(
                                recipientRowId = recipient.id,
                                campaignId = campaignId,
                                newState = "CANCELLED",
                                transportMode = campaign.transportType,
                                subscriptionId = campaign.selectedSubscriptionId,
                                errorCode = "OPT_OUT_SUPPRESSION",
                                errorMessage = "Customer opted out; message suppressed"
                            )
                            syncCampaignState(campaignId)
                            continue
                        }

                        // Update current recipient in live state
                        _liveEngineState.value = _liveEngineState.value?.copy(
                            currentRecipientName = recipient.recipientNameSnapshot,
                            currentRecipientPhone = recipient.normalizedPhoneSnapshot,
                            currentRecipientMessage = recipient.resolvedMessageBody,
                            status = "RUNNING",
                            isRunning = true,
                            isPaused = false
                        )

                        // Dispatch single recipient
                        val outcome = campaignRepository.dispatchSingleRecipient(campaignId, recipient.id)

                        // Auto-retry policy for temporary SIM failures (1 retry with 2s exponential backoff)
                        if (outcome is TransportSubmissionOutcome.FailedImmediately && campaign.transportType == "DIRECT_SIM") {
                            delay(2000)
                            if (!isStopped.get() && !isPaused.get()) {
                                campaignRepository.dispatchSingleRecipient(campaignId, recipient.id)
                            }
                        }

                        syncCampaignState(campaignId)

                        // For external handoffs (Composer), pause automatic advance and yield to user
                        if (campaign.transportType == "COMPOSER_HANDOFF") {
                            _liveEngineState.value = _liveEngineState.value?.copy(
                                statusMessage = "Message pre-filled in SMS App. Return to ApnaSandesh and tap Mark Sent to advance.",
                                isPaused = true,
                                isRunning = false
                            )
                            campaignDao.updateCampaignStatus(campaignId, "PAUSED")
                            isPaused.set(true)
                            break
                        }

                        // Sequential pacing interval between recipients
                        val delayMillis = (resolvedInterval.coerceAtLeast(1)) * 1000L
                        delay(delayMillis)
                    }

                    // Loop ended: wait briefly for in-flight radio broadcasts to settle
                    if (!isStopped.get() && !isPaused.get()) {
                        var waitRounds = 0
                        while (isActive && waitRounds < 10) {
                            val hasInFlight = campaignDao.getCampaignRecipientsSnapshot(campaignId).any { it.state == "SUBMITTED" }
                            if (!hasInFlight) break
                            delay(500)
                            waitRounds++
                        }
                        finalizeCampaign(campaignId)
                    }
                } catch (e: CancellationException) {
                    PrivacySafeLogger.info("AutomaticCampaignSendingEngine job cancelled for $campaignId")
                } catch (e: Exception) {
                    PrivacySafeLogger.error("Error during automatic campaign sending", e)
                    _liveEngineState.value = _liveEngineState.value?.copy(
                        statusMessage = "Engine error: ${e.message}",
                        isRunning = false
                    )
                }
            }
        }
    }

    private suspend fun finalizeCampaign(campaignId: String) = withContext(dispatchers.io) {
        val recipients = campaignDao.getCampaignRecipientsSnapshot(campaignId)
        val sent = recipients.count { it.state == "SENT" || it.state == "DELIVERED" }
        val failed = recipients.count { it.state == "FAILED" }
        val now = System.currentTimeMillis()

        val finalStatus = when {
            sent > 0 && failed > 0 -> "COMPLETED_WITH_ERRORS"
            sent > 0 && failed == 0 -> "COMPLETED"
            sent == 0 && failed > 0 -> "FAILED"
            else -> "COMPLETED"
        }

        campaignDao.updateCampaignStatus(
            campaignId = campaignId,
            status = finalStatus,
            completedAt = now
        )
        syncCampaignState(campaignId)
    }

    fun pauseCampaign(campaignId: String) {
        isPaused.set(true)
        engineScope.launch {
            campaignDao.updateCampaignStatus(campaignId, "PAUSED")
            syncCampaignState(campaignId)
        }
    }

    fun resumeCampaign(campaignId: String) {
        isPaused.set(false)
        if (currentExecutionJob?.isActive == true) {
            engineScope.launch {
                campaignDao.updateCampaignStatus(campaignId, "RUNNING")
                syncCampaignState(campaignId)
            }
        } else {
            startCampaign(campaignId)
        }
    }

    fun stopCampaign(campaignId: String) {
        isStopped.set(true)
        currentExecutionJob?.cancel()
        engineScope.launch {
            campaignDao.stopPendingRecipients(campaignId)
            campaignDao.updateCampaignStatus(
                campaignId = campaignId,
                status = "STOPPED",
                completedAt = System.currentTimeMillis()
            )
            syncCampaignState(campaignId)
        }
    }

    fun retryFailedRecipients(campaignId: String) {
        engineScope.launch {
            campaignDao.resetFailedRecipientsToQueued(campaignId)
            startCampaign(campaignId)
        }
    }
}
