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
                "SEND_SMS permission has not been granted."
            )
        }

        val activeSubs = policyGate.getActiveSimSubscriptions()
        val defaultSubId = policyGate.getDefaultSubscriptionId()

        val resolvedSubId = when {
            explicitSubscriptionId != null && activeSubs.any { it.subscriptionId == explicitSubscriptionId } -> explicitSubscriptionId
            explicitSubscriptionId != null && explicitSubscriptionId != android.telephony.SubscriptionManager.INVALID_SUBSCRIPTION_ID -> explicitSubscriptionId
            activeSubs.isNotEmpty() -> activeSubs.first().subscriptionId
            defaultSubId != null -> defaultSubId
            else -> null
        }

        val cleanDestination = destinationPhoneE164.trim().replace("[^0-9+]".toRegex(), "")

        return runCatching {
            val subIdToUse = resolvedSubId?.takeIf { it != android.telephony.SubscriptionManager.INVALID_SUBSCRIPTION_ID }
            val smsManager: SmsManager = if (subIdToUse != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)?.createForSubscriptionId(subIdToUse)
                        ?: @Suppress("DEPRECATION") SmsManager.getSmsManagerForSubscriptionId(subIdToUse)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getSmsManagerForSubscriptionId(subIdToUse)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                        ?: @Suppress("DEPRECATION") SmsManager.getDefault()
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
            }

            val parts = smsManager.divideMessage(messageBody)
            val sentIntents = ArrayList<PendingIntent>(parts.size)
            val deliveryIntents = ArrayList<PendingIntent>(parts.size)

            parts.forEachIndexed { partIndex, _ ->
                val requestCode = kotlin.math.abs(recipientRowId.hashCode() xor (partIndex shl 8))
                val sentIntent = Intent(context, SmsStatusBroadcastReceiver::class.java).apply {
                    action = SmsStatusBroadcastReceiver.ACTION_SMS_SENT
                    putExtra(SmsStatusBroadcastReceiver.EXTRA_RECIPIENT_ROW_ID, recipientRowId)
                    putExtra(SmsStatusBroadcastReceiver.EXTRA_SUBSCRIPTION_ID, subIdToUse)
                }
                val deliveredIntent = Intent(context, SmsStatusBroadcastReceiver::class.java).apply {
                    action = SmsStatusBroadcastReceiver.ACTION_SMS_DELIVERED
                    putExtra(SmsStatusBroadcastReceiver.EXTRA_RECIPIENT_ROW_ID, recipientRowId)
                    putExtra(SmsStatusBroadcastReceiver.EXTRA_SUBSCRIPTION_ID, subIdToUse)
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
                    cleanDestination,
                    null,
                    messageBody,
                    sentIntents.first(),
                    deliveryIntents.first()
                )
            } else {
                smsManager.sendMultipartTextMessage(
                    cleanDestination,
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
