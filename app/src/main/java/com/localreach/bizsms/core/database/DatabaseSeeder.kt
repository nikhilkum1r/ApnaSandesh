package com.localreach.bizsms.core.database

import com.localreach.bizsms.core.model.TemplateCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val templateDao: TemplateDao,
    private val profileAndSettingsDao: ProfileAndSettingsDao
) {
    suspend fun ensureSeeded() {
        if (profileAndSettingsDao.getBusinessProfile() == null) {
            profileAndSettingsDao.upsertBusinessProfile(
                BusinessProfileEntity(
                    id = "default_profile",
                    businessName = "My Local Shop",
                    ownerDisplayName = "Shop Owner",
                    businessCategory = "Retail & Services",
                    communicationPhone = "",
                    businessAddress = "",
                    defaultCountryCallingCode = "+91",
                    defaultMessageSignature = "- My Local Shop",
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
            )
            profileAndSettingsDao.upsertAppSettings(
                AppSettingsEntity(
                    id = "app_settings",
                    onboardingCompleted = false,
                    preferredTransport = "COMPOSER_HANDOFF",
                    directSimPolicyAcknowledged = false,
                    directSimFeatureEnabled = false,
                    selectedSimSubscriptionId = null,
                    indiaTraiComplianceMode = true,
                    appendSignatureByDefault = true,
                    themeMode = "SYSTEM",
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }

        if (templateDao.countTemplates() == 0) {
            val defaults = listOf(
                MessageTemplateEntity(
                    id = "tpl_order_confirm",
                    title = "Order Confirmation",
                    category = TemplateCategory.ORDER_CONFIRMATION.name,
                    bodyTemplate = "Hello {{customer_name}}, thank you for your order with {{business_name}}. We are preparing it now and will notify you when it is ready.",
                    isPromotional = false,
                    isSystemDefault = true
                ),
                MessageTemplateEntity(
                    id = "tpl_appointment_reminder",
                    title = "Appointment Reminder",
                    category = TemplateCategory.APPOINTMENT_REMINDER.name,
                    bodyTemplate = "Hi {{customer_name}}, this is a friendly reminder of your upcoming appointment at {{business_name}}. Call {{business_phone}} if you need to reschedule.",
                    isPromotional = false,
                    isSystemDefault = true
                ),
                MessageTemplateEntity(
                    id = "tpl_payment_reminder",
                    title = "Payment Reminder",
                    category = TemplateCategory.PAYMENT_REMINDER.name,
                    bodyTemplate = "Dear {{customer_name}}, this is a gentle reminder from {{business_name}} regarding your pending invoice balance. Please ignore if already paid.",
                    isPromotional = false,
                    isSystemDefault = true
                ),
                MessageTemplateEntity(
                    id = "tpl_service_completion",
                    title = "Service Completion",
                    category = TemplateCategory.SERVICE_COMPLETION.name,
                    bodyTemplate = "Hello {{customer_name}}, your service request at {{business_name}} is complete and ready for pickup. For queries, reach us at {{business_phone}}.",
                    isPromotional = false,
                    isSystemDefault = true
                ),
                MessageTemplateEntity(
                    id = "tpl_delivery_update",
                    title = "Delivery Update",
                    category = TemplateCategory.DELIVERY_UPDATE.name,
                    bodyTemplate = "Hi {{customer_name}}, your order from {{business_name}} is out for local delivery today. Please keep your phone reachable.",
                    isPromotional = false,
                    isSystemDefault = true
                ),
                MessageTemplateEntity(
                    id = "tpl_thank_you",
                    title = "Customer Thank-You",
                    category = TemplateCategory.THANK_YOU.name,
                    bodyTemplate = "Thank you for visiting {{business_name}}, {{customer_name}}! We truly appreciate your support and look forward to serving you again.",
                    isPromotional = false,
                    isSystemDefault = true
                ),
                MessageTemplateEntity(
                    id = "tpl_announcement",
                    title = "Business Announcement",
                    category = TemplateCategory.ANNOUNCEMENT.name,
                    bodyTemplate = "Hello {{customer_name}}, an important update from {{business_name}}: please note our updated store timings this week. Contact {{business_phone}} for assistance.",
                    isPromotional = false,
                    isSystemDefault = true
                ),
                MessageTemplateEntity(
                    id = "tpl_opt_in_promo",
                    title = "Opt-In Customer Promotion",
                    category = TemplateCategory.OPT_IN_PROMOTION.name,
                    bodyTemplate = "Hi {{customer_name}}, as a valued opted-in member of {{business_name}}, enjoy our seasonal special this weekend in-store!",
                    isPromotional = true,
                    isSystemDefault = true
                ),
                MessageTemplateEntity(
                    id = "tpl_festival_greeting",
                    title = "Festival Greeting",
                    category = TemplateCategory.FESTIVAL_GREETING.name,
                    bodyTemplate = "Warm festive greetings to you and your family, {{customer_name}}! Wishing you joy and prosperity from everyone at {{business_name}}.",
                    isPromotional = true,
                    isSystemDefault = true
                ),
                MessageTemplateEntity(
                    id = "tpl_custom_blank",
                    title = "Custom Business Message",
                    category = TemplateCategory.CUSTOM.name,
                    bodyTemplate = "Hello {{customer_name}}, update from {{business_name}}: ",
                    isPromotional = false,
                    isSystemDefault = true
                )
            )
            templateDao.insertTemplates(defaults)
        }
    }
}
