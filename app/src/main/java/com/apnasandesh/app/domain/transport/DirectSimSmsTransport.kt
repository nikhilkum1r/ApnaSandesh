package com.apnasandesh.app.domain.transport

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import com.apnasandesh.app.core.common.PrivacySafeLogger
import com.apnasandesh.app.core.model.TransportMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DirectSimSmsTransport @Inject constructor(
    @ApplicationContext private val context: Context,
    private val policyGate: TransportPolicyGate
) : SmsTransport {

    override val transportMode: TransportMode = TransportMode.DIRECT_SIM

    override fun send(
        recipientRowId: String,
        destinationPhoneE164: String,
        messageBody: String,
        explicitSubscriptionId: Int?
    ): TransportSubmissionOutcome {
        if (!policyGate.hasDirectSmsRuntimePermissions()) {
            return TransportSubmissionOutcome.BlockedByPolicyOrPermission(
                "SEND_SMS and READ_PHONE_STATE permissions have not been granted."
            )
        }

        val activeSubs = policyGate.getActiveSimSubscriptions()
        if (activeSubs.isEmpty()) {
            return TransportSubmissionOutcome.FailedImmediately("No active SIM card detected in device.")
        }

        val resolvedSubId = explicitSubscriptionId ?: activeSubs.first().subscriptionId
        if (activeSubs.none { it.subscriptionId == resolvedSubId }) {
            return TransportSubmissionOutcome.FailedImmediately(
                "INVALID_SUBSCRIPTION_ID: Selected SIM subscription ID ($resolvedSubId) is no longer active. Will not silently switch SIMs."
            )
        }

        return runCatching {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
                    .createForSubscriptionId(resolvedSubId)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getSmsManagerForSubscriptionId(resolvedSubId)
            }

            val parts = smsManager.divideMessage(messageBody)
            val sentIntents = ArrayList<PendingIntent>(parts.size)
            val deliveryIntents = ArrayList<PendingIntent>(parts.size)

            parts.forEachIndexed { partIndex, _ ->
                val requestCode = (recipientRowId.hashCode() xor partIndex)
                val sentIntent = Intent(context, SmsStatusBroadcastReceiver::class.java).apply {
                    action = SmsStatusBroadcastReceiver.ACTION_SMS_SENT
                    putExtra(SmsStatusBroadcastReceiver.EXTRA_RECIPIENT_ROW_ID, recipientRowId)
                    putExtra(SmsStatusBroadcastReceiver.EXTRA_SUBSCRIPTION_ID, resolvedSubId)
                }
                val deliveredIntent = Intent(context, SmsStatusBroadcastReceiver::class.java).apply {
                    action = SmsStatusBroadcastReceiver.ACTION_SMS_DELIVERED
                    putExtra(SmsStatusBroadcastReceiver.EXTRA_RECIPIENT_ROW_ID, recipientRowId)
                    putExtra(SmsStatusBroadcastReceiver.EXTRA_SUBSCRIPTION_ID, resolvedSubId)
                }

                sentIntents.add(
                    PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        sentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                deliveryIntents.add(
                    PendingIntent.getBroadcast(
                        context,
                        requestCode + 10_000,
                        deliveredIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }

            if (parts.size == 1) {
                smsManager.sendTextMessage(
                    destinationPhoneE164,
                    null,
                    messageBody,
                    sentIntents.first(),
                    deliveryIntents.first()
                )
            } else {
                smsManager.sendMultipartTextMessage(
                    destinationPhoneE164,
                    null,
                    parts,
                    sentIntents,
                    deliveryIntents
                )
            }
            TransportSubmissionOutcome.SubmittedToSimRadio
        }.getOrElse { error ->
            PrivacySafeLogger.error("Direct SIM submission threw exception", error)
            TransportSubmissionOutcome.FailedImmediately(
                error.message ?: "Carrier radio rejected submission"
            )
        }
    }
}
