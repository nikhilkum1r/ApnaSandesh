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
        val pendingResult = goAsync()

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReceiverEntryPoint::class.java
        )
        val campaignDao = entryPoint.campaignDao()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val recipient = campaignDao.getCampaignRecipientById(recipientRowId)
                val campaignId = recipient?.campaignId ?: ""

                when (intent.action) {
                    ACTION_SMS_SENT -> {
                        if (resultCode == Activity.RESULT_OK) {
                            campaignDao.recordSendAttemptAndTransitionState(
                                recipientRowId = recipientRowId,
                                campaignId = campaignId,
                                newState = "SENT",
                                transportMode = "DIRECT_SIM",
                                subscriptionId = subscriptionId,
                                errorCode = null,
                                errorMessage = "Accepted by cellular network (SENT)"
                            )
                        } else {
                            val (code, reason) = when (resultCode) {
                                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "RESULT_ERROR_GENERIC_FAILURE" to "Generic carrier failure / insufficient balance"
                                SmsManager.RESULT_ERROR_NO_SERVICE -> "RESULT_ERROR_NO_SERVICE" to "No cellular service available"
                                SmsManager.RESULT_ERROR_RADIO_OFF -> "RESULT_ERROR_RADIO_OFF" to "Cellular radio is turned off / Airplane mode"
                                SmsManager.RESULT_ERROR_NULL_PDU -> "RESULT_ERROR_NULL_PDU" to "Null PDU error from telephony layer"
                                SmsManager.RESULT_ERROR_LIMIT_EXCEEDED -> "RESULT_ERROR_LIMIT_EXCEEDED" to "Carrier or Android SMS quota limit exceeded"
                                else -> "ERROR_CODE_$resultCode" to "SMS transmission failed (code $resultCode)"
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
                    }
                    ACTION_SMS_DELIVERED -> {
                        if (resultCode == Activity.RESULT_OK) {
                            campaignDao.recordSendAttemptAndTransitionState(
                                recipientRowId = recipientRowId,
                                campaignId = campaignId,
                                newState = "DELIVERED",
                                transportMode = "DIRECT_SIM",
                                subscriptionId = subscriptionId,
                                errorCode = null,
                                errorMessage = "Confirmed by carrier delivery receipt"
                            )
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SMS_SENT = "com.apnasandesh.app.ACTION_SMS_SENT"
        const val ACTION_SMS_DELIVERED = "com.apnasandesh.app.ACTION_SMS_DELIVERED"
        const val EXTRA_RECIPIENT_ROW_ID = "extra_recipient_row_id"
        const val EXTRA_SUBSCRIPTION_ID = "extra_subscription_id"
    }
}
