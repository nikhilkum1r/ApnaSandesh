package com.localreach.bizsms.domain.transport

import com.localreach.bizsms.core.model.TransportMode

sealed class TransportSubmissionOutcome {
    data object HandedOffToComposer : TransportSubmissionOutcome()
    data object SubmittedToSimRadio : TransportSubmissionOutcome()
    data class BlockedByPolicyOrPermission(val reason: String) : TransportSubmissionOutcome()
    data class FailedImmediately(val diagnostic: String) : TransportSubmissionOutcome()
}

interface SmsTransport {
    val transportMode: TransportMode

    fun send(
        recipientRowId: String,
        destinationPhoneE164: String,
        messageBody: String,
        explicitSubscriptionId: Int? = null
    ): TransportSubmissionOutcome
}
