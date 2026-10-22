package com.localreach.bizsms.core.model

enum class ConsentStatus {
    GRANTED,
    UNKNOWN,
    OPTED_OUT
}

enum class ConsentSource {
    MANUAL_ENTRY,
    CSV_IMPORT,
    CONTACT_PICKER,
    CUSTOMER_REQUEST,
    IN_STORE_REGISTER
}

enum class TemplateCategory(val displayName: String, val isPromotional: Boolean) {
    ORDER_CONFIRMATION("Order Confirmation", false),
    APPOINTMENT_REMINDER("Appointment Reminder", false),
    PAYMENT_REMINDER("Payment Reminder", false),
    SERVICE_COMPLETION("Service Completion", false),
    DELIVERY_UPDATE("Delivery Update", false),
    THANK_YOU("Customer Thank-You", false),
    ANNOUNCEMENT("Business Announcement", false),
    OPT_IN_PROMOTION("Opt-In Promotion", true),
    FESTIVAL_GREETING("Festival Greeting", true),
    CUSTOM("Custom Message", false)
}

enum class CampaignStatus {
    DRAFT,
    VALIDATED,
    AWAITING_CONFIRMATION,
    IN_PROGRESS,
    HANDED_OFF_TO_MESSAGING_APP,
    QUEUED,
    SUBMITTED,
    COMPLETED,
    PARTIAL,
    CANCELLED,
    FAILED
}

enum class RecipientDeliveryState {
    DRAFT,
    VALIDATED,
    AWAITING_CONFIRMATION,
    HANDED_OFF_TO_MESSAGING_APP,
    QUEUED,
    SUBMITTED,
    SENT,
    DELIVERED,
    FAILED,
    UNKNOWN,
    CANCELLED,
    SUPPRESSED_OPT_OUT
}

enum class TransportMode {
    COMPOSER_HANDOFF,
    DIRECT_SIM
}

enum class SmsEncoding {
    GSM_7BIT,
    UCS_2
}

data class SmsEncodingResult(
    val encoding: SmsEncoding,
    val isUcs2Unicode: Boolean,
    val charCount: Int,
    val segmentCount: Int,
    val remainingInCurrentSegment: Int,
    val maxPerSingleSegment: Int,
    val maxPerMultipartSegment: Int
)

data class SimSubscriptionInfo(
    val subscriptionId: Int,
    val simSlotIndex: Int,
    val carrierName: String,
    val displayName: String
)
