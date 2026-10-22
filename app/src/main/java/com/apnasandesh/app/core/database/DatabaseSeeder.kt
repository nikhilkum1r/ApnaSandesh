package com.apnasandesh.app.core.database

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val templateDao: TemplateDao,
    private val profileAndSettingsDao: ProfileAndSettingsDao,
    private val customerDao: CustomerDao
) {
    suspend fun ensureSeeded() {
        if (profileAndSettingsDao.getBusinessProfile() == null) {
            profileAndSettingsDao.upsertBusinessProfile(
                BusinessProfileEntity(
                    id = "default_profile",
                    businessName = "Apna General Store",
                    ownerDisplayName = "Store Owner",
                    businessCategory = "Retail & Services",
                    communicationPhone = "",
                    businessAddress = "",
                    defaultCountryCallingCode = "+91",
                    defaultMessageSignature = "- Apna General Store",
                    website = "",
                    currencySymbol = "₹",
                    preferredLanguage = "English",
                    brandTone = "Professional",
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
            )
        }

        if (profileAndSettingsDao.getAppSettings() == null) {
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

        // Seed full library if templates are empty or only initial basic templates exist
        if (templateDao.countTemplates() < 15) {
            templateDao.insertTemplates(TemplateLibraryDefaults.getDefaultTemplates())
        }

        // Seed sample demo customers if directory is brand new
        if (customerDao.countCustomers() == 0) {
            val sampleCustomers = listOf(
                CustomerEntity(
                    fullName = "Aarav Sharma",
                    rawPhoneNumber = "+919876543210",
                    normalizedPhoneE164 = "+919876543210",
                    isPhoneValid = true,
                    tagsCsv = "VIP, Regular",
                    consentStatus = "GRANTED",
                    consentSource = "DEMO_SAMPLE",
                    company = "Apex Retail",
                    notes = "Sample customer created for onboarding",
                    lastOrderId = "ORD-101",
                    lastOrderAmount = "1,450"
                ),
                CustomerEntity(
                    fullName = "Priya Patel",
                    rawPhoneNumber = "+919876543211",
                    normalizedPhoneE164 = "+919876543211",
                    isPhoneValid = true,
                    tagsCsv = "New, Lead",
                    consentStatus = "GRANTED",
                    consentSource = "DEMO_SAMPLE",
                    company = "Sunrise Studio",
                    notes = "Sample customer created for onboarding",
                    lastOrderId = "ORD-102",
                    lastOrderAmount = "2,890"
                ),
                CustomerEntity(
                    fullName = "Vikram Malhotra",
                    rawPhoneNumber = "+919876543212",
                    normalizedPhoneE164 = "+919876543212",
                    isPhoneValid = true,
                    tagsCsv = "Corporate, Regular",
                    consentStatus = "GRANTED",
                    consentSource = "DEMO_SAMPLE",
                    company = "Malhotra Logistics",
                    notes = "Sample customer created for onboarding",
                    lastOrderId = "ORD-103",
                    lastOrderAmount = "5,200"
                )
            )
            sampleCustomers.forEach { customerDao.insertCustomer(it) }
        }
    }

    suspend fun restoreDefaultTemplates() {
        templateDao.deleteAllSystemDefaultTemplates()
        templateDao.insertTemplates(TemplateLibraryDefaults.getDefaultTemplates())
    }
}
