package com.localreach.bizsms.domain.validation

import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

data class TraiComplianceAdvisory(
    val requiresWarningBanner: Boolean,
    val exceedsDailyPersonalSimCap: Boolean,
    val isOutsidePromotionalWindow: Boolean,
    val title: String,
    val guidanceBullets: List<String>
)

@Singleton
class TraiDltComplianceAdvisor @Inject constructor() {

    /**
     * Evaluates campaign characteristics against Indian TRAI TCCCPR (2018) regulations and SIM Fair-Use limits.
     */
    fun evaluateCampaign(
        recipientCount: Int,
        isPromotionalCategory: Boolean,
        countryCode: String,
        currentTime: LocalTime = LocalTime.now(ZoneId.of("Asia/Kolkata"))
    ): TraiComplianceAdvisory {
        if (countryCode != "+91") {
            return TraiComplianceAdvisory(
                requiresWarningBanner = recipientCount > 50,
                exceedsDailyPersonalSimCap = false,
                isOutsidePromotionalWindow = false,
                title = "Carrier Fair-Use & Customer Consent Notice",
                guidanceBullets = listOf(
                    "Personal SIM plans are intended for direct customer communication and may enforce daily rate limits.",
                    "Ensure every recipient has provided consent and honor all STOP/opt-out requests immediately."
                )
            )
        }

        val exceeds100SmsCap = recipientCount > 100
        val isOutsideWindow = if (isPromotionalCategory) {
            val startWindow = LocalTime.of(10, 0)
            val endWindow = LocalTime.of(21, 0)
            currentTime.isBefore(startWindow) || currentTime.isAfter(endWindow)
        } else {
            false
        }

        val bullets = mutableListOf<String>()
        bullets.add("Personal SIM Daily Quota: Indian telecom operators (Jio, Airtel, Vi, BSNL) enforce a strict limit of 100 SMS/day per SIM on standard consumer plans (TRAI telecom regulations).")

        if (isPromotionalCategory) {
            bullets.add("Promotional Messaging Hours: Under TRAI TCCCPR regulations, promotional communications are strictly permitted only between 10:00 AM and 9:00 PM IST.")
            if (isOutsideWindow) {
                bullets.add("TIMING WARNING: The current time is outside the permitted 10:00 AM - 9:00 PM IST window for promotional messages.")
            }
            bullets.add("Opt-In Requirement: Promotional messages may only be sent to customers who explicitly granted opt-in consent.")
            bullets.add("DLT Transparency: This app does not bypass TRAI DLT distributed ledger registration or telecom operator scrubbing. For high-volume automated commercial broadcasts, please register with a Principal Entity & Telemarketer DLT portal.")
        } else {
            bullets.add("Transactional / Service Communication: Use for direct customer follow-ups (order updates, booking reminders, service status) with customers who have an active business relationship.")
        }

        if (exceeds100SmsCap) {
            bullets.add("ACTION REQUIRED: Selected batch size ($recipientCount) exceeds the 100 SMS/day personal SIM threshold. Reduce your recipient batch to avoid carrier rate-limiting or SIM suspension.")
        }

        return TraiComplianceAdvisory(
            requiresWarningBanner = true,
            exceedsDailyPersonalSimCap = exceeds100SmsCap,
            isOutsidePromotionalWindow = isOutsideWindow,
            title = "India Telecom Regulatory Notice (TRAI TCCCPR & DLT)",
            guidanceBullets = bullets
        )
    }
}
