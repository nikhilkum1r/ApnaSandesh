package com.localreach.bizsms

import com.localreach.bizsms.core.database.BusinessProfileEntity
import com.localreach.bizsms.core.database.CustomerEntity
import com.localreach.bizsms.core.model.CampaignStatus
import com.localreach.bizsms.core.model.RecipientDeliveryState
import com.localreach.bizsms.domain.campaign.CampaignStateMachine
import com.localreach.bizsms.domain.validation.CsvCustomerParser
import com.localreach.bizsms.domain.validation.PhoneNormalizer
import com.localreach.bizsms.domain.validation.PhoneValidationResult
import com.localreach.bizsms.domain.validation.SmsSegmentCalculator
import com.localreach.bizsms.domain.validation.TemplateRenderer
import com.localreach.bizsms.domain.validation.TraiDltComplianceAdvisor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.time.LocalTime

class DomainLogicUnitTest {

    private val normalizer = PhoneNormalizer()
    private val segmentCalculator = SmsSegmentCalculator()
    private val renderer = TemplateRenderer()
    private val stateMachine = CampaignStateMachine()
    private val csvParser = CsvCustomerParser(normalizer)
    private val traiAdvisor = TraiDltComplianceAdvisor()

    // 1. Phone Normalization Tests
    @Test
    fun testPhoneNumberNormalizationIndianStandard() {
        val result = normalizer.normalize("9876543210", "+91")
        assertTrue(result is PhoneValidationResult.Valid)
        assertEquals("+919876543210", (result as PhoneValidationResult.Valid).e164Phone)
    }

    @Test
    fun testPhoneNumberNormalizationIndianWithTrunkZero() {
        val result = normalizer.normalize("09876543210", "+91")
        assertTrue(result is PhoneValidationResult.Valid)
        assertEquals("+919876543210", (result as PhoneValidationResult.Valid).e164Phone)
    }

    @Test
    fun testPhoneNumberNormalizationWithFormattingNoise() {
        val result = normalizer.normalize("+91 (9876) 543-210", "+91")
        assertTrue(result is PhoneValidationResult.Valid)
        assertEquals("+919876543210", (result as PhoneValidationResult.Valid).e164Phone)
    }

    @Test
    fun testPhoneNumberNormalizationWithLeading00() {
        val result = normalizer.normalize("00919876543210", "+91")
        assertTrue(result is PhoneValidationResult.Valid)
        assertEquals("+919876543210", (result as PhoneValidationResult.Valid).e164Phone)
    }

    @Test
    fun testPhoneNumberNormalizationInternational() {
        val result = normalizer.normalize("+14155552671", "+1")
        assertTrue(result is PhoneValidationResult.Valid)
        assertEquals("+14155552671", (result as PhoneValidationResult.Valid).e164Phone)
    }

    @Test
    fun testPhoneNumberRejectionInvalidLengthAndDigits() {
        val tooShort = normalizer.normalize("12345", "+91")
        assertTrue(tooShort is PhoneValidationResult.Invalid)

        val invalidChars = normalizer.normalize("98765abcde", "+91")
        assertTrue(invalidChars is PhoneValidationResult.Invalid)

        // Invalid Indian mobile prefix (starts with 1 instead of 6,7,8,9)
        val invalidPrefix = normalizer.normalize("1234567890", "+91")
        assertTrue(invalidPrefix is PhoneValidationResult.Invalid)
    }

    // 2. Duplicate Detection & CSV Import Parsing Tests
    @Test
    fun testCsvCustomerParsingDeduplicationAndSuppression() {
        val csv = """
            Name,Phone,Tags,Consent,Notes
            Rohan Gupta,9876543210,regular,GRANTED,First entry
            Rohan Duplicate,+919876543210,vip,GRANTED,Duplicate entry
            Suppressed Contact,9876500000,vip,GRANTED,Suppressed
            Invalid Customer,12345,bad,GRANTED,Invalid phone
        """.trimIndent()

        val summary = csvParser.parseCsvStream(
            inputStream = ByteArrayInputStream(csv.toByteArray()),
            defaultCountryCode = "+91",
            existingSuppressedPhones = setOf("+919876500000")
        )

        assertEquals(1, summary.validCustomers.size)
        assertEquals("+919876543210", summary.validCustomers.first().normalizedPhoneE164)
        assertEquals(1, summary.duplicatePhonesInFile.size)
        assertEquals("+919876543210", summary.duplicatePhonesInFile.first())
        assertEquals(1, summary.suppressedSkippedCount)
        assertEquals(1, summary.invalidRows.size)
    }

    // 3. SMS Character Count & Segment Calculation Tests
    @Test
    fun testSmsCalculationSingleSegmentGsm7() {
        val msg = "Hello from LocalReach SMS. Your order is ready for pickup."
        val result = segmentCalculator.calculate(msg)
        assertFalse(result.isUcs2Unicode)
        assertEquals(1, result.segmentCount)
        assertEquals(160, result.maxPerSingleSegment)
    }

    @Test
    fun testSmsCalculationMultipartGsm7() {
        val msg = "A".repeat(161)
        val result = segmentCalculator.calculate(msg)
        assertFalse(result.isUcs2Unicode)
        assertEquals(2, result.segmentCount)
        assertEquals(153, result.maxPerMultipartSegment)
    }

    @Test
    fun testSmsCalculationExtendedGsm7Characters() {
        // Extended characters count as 2 septets: ^, {, }, \, [, ~, ], |, €
        val msg = "Price: 10 € only"
        val result = segmentCalculator.calculate(msg)
        assertFalse(result.isUcs2Unicode)
        // 'Price: 10  only' is 15 chars, plus € as 2 septets = 16 septets
        assertEquals(17, result.charCount)
        assertEquals(1, result.segmentCount)
    }

    @Test
    fun testSmsCalculationUnicodeUcs2SingleAndMultipart() {
        // Hindi / Devanagari script triggers UCS-2
        val hindiMsg = "नमस्ते ग्राहक, आपका ऑर्डर तैयार है।"
        val singleResult = segmentCalculator.calculate(hindiMsg)
        assertTrue(singleResult.isUcs2Unicode)
        assertEquals(1, singleResult.segmentCount)
        assertEquals(70, singleResult.maxPerSingleSegment)

        val longHindi = "नमस्ते ".repeat(15) // > 70 characters
        val multiResult = segmentCalculator.calculate(longHindi)
        assertTrue(multiResult.isUcs2Unicode)
        assertTrue(multiResult.segmentCount >= 2)
        assertEquals(67, multiResult.maxPerMultipartSegment)
    }

    // 4. Template Personalization & Fallback Safety Tests
    @Test
    fun testTemplateRendererSafeFallbackAndTokenValidation() {
        val template = "Hello {{customer_name}}, welcome to {{business_name}}! Call {{business_phone}}."
        val unknown = renderer.validateTemplateTokens("Hello {{customer_name}}, secret {{unsupported_token}}")
        assertEquals(listOf("{{unsupported_token}}"), unknown)

        val customerWithoutName = CustomerEntity(
            fullName = "",
            rawPhoneNumber = "9876543210",
            normalizedPhoneE164 = "+919876543210"
        )
        val profile = BusinessProfileEntity(
            businessName = "Apex Services",
            ownerDisplayName = "Rajesh",
            businessCategory = "Service",
            communicationPhone = "+919876543210",
            defaultMessageSignature = "- Apex Services"
        )

        val rendered = renderer.render(
            rawTemplate = template,
            customer = customerWithoutName,
            profile = profile,
            appendSignature = true
        )

        assertTrue(rendered.contains("Hello Valued Customer"))
        assertTrue(rendered.contains("welcome to Apex Services!"))
        assertTrue(rendered.contains("Call +919876543210."))
        assertTrue(rendered.contains("- Apex Services"))
        assertFalse(rendered.contains("{{"))
    }

    // 5. State Machine Transition Integrity Tests
    @Test
    fun testStateMachineTransitionsSubmittedNeverAutoDelivered() {
        // SUBMITTED can transition to SENT, FAILED, or UNKNOWN
        assertTrue(stateMachine.canTransitionRecipient(RecipientDeliveryState.SUBMITTED, RecipientDeliveryState.SENT))
        assertTrue(stateMachine.canTransitionRecipient(RecipientDeliveryState.SUBMITTED, RecipientDeliveryState.FAILED))
        assertTrue(stateMachine.canTransitionRecipient(RecipientDeliveryState.SUBMITTED, RecipientDeliveryState.UNKNOWN))

        // SUBMITTED can NEVER directly transition to DELIVERED without SENT
        assertFalse(stateMachine.canTransitionRecipient(RecipientDeliveryState.SUBMITTED, RecipientDeliveryState.DELIVERED))
    }

    @Test
    fun testStateMachinePreventsBlindRetryFromUnknown() {
        // UNKNOWN state is a terminal/manual state to prevent duplicate SMS
        assertFalse(stateMachine.canTransitionRecipient(RecipientDeliveryState.UNKNOWN, RecipientDeliveryState.QUEUED))
        assertFalse(stateMachine.canTransitionRecipient(RecipientDeliveryState.UNKNOWN, RecipientDeliveryState.SUBMITTED))
    }

    @Test
    fun testStateMachineSuppressedOptOutIsPermanent() {
        // SUPPRESSED_OPT_OUT cannot transition to any active sending state
        assertFalse(stateMachine.canTransitionRecipient(RecipientDeliveryState.SUPPRESSED_OPT_OUT, RecipientDeliveryState.QUEUED))
        assertFalse(stateMachine.canTransitionRecipient(RecipientDeliveryState.SUPPRESSED_OPT_OUT, RecipientDeliveryState.SUBMITTED))
        assertFalse(stateMachine.canTransitionRecipient(RecipientDeliveryState.SUPPRESSED_OPT_OUT, RecipientDeliveryState.VALIDATED))
    }

    // 6. India TRAI TCCCPR & Quota Advisory Tests
    @Test
    fun testTraiAdvisorEnforces100SmsCapWarning() {
        val advisoryUnder100 = traiAdvisor.evaluateCampaign(
            recipientCount = 45,
            isPromotionalCategory = false,
            countryCode = "+91"
        )
        assertFalse(advisoryUnder100.exceedsDailyPersonalSimCap)

        val advisoryOver100 = traiAdvisor.evaluateCampaign(
            recipientCount = 120,
            isPromotionalCategory = false,
            countryCode = "+91"
        )
        assertTrue(advisoryOver100.exceedsDailyPersonalSimCap)
        assertTrue(advisoryOver100.guidanceBullets.any { it.contains("100 SMS/day") })
    }

    @Test
    fun testTraiAdvisorPromotionalWindowCheck() {
        // 11:00 PM IST is outside 10:00 AM - 9:00 PM IST
        val lateNight = LocalTime.of(23, 0)
        val advisoryLate = traiAdvisor.evaluateCampaign(
            recipientCount = 20,
            isPromotionalCategory = true,
            countryCode = "+91",
            currentTime = lateNight
        )
        assertTrue(advisoryLate.isOutsidePromotionalWindow)

        // 2:00 PM IST is inside 10:00 AM - 9:00 PM IST
        val afternoon = LocalTime.of(14, 0)
        val advisoryAfternoon = traiAdvisor.evaluateCampaign(
            recipientCount = 20,
            isPromotionalCategory = true,
            countryCode = "+91",
            currentTime = afternoon
        )
        assertFalse(advisoryAfternoon.isOutsidePromotionalWindow)
    }
}
