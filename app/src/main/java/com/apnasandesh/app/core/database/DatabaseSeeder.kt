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
        val existingProfile = profileAndSettingsDao.getBusinessProfile()
        if (existingProfile == null) {
            profileAndSettingsDao.upsertBusinessProfile(
                BusinessProfileEntity(
                    id = "default_profile",
                    businessName = "",
                    ownerDisplayName = "",
                    businessCategory = "Retail & Services",
                    communicationPhone = "",
                    businessAddress = "",
                    defaultCountryCallingCode = "+91",
                    defaultMessageSignature = "",
                    website = "",
                    currencySymbol = "₹",
                    preferredLanguage = "English",
                    brandTone = "Professional",
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
            )
        } else if (existingProfile.businessName == "Apna General Store") {
            profileAndSettingsDao.upsertBusinessProfile(
                existingProfile.copy(
                    businessName = "",
                    ownerDisplayName = "",
                    defaultMessageSignature = "",
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
            )
        }

        val currentSettings = profileAndSettingsDao.getAppSettings()
        if (currentSettings == null) {
            profileAndSettingsDao.upsertAppSettings(
                AppSettingsEntity(
                    id = "app_settings",
                    onboardingCompleted = false,
                    preferredTransport = "DIRECT_SIM",
                    directSimPolicyAcknowledged = true,
                    directSimFeatureEnabled = true,
                    selectedSimSubscriptionId = null,
                    indiaTraiComplianceMode = true,
                    appendSignatureByDefault = true,
                    themeMode = "SYSTEM",
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        } else if (currentSettings.preferredTransport != "DIRECT_SIM") {
            profileAndSettingsDao.upsertAppSettings(
                currentSettings.copy(
                    preferredTransport = "DIRECT_SIM",
                    directSimPolicyAcknowledged = true,
                    directSimFeatureEnabled = true,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }

        // Seed full library if templates are empty or only initial basic templates exist
        if (templateDao.countTemplates() < 15) {
            templateDao.insertTemplates(TemplateLibraryDefaults.getDefaultTemplates())
        }

        // Remove any legacy sample demo customers
        runCatching {
            customerDao.purgeDemoSampleData()
        }
    }

    suspend fun restoreDefaultTemplates() {
        templateDao.deleteAllSystemDefaultTemplates()
        templateDao.insertTemplates(TemplateLibraryDefaults.getDefaultTemplates())
    }
}
