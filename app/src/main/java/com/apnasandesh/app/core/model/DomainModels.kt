package com.apnasandesh.app.core.model

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
    // Comprehensive Categories A through H
    GREETINGS_RELATIONSHIPS("Greetings & Relationships", false),
    MARKETING_PROMOTIONS("Marketing & Promotions", true),
    SALES_LEADS("Sales & Lead Follow-ups", false),
    ORDERS_PAYMENTS("Orders & Payments", false),
    APPOINTMENTS_SERVICES("Appointments & Services", false),
    CUSTOMER_RETENTION("Customer Retention", false),
    INDUSTRY_SPECIFIC("Industry-Specific", false),
    OPERATIONAL("Operational & Updates", false),

    // Backward-compatible legacy categories
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

enum class TemplateTone(val displayName: String) {
    PROFESSIONAL("Professional"),
    FRIENDLY("Friendly"),
    WARM("Warm"),
    CASUAL("Casual"),
    PREMIUM("Premium"),
    PROMOTIONAL("Promotional"),
    CONCISE("Concise")
}

enum class TemplateLanguage(val displayName: String) {
    ENGLISH("English"),
    HINDI("Hindi (हिंदी)"),
    HINGLISH("Hinglish")
}

enum class IndustryType(val displayName: String) {
    GENERAL("General Business"),
    RETAIL("Retail Shops"),
    GROCERY("Grocery Stores"),
    ELECTRONICS("Electronics Shops"),
    CLOTHING("Clothing Stores"),
    RESTAURANT("Restaurants & Cafés"),
    SALON("Salons & Beauty"),
    CLINIC("Clinics & Wellness"),
    COACHING("Coaching & Education"),
    REAL_ESTATE("Real Estate Agents"),
    FREELANCER("Freelancers & Consultants"),
    REPAIR("Repair & Maintenance"),
    TRAVEL("Travel Agencies"),
    ECOMMERCE("E-commerce"),
    B2B("B2B Companies"),
    LOCAL_SERVICES("Local Services")
}

data class TemplateVariable(
    val token: String,
    val displayName: String,
    val description: String,
    val sampleValue: String,
    val group: String
)

enum class CampaignStatus {
    DRAFT,
    VALIDATED,
    AWAITING_CONFIRMATION,
    IN_PROGRESS,
    RUNNING,
    PAUSED,
    HANDED_OFF_TO_MESSAGING_APP,
    QUEUED,
    SUBMITTED,
    COMPLETED,
    COMPLETED_WITH_ERRORS,
    PARTIAL,
    STOPPED,
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

