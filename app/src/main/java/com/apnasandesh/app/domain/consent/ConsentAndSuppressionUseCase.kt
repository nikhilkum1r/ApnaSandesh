package com.apnasandesh.app.domain.consent

import com.apnasandesh.app.core.database.CampaignDao
import com.apnasandesh.app.core.database.CustomerDao
import com.apnasandesh.app.core.database.CustomerEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentAndSuppressionUseCase @Inject constructor(
    private val customerDao: CustomerDao,
    private val campaignDao: CampaignDao
) {

    suspend fun isRecipientEligible(customer: CustomerEntity, isPromotional: Boolean): Boolean {
        if (customer.isOptedOut || customer.consentStatus == "OPTED_OUT" || !customer.isPhoneValid) {
            return false
        }
        if (isPromotional && customer.consentStatus != "GRANTED") {
            return false
        }
        // Double check permanent suppression table
        return !customerDao.isPhoneSuppressed(customer.normalizedPhoneE164)
    }

    suspend fun recordOptOut(customerId: String, normalizedPhone: String, reason: String) {
        customerDao.permanentlyOptOutCustomer(customerId, normalizedPhone, reason)
        campaignDao.suppressPendingRecipientsByPhone(normalizedPhone)
    }

    suspend fun validateRecipientPhoneBeforeSend(phone: String): Boolean {
        return !customerDao.isPhoneSuppressed(phone)
    }
}
