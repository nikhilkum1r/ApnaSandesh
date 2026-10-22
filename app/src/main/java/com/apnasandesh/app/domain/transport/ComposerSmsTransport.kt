package com.apnasandesh.app.domain.transport

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.apnasandesh.app.core.common.PrivacySafeLogger
import com.apnasandesh.app.core.model.TransportMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComposerSmsTransport @Inject constructor(
    @ApplicationContext private val context: Context
) : SmsTransport {

    override val transportMode: TransportMode = TransportMode.COMPOSER_HANDOFF

    fun buildComposerIntent(destinationPhoneE164: String, messageBody: String): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${Uri.encode(destinationPhoneE164)}")
            putExtra("sms_body", messageBody)
            putExtra(Intent.EXTRA_TEXT, messageBody)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    override fun send(
        recipientRowId: String,
        destinationPhoneE164: String,
        messageBody: String,
        explicitSubscriptionId: Int?
    ): TransportSubmissionOutcome {
        return runCatching {
            val intent = buildComposerIntent(destinationPhoneE164, messageBody)
            if (intent.resolveActivity(context.packageManager) == null) {
                return TransportSubmissionOutcome.FailedImmediately(
                    "No default SMS messaging app found on this device to handle smsto: intents."
                )
            }
            context.startActivity(intent)
            PrivacySafeLogger.info("Handed off campaign recipient to system SMS composer.")
            TransportSubmissionOutcome.HandedOffToComposer
        }.getOrElse { error ->
            PrivacySafeLogger.error("System SMS composer handoff failed", error)
            TransportSubmissionOutcome.FailedImmediately(error.message ?: "Unable to launch SMS composer")
        }
    }
}
