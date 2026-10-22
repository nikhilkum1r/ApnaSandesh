package com.localreach.bizsms.data.campaign

import android.content.Context
import android.net.Uri
import com.localreach.bizsms.core.common.DispatcherProvider
import com.localreach.bizsms.core.database.BusinessProfileEntity
import com.localreach.bizsms.core.database.CampaignDao
import com.localreach.bizsms.core.database.CampaignEntity
import com.localreach.bizsms.core.database.CampaignRecipientEntity
import com.localreach.bizsms.core.database.CustomerDao
import com.localreach.bizsms.core.database.MessageTemplateEntity
import com.localreach.bizsms.core.database.ProfileAndSettingsDao
import com.localreach.bizsms.core.database.SendAttemptEntity
import com.localreach.bizsms.core.database.TemplateDao
import com.localreach.bizsms.domain.campaign.CampaignStateMachine
import com.localreach.bizsms.domain.transport.ComposerSmsTransport
import com.localreach.bizsms.domain.transport.DirectSimSmsTransport
import com.localreach.bizsms.domain.transport.TransportSubmissionOutcome
import com.localreach.bizsms.domain.validation.SmsSegmentCalculator
import com.localreach.bizsms.domain.validation.TemplateRenderer
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
        selectedCustomerIds: Set<String>?
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
            businessName = "My Local Shop",
            ownerDisplayName = "Owner",
            businessCategory = "Retail",
            communicationPhone = "",
            businessAddress = "",
            defaultMessageSignature = "- My Local Shop",
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

    suspend fun markRecipientComposerDone(recipientRowId: String, campaignId: String) = withContext(dispatchers.io) {
        campaignDao.recordSendAttemptAndTransitionState(
            recipientRowId = recipientRowId,
            campaignId = campaignId,
            newState = "SENT",
            transportMode = "COMPOSER_HANDOFF",
            subscriptionId = null,
            errorCode = null,
            errorMessage = "Marked sent after messaging app handoff"
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
                            errorMessage = "Opened in messaging app; awaiting user confirmation"
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
                    errorMessage = "Submitting to SIM radio..."
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
}
