package com.apnasandesh.app.domain.transport

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.apnasandesh.app.core.database.CampaignDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsStatusBroadcastReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReceiverEntryPoint {
        fun campaignDao(): CampaignDao
    }

    override fun onReceive(context: Context, intent: Intent) {
        val recipientRowId = intent.getStringExtra(EXTRA_RECIPIENT_ROW_ID) ?: return
        val subscriptionId = intent.getIntExtra(EXTRA_SUBSCRIPTION_ID, -1).takeIf { it != -1 }

        // CRITICAL: resultCode MUST be captured synchronously BEFORE goAsync().
        // Calling goAsync() nullifies mPendingResult in BroadcastReceiver, which causes
        // any subsequent access to resultCode / getResultCode() to unconditionally return 0!
        val capturedResultCode = resultCode
        val pendingResult = goAsync()

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReceiverEntryPoint::class.java
        )
        val campaignDao = entryPoint.campaignDao()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val recipient = campaignDao.getCampaignRecipientById(recipientRowId)
                val campaignId = recipient?.campaignId.orEmpty()

                when (intent.action) {
                    ACTION_SMS_SENT -> {
                        if (capturedResultCode == Activity.RESULT_OK) {
                            campaignDao.recordSendAttemptAndTransitionState(
                                recipientRowId = recipientRowId,
                                campaignId = campaignId,
                                newState = "SENT",
                                transportMode = "DIRECT_SIM",
                                subscriptionId = subscriptionId,
                                errorCode = null,
                                errorMessage = null
                            )
                        } else {
                            val (code, reason) = when (capturedResultCode) {
                                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "RESULT_ERROR_GENERIC_FAILURE" to "Generic carrier failure / insufficient balance"
                                SmsManager.RESULT_ERROR_NO_SERVICE -> "RESULT_ERROR_NO_SERVICE" to "No cellular service available"
                                SmsManager.RESULT_ERROR_RADIO_OFF -> "RESULT_ERROR_RADIO_OFF" to "Cellular radio is turned off / Airplane mode"
                                SmsManager.RESULT_ERROR_NULL_PDU -> "RESULT_ERROR_NULL_PDU" to "Null PDU error from telephony layer"
                                SmsManager.RESULT_ERROR_LIMIT_EXCEEDED -> "RESULT_ERROR_LIMIT_EXCEEDED" to "Carrier or Android SMS quota limit exceeded"
                                Activity.RESULT_CANCELED -> "RESULT_CANCELED" to "SMS transmission cancelled (SIM slot not ready or carrier cancelled)"
                                else -> "ERROR_CODE_$capturedResultCode" to "SMS transmission failed (code $capturedResultCode)"
                            }
                            campaignDao.recordSendAttemptAndTransitionState(
                                recipientRowId = recipientRowId,
                                campaignId = campaignId,
                                newState = "FAILED",
                                transportMode = "DIRECT_SIM",
                                subscriptionId = subscriptionId,
                                errorCode = code,
                                errorMessage = reason
                            )
                        }

                        if (campaignId.isNotBlank()) {
                            checkAndFinalizeCampaignIfDone(campaignDao, campaignId)
                        }
                    }
                    ACTION_SMS_DELIVERED -> {
                        if (capturedResultCode == Activity.RESULT_OK) {
                            campaignDao.recordSendAttemptAndTransitionState(
                                recipientRowId = recipientRowId,
                                campaignId = campaignId,
                                newState = "DELIVERED",
                                transportMode = "DIRECT_SIM",
                                subscriptionId = subscriptionId,
                                errorCode = null,
                                errorMessage = null
                            )
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun checkAndFinalizeCampaignIfDone(campaignDao: CampaignDao, campaignId: String) {
        val remaining = campaignDao.getCampaignRecipientsSnapshot(campaignId).filter {
            it.state in setOf("DRAFT", "VALIDATED", "AWAITING_CONFIRMATION", "QUEUED", "SUBMITTED", "HANDED_OFF_TO_MESSAGING_APP")
        }
        if (remaining.isEmpty()) {
            val all = campaignDao.getCampaignRecipientsSnapshot(campaignId)
            val failed = all.count { it.state == "FAILED" }
            val sent = all.count { it.state == "SENT" || it.state == "DELIVERED" }
            val finalStatus = when {
                sent > 0 && failed > 0 -> "COMPLETED_WITH_ERRORS"
                sent > 0 && failed == 0 -> "COMPLETED"
                sent == 0 && failed > 0 -> "FAILED"
                else -> "COMPLETED"
            }
            campaignDao.updateCampaignStatus(
                campaignId = campaignId,
                status = finalStatus,
                completedAt = System.currentTimeMillis()
            )
        }
    }

    companion object {
        const val ACTION_SMS_SENT = "com.apnasandesh.app.ACTION_SMS_SENT"
        const val ACTION_SMS_DELIVERED = "com.apnasandesh.app.ACTION_SMS_DELIVERED"
        const val EXTRA_RECIPIENT_ROW_ID = "extra_recipient_row_id"
        const val EXTRA_SUBSCRIPTION_ID = "extra_subscription_id"
    }
}
