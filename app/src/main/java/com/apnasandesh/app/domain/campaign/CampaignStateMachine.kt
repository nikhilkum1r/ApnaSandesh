package com.apnasandesh.app.domain.campaign

import com.apnasandesh.app.core.model.CampaignStatus
import com.apnasandesh.app.core.model.RecipientDeliveryState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CampaignStateMachine @Inject constructor() {

    fun canTransitionRecipient(
        current: RecipientDeliveryState,
        target: RecipientDeliveryState
    ): Boolean {
        if (current == RecipientDeliveryState.SUPPRESSED_OPT_OUT) return false
        if (target == RecipientDeliveryState.SUPPRESSED_OPT_OUT) return true

        return when (current) {
            RecipientDeliveryState.DRAFT -> target in setOf(
                RecipientDeliveryState.VALIDATED,
                RecipientDeliveryState.CANCELLED
            )
            RecipientDeliveryState.VALIDATED -> target in setOf(
                RecipientDeliveryState.AWAITING_CONFIRMATION,
                RecipientDeliveryState.DRAFT,
                RecipientDeliveryState.CANCELLED
            )
            RecipientDeliveryState.AWAITING_CONFIRMATION -> target in setOf(
                RecipientDeliveryState.HANDED_OFF_TO_MESSAGING_APP,
                RecipientDeliveryState.SENT,
                RecipientDeliveryState.QUEUED,
                RecipientDeliveryState.CANCELLED
            )
            RecipientDeliveryState.HANDED_OFF_TO_MESSAGING_APP -> target in setOf(
                RecipientDeliveryState.SENT,
                RecipientDeliveryState.UNKNOWN,
                RecipientDeliveryState.CANCELLED
            )
            RecipientDeliveryState.QUEUED -> target in setOf(
                RecipientDeliveryState.SUBMITTED,
                RecipientDeliveryState.FAILED,
                RecipientDeliveryState.CANCELLED
            )
            RecipientDeliveryState.SUBMITTED -> target in setOf(
                RecipientDeliveryState.SENT,
                RecipientDeliveryState.FAILED,
                RecipientDeliveryState.UNKNOWN
            )
            RecipientDeliveryState.SENT -> target in setOf(
                RecipientDeliveryState.DELIVERED
            )
            RecipientDeliveryState.DELIVERED,
            RecipientDeliveryState.FAILED,
            RecipientDeliveryState.UNKNOWN,
            RecipientDeliveryState.CANCELLED,
            RecipientDeliveryState.SUPPRESSED_OPT_OUT -> false
        }
    }

    fun canTransitionCampaign(
        current: CampaignStatus,
        target: CampaignStatus
    ): Boolean {
        return when (current) {
            CampaignStatus.DRAFT -> target in setOf(CampaignStatus.VALIDATED, CampaignStatus.CANCELLED)
            CampaignStatus.VALIDATED -> target in setOf(CampaignStatus.AWAITING_CONFIRMATION, CampaignStatus.DRAFT, CampaignStatus.CANCELLED)
            CampaignStatus.AWAITING_CONFIRMATION -> target in setOf(
                CampaignStatus.IN_PROGRESS,
                CampaignStatus.RUNNING,
                CampaignStatus.QUEUED,
                CampaignStatus.HANDED_OFF_TO_MESSAGING_APP,
                CampaignStatus.CANCELLED
            )
            CampaignStatus.QUEUED -> target in setOf(
                CampaignStatus.RUNNING,
                CampaignStatus.IN_PROGRESS,
                CampaignStatus.CANCELLED,
                CampaignStatus.STOPPED
            )
            CampaignStatus.RUNNING,
            CampaignStatus.IN_PROGRESS -> target in setOf(
                CampaignStatus.PAUSED,
                CampaignStatus.COMPLETED,
                CampaignStatus.COMPLETED_WITH_ERRORS,
                CampaignStatus.PARTIAL,
                CampaignStatus.FAILED,
                CampaignStatus.CANCELLED,
                CampaignStatus.STOPPED
            )
            CampaignStatus.PAUSED -> target in setOf(
                CampaignStatus.RUNNING,
                CampaignStatus.IN_PROGRESS,
                CampaignStatus.CANCELLED,
                CampaignStatus.STOPPED
            )
            CampaignStatus.HANDED_OFF_TO_MESSAGING_APP,
            CampaignStatus.SUBMITTED,
            CampaignStatus.COMPLETED,
            CampaignStatus.COMPLETED_WITH_ERRORS,
            CampaignStatus.PARTIAL,
            CampaignStatus.STOPPED,
            CampaignStatus.CANCELLED,
            CampaignStatus.FAILED -> target in setOf(
                CampaignStatus.QUEUED,
                CampaignStatus.RUNNING
            )
        }
    }
}
