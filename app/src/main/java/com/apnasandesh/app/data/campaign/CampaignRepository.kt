package com.apnasandesh.app.data.campaign

import android.content.Context
import android.net.Uri
import com.apnasandesh.app.core.common.DispatcherProvider
import com.apnasandesh.app.core.database.BusinessProfileEntity
import com.apnasandesh.app.core.database.CampaignDao
import com.apnasandesh.app.core.database.CampaignEntity
import com.apnasandesh.app.core.database.CampaignRecipientEntity
import com.apnasandesh.app.core.database.CustomerDao
import com.apnasandesh.app.core.database.MessageTemplateEntity
import com.apnasandesh.app.core.database.ProfileAndSettingsDao
import com.apnasandesh.app.core.database.SendAttemptEntity
import com.apnasandesh.app.core.database.TemplateDao
import com.apnasandesh.app.domain.campaign.CampaignStateMachine
import com.apnasandesh.app.domain.transport.ComposerSmsTransport
import com.apnasandesh.app.domain.transport.DirectSimSmsTransport
import com.apnasandesh.app.domain.transport.TransportSubmissionOutcome
import com.apnasandesh.app.domain.validation.SmsSegmentCalculator
import com.apnasandesh.app.domain.validation.TemplateRenderer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CampaignRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val campaignDao: CampaignDao,
    private val customerDao: CustomerDao,
    private val templateDao: TemplateDao,
    private val profileAndSettingsDao: ProfileAndSettingsDao,
    private val templateRenderer: TemplateRenderer,
    private val segmentCalculator: SmsSegmentCalculator,
    private val stateMachine: CampaignStateMachine,
    private val composerTransport: ComposerSmsTransport,
    private val directSimTransport: DirectSimSmsTransport,
    private val dispatchers: DispatcherProvider
) {
    fun observeCampaigns(): Flow<List<CampaignEntity>> = campaignDao.observeCampaigns()
    fun observeCampaignRecipients(campaignId: String): Flow<List<CampaignRecipientEntity>> =
        campaignDao.observeCampaignRecipients(campaignId)
    fun observeTemplates(): Flow<List<MessageTemplateEntity>> = templateDao.observeTemplates()
    fun observeSendAttempts(campaignId: String): Flow<List<SendAttemptEntity>> =
        campaignDao.observeSendAttempts(campaignId)

    suspend fun getCampaignById(campaignId: String): CampaignEntity? = withContext(dispatchers.io) {
        campaignDao.getCampaignById(campaignId)
    }

    suspend fun createOrUpdateValidatedCampaignDraft(
        existingCampaignId: String?,
        name: String,
        templateId: String?,
        rawMessageTemplate: String,
        appendSignature: Boolean,
        isPromotional: Boolean,
        transportType: String,
        selectedSubscriptionId: Int?,
        selectedCustomerIds: Set<String>?,
        sendingIntervalSeconds: Int = 3,
        scheduledAtMillis: Long? = null
    ): Result<CampaignEntity> = withContext(dispatchers.io) {
        if (name.isBlank() || rawMessageTemplate.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Campaign name and message template cannot be empty."))
        }
        val unknownTokens = templateRenderer.validateTemplateTokens(rawMessageTemplate)
        if (unknownTokens.isNotEmpty()) {
            return@withContext Result.failure(
                IllegalArgumentException("Unsupported placeholder token(s): ${unknownTokens.joinToString()}")
            )
        }

        val profile = profileAndSettingsDao.getBusinessProfile() ?: BusinessProfileEntity(
            id = "default_profile",
            businessName = "",
            ownerDisplayName = "",
            businessCategory = "Retail & Services",
            communicationPhone = "",
            businessAddress = "",
            defaultMessageSignature = "",
            updatedAtEpochMillis = System.currentTimeMillis()
        )

        // Strictly query only eligible, non-suppressed, non-opted-out customers
        val allEligible = customerDao.getEligibleCustomers(requirePromotionalOptIn = isPromotional)
        val filteredCustomers = if (selectedCustomerIds == null) {
            allEligible
        } else {
            allEligible.filter { it.id in selectedCustomerIds }
        }.distinctBy { it.normalizedPhoneE164 }

        if (filteredCustomers.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("No eligible, opted-in customers match your selection after applying Opt-Out Suppression.")
            )
        }

        val campaignId = existingCampaignId ?: UUID.randomUUID().toString()
        val campaign = CampaignEntity(
            id = campaignId,
            name = name.trim(),
            templateId = templateId,
            rawMessageTemplate = rawMessageTemplate,
            transportType = transportType,
            selectedSubscriptionId = selectedSubscriptionId,
            status = "AWAITING_CONFIRMATION",
            totalRecipients = filteredCustomers.size,
            sendingIntervalSeconds = sendingIntervalSeconds,
            scheduledAtMillis = scheduledAtMillis,
            createdAtMillis = System.currentTimeMillis(),
            updatedAtMillis = System.currentTimeMillis()
        )

        val recipients = filteredCustomers.map { customer ->
            val rendered = templateRenderer.render(
                rawTemplate = rawMessageTemplate,
                customer = customer,
                profile = profile,
                appendSignature = appendSignature,
                appendOptOutFooter = isPromotional,
                optOutFooterText = "Reply STOP to opt out."
            )
            val calc = segmentCalculator.calculate(rendered)
            CampaignRecipientEntity(
                id = UUID.randomUUID().toString(),
                campaignId = campaignId,
                customerId = customer.id,
                recipientNameSnapshot = customer.fullName,
                normalizedPhoneSnapshot = customer.normalizedPhoneE164,
                resolvedMessageBody = rendered,
                characterCount = calc.charCount,
                estimatedSegments = calc.segmentCount,
                encodingType = calc.encoding.name,
                state = "AWAITING_CONFIRMATION",
                updatedAtMillis = System.currentTimeMillis()
            )
        }

        campaignDao.replaceDraftCampaignWithRecipients(campaign, recipients)
        Result.success(campaign)
    }

    suspend fun updateSingleRecipientMessage(recipientId: String, editedMessage: String): Result<Unit> = withContext(dispatchers.io) {
        val recipient = campaignDao.getCampaignRecipientById(recipientId)
            ?: return@withContext Result.failure(IllegalArgumentException("Recipient not found"))
        val calc = segmentCalculator.calculate(editedMessage)
        campaignDao.updateRecipientResolvedMessage(
            recipientId = recipientId,
            newMessage = editedMessage,
            charCount = calc.charCount,
            segments = calc.segmentCount,
            encoding = calc.encoding.name
        )
        Result.success(Unit)
    }

    suspend fun duplicateCampaignAsDraft(sourceCampaignId: String): Result<String> = withContext(dispatchers.io) {
        val source = campaignDao.getCampaignById(sourceCampaignId)
            ?: return@withContext Result.failure(IllegalArgumentException("Source campaign not found"))
        val copy = createOrUpdateValidatedCampaignDraft(
            existingCampaignId = null,
            name = "${source.name} (Copy)",
            templateId = source.templateId,
            rawMessageTemplate = source.rawMessageTemplate,
            appendSignature = true,
            isPromotional = false,
            transportType = source.transportType,
            selectedSubscriptionId = source.selectedSubscriptionId,
            selectedCustomerIds = null
        )
        copy.map { it.id }
    }

    suspend fun confirmCampaignSession(campaignId: String) = withContext(dispatchers.io) {
        campaignDao.updateCampaignStatus(
            campaignId = campaignId,
            status = "QUEUED",
            confirmedAt = System.currentTimeMillis()
        )
    }

    suspend fun cancelCampaign(campaignId: String) = withContext(dispatchers.io) {
        val recipients = campaignDao.getCampaignRecipientsSnapshot(campaignId)
        val now = System.currentTimeMillis()
        recipients.forEach { recipient ->
            if (recipient.state in setOf("DRAFT", "VALIDATED", "AWAITING_CONFIRMATION", "QUEUED")) {
                campaignDao.updateRecipientState(
                    recipientRowId = recipient.id,
                    newState = "CANCELLED",
                    errorCode = "USER_CANCELLED",
                    errorMessage = "Cancelled by user",
                    timestamp = now
                )
            }
        }
        campaignDao.updateCampaignStatus(
            campaignId = campaignId,
            status = "CANCELLED",
            completedAt = now
        )
    }

    suspend fun markCampaignScheduled(campaignId: String, scheduledAtMillis: Long) = withContext(dispatchers.io) {
        val campaign = campaignDao.getCampaignById(campaignId)
        if (campaign != null) {
            campaignDao.upsertCampaign(
                campaign.copy(
                    status = "SCHEDULED",
                    scheduledAtMillis = scheduledAtMillis,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun updateCampaignTransportType(campaignId: String, transportType: String) = withContext(dispatchers.io) {
        campaignDao.updateCampaignTransportType(campaignId, transportType)
    }

    suspend fun markRecipientComposerDone(recipientRowId: String, campaignId: String) = withContext(dispatchers.io) {
        campaignDao.recordSendAttemptAndTransitionState(
            recipientRowId = recipientRowId,
            campaignId = campaignId,
            newState = "SENT",
            transportMode = "COMPOSER_HANDOFF",
            subscriptionId = null,
            errorCode = null,
            errorMessage = null
        )
        checkAndCompleteCampaignIfDone(campaignId)
    }

    suspend fun markRecipientComposerSkipped(recipientRowId: String, campaignId: String) = withContext(dispatchers.io) {
        campaignDao.recordSendAttemptAndTransitionState(
            recipientRowId = recipientRowId,
            campaignId = campaignId,
            newState = "CANCELLED",
            transportMode = "COMPOSER_HANDOFF",
            subscriptionId = null,
            errorCode = "SKIPPED",
            errorMessage = "Skipped by user in messaging app"
        )
        checkAndCompleteCampaignIfDone(campaignId)
    }

    private suspend fun checkAndCompleteCampaignIfDone(campaignId: String) {
        val remaining = campaignDao.getCampaignRecipientsSnapshot(campaignId).filter {
            it.state in setOf("DRAFT", "VALIDATED", "AWAITING_CONFIRMATION", "QUEUED", "SUBMITTED", "HANDED_OFF_TO_MESSAGING_APP")
        }
        if (remaining.isEmpty()) {
            campaignDao.updateCampaignStatus(
                campaignId = campaignId,
                status = "COMPLETED",
                completedAt = System.currentTimeMillis()
            )
        }
    }

    /**
     * Dispatches a single recipient row after checking suppression one final time right before send.
     */
    suspend fun dispatchSingleRecipient(
        campaignId: String,
        recipientRowId: String
    ): TransportSubmissionOutcome = withContext(dispatchers.io) {
        val campaign = campaignDao.getCampaignById(campaignId)
            ?: return@withContext TransportSubmissionOutcome.FailedImmediately("Campaign not found")
        val recipient = campaignDao.getCampaignRecipientById(recipientRowId)
            ?: return@withContext TransportSubmissionOutcome.FailedImmediately("Recipient not found")

        // Final real-time suppression check before any transport action
        if (customerDao.isPhoneSuppressed(recipient.normalizedPhoneSnapshot)) {
            campaignDao.recordSendAttemptAndTransitionState(
                recipientRowId = recipient.id,
                campaignId = campaignId,
                newState = "CANCELLED",
                transportMode = campaign.transportType,
                subscriptionId = campaign.selectedSubscriptionId,
                errorCode = "OPT_OUT_SUPPRESSION",
                errorMessage = "Suppressed immediately prior to send due to opt-out"
            )
            return@withContext TransportSubmissionOutcome.FailedImmediately("Recipient opted out; suppressed.")
        }

        when (campaign.transportType) {
            "COMPOSER_HANDOFF" -> {
                val outcome = composerTransport.send(
                    recipientRowId = recipient.id,
                    destinationPhoneE164 = recipient.normalizedPhoneSnapshot,
                    messageBody = recipient.resolvedMessageBody
                )
                when (outcome) {
                    is TransportSubmissionOutcome.HandedOffToComposer -> {
                        campaignDao.recordSendAttemptAndTransitionState(
                            recipientRowId = recipient.id,
                            campaignId = campaignId,
                            newState = "HANDED_OFF_TO_MESSAGING_APP",
                            transportMode = "COMPOSER_HANDOFF",
                            subscriptionId = null,
                            errorCode = null,
                            errorMessage = null
                        )
                        campaignDao.updateCampaignStatus(campaignId, "IN_PROGRESS")
                    }
                    is TransportSubmissionOutcome.FailedImmediately -> {
                        campaignDao.recordSendAttemptAndTransitionState(
                            recipientRowId = recipient.id,
                            campaignId = campaignId,
                            newState = "FAILED",
                            transportMode = "COMPOSER_HANDOFF",
                            subscriptionId = null,
                            errorCode = "COMPOSER_FAILED",
                            errorMessage = outcome.diagnostic
                        )
                    }
                    else -> Unit
                }
                outcome
            }
            "DIRECT_SIM" -> {
                campaignDao.recordSendAttemptAndTransitionState(
                    recipientRowId = recipient.id,
                    campaignId = campaignId,
                    newState = "SUBMITTED",
                    transportMode = "DIRECT_SIM",
                    subscriptionId = campaign.selectedSubscriptionId,
                    errorCode = null,
                    errorMessage = null
                )
                campaignDao.updateCampaignStatus(campaignId, "IN_PROGRESS")
                val outcome = directSimTransport.send(
                    recipientRowId = recipient.id,
                    destinationPhoneE164 = recipient.normalizedPhoneSnapshot,
                    messageBody = recipient.resolvedMessageBody,
                    explicitSubscriptionId = campaign.selectedSubscriptionId
                )
                if (outcome is TransportSubmissionOutcome.BlockedByPolicyOrPermission) {
                    campaignDao.recordSendAttemptAndTransitionState(
                        recipientRowId = recipient.id,
                        campaignId = campaignId,
                        newState = "FAILED",
                        transportMode = "DIRECT_SIM",
                        subscriptionId = campaign.selectedSubscriptionId,
                        errorCode = "BLOCKED_BY_POLICY_OR_PERMISSION",
                        errorMessage = outcome.reason
                    )
                } else if (outcome is TransportSubmissionOutcome.FailedImmediately) {
                    campaignDao.recordSendAttemptAndTransitionState(
                        recipientRowId = recipient.id,
                        campaignId = campaignId,
                        newState = "FAILED",
                        transportMode = "DIRECT_SIM",
                        subscriptionId = campaign.selectedSubscriptionId,
                        errorCode = "DIRECT_SIM_FAILED",
                        errorMessage = outcome.diagnostic
                    )
                }
                outcome
            }
            else -> TransportSubmissionOutcome.FailedImmediately("Unknown transport type: ${campaign.transportType}")
        }
    }

    suspend fun pauseCampaign(campaignId: String) = withContext(dispatchers.io) {
        campaignDao.updateCampaignStatus(campaignId, "PAUSED")
    }

    suspend fun resumeCampaign(campaignId: String) = withContext(dispatchers.io) {
        campaignDao.updateCampaignStatus(campaignId, "RUNNING")
    }

    suspend fun stopCampaign(campaignId: String) = withContext(dispatchers.io) {
        campaignDao.stopPendingRecipients(campaignId)
        campaignDao.updateCampaignStatus(
            campaignId = campaignId,
            status = "STOPPED",
            completedAt = System.currentTimeMillis()
        )
    }

    suspend fun retryFailedRecipients(campaignId: String): Int = withContext(dispatchers.io) {
        val resetCount = campaignDao.resetFailedRecipientsToQueued(campaignId)
        if (resetCount > 0) {
            campaignDao.updateCampaignStatus(campaignId, "QUEUED")
        }
        resetCount
    }

    suspend fun exportCampaignReportCsv(campaignId: String, destinationUri: Uri): Result<Int> =
        withContext(dispatchers.io) {
            runCatching {
                val campaign = campaignDao.getCampaignById(campaignId) ?: error("Campaign not found")
                val recipients = campaignDao.getCampaignRecipientsSnapshot(campaignId)
                context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                    OutputStreamWriter(out, Charsets.UTF_8).use { writer ->
                        writer.write("Campaign_Name,Recipient_Name,Phone_E164,State,Segments,Encoding,Diagnostic\n")
                        recipients.forEach { r ->
                            writer.write(
                                "\"${campaign.name.replace("\"", "\"\"")}\"," +
                                    "\"${r.recipientNameSnapshot.replace("\"", "\"\"")}\"," +
                                    "\"${r.normalizedPhoneSnapshot}\"," +
                                    "\"${r.state}\"," +
                                    "${r.estimatedSegments}," +
                                    "\"${r.encodingType}\"," +
                                    "\"${(r.lastErrorMessage ?: "").replace("\"", "\"\"")}\"\n"
                            )
                        }
                    }
                }
                recipients.size
            }
        }

    suspend fun duplicateCampaign(campaignId: String): Result<CampaignEntity> = withContext(dispatchers.io) {
        runCatching {
            val original = campaignDao.getCampaignById(campaignId) ?: error("Campaign not found")
            val originalRecipients = campaignDao.getCampaignRecipientsSnapshot(campaignId)
            val newCampaignId = UUID.randomUUID().toString()
            val duplicatedCampaign = original.copy(
                id = newCampaignId,
                name = "${original.name} (Copy)",
                status = "DRAFT",
                createdAtMillis = System.currentTimeMillis(),
                updatedAtMillis = System.currentTimeMillis(),
                confirmedAtMillis = null,
                completedAtMillis = null
            )
            val duplicatedRecipients = originalRecipients.map { r ->
                r.copy(
                    id = UUID.randomUUID().toString(),
                    campaignId = newCampaignId,
                    state = "DRAFT",
                    lastErrorCode = null,
                    lastErrorMessage = null,
                    updatedAtMillis = System.currentTimeMillis()
                )
            }
            campaignDao.replaceDraftCampaignWithRecipients(duplicatedCampaign, duplicatedRecipients)
            duplicatedCampaign
        }
    }

    suspend fun deleteCampaign(campaignId: String): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            campaignDao.clearDraftRecipients(campaignId)
            campaignDao.deleteCampaignById(campaignId)
        }
    }
}
