package com.apnasandesh.app

import com.apnasandesh.app.core.database.BusinessProfileEntity
import com.apnasandesh.app.core.database.CustomerEntity
import com.apnasandesh.app.core.model.CampaignStatus
import com.apnasandesh.app.core.model.RecipientDeliveryState
import com.apnasandesh.app.domain.campaign.CampaignStateMachine
import com.apnasandesh.app.domain.validation.CsvCustomerParser
import com.apnasandesh.app.domain.validation.PhoneNormalizer
import com.apnasandesh.app.domain.validation.PhoneValidationResult
import com.apnasandesh.app.domain.validation.SmsCostOptimizer
import com.apnasandesh.app.domain.validation.SmsSegmentCalculator
import com.apnasandesh.app.domain.validation.TemplateRenderer
import com.apnasandesh.app.domain.validation.TraiDltComplianceAdvisor
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

    @Test
    fun testTemplateRendererUpiAndReviewTokens() {
        val customer = CustomerEntity(
            fullName = "Anil Sharma",
            rawPhoneNumber = "9876543210",
            normalizedPhoneE164 = "+919876543210",
            lastOrderId = "INV990",
            lastOrderAmount = "1500"
        )
        val profileWithUpi = BusinessProfileEntity(
            businessName = "Sharma Kirana",
            ownerDisplayName = "Sharma Ji",
            businessCategory = "Grocery",
            communicationPhone = "+919876543210",
            upiId = "sharma@okhdfcbank",
            googleReviewLink = "https://g.page/r/sharmakirana/review"
        )

        val template = "Dear {{first_name}}, pay bill here: {{upi_pay_link}} and rate us: {{google_review_link}}"
        val rendered = renderer.render(
            rawTemplate = template,
            customer = customer,
            profile = profileWithUpi,
            appendSignature = false
        )

        assertTrue(rendered.contains("Dear Anil"))
        assertTrue(rendered.contains("upi://pay?pa=sharma@okhdfcbank&pn=Sharma+Kirana&am=1500&cu=INR&tn=Invoice_INV990"))
        assertTrue(rendered.contains("https://g.page/r/sharmakirana/review"))
        assertFalse(rendered.contains("{{"))
    }

    @Test
    fun testSmsCostOptimizerCondensesVerboseText() {
        val costOptimizer = SmsCostOptimizer(segmentCalculator)
        val verboseMessage = "Thank you very much for your business! Please visit our store for your appointment and discount. Please contact us at 9876543210."
        val result = costOptimizer.optimize(verboseMessage)

        assertTrue(result.savedCharacters > 0)
        assertTrue(result.optimizedText.contains("Thanks a lot"))
        assertTrue(result.optimizedText.contains("Visit us"))
        assertTrue(result.optimizedText.contains("Appt"))
        assertTrue(result.optimizedText.contains("Disc"))
        assertTrue(result.optimizedText.contains("Call"))
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

    // 7. Dynamic Personalization with First/Last Name and Unicode Tests
    @Test
    fun testDynamicPersonalizationWithFirstNameAndAliases() {
        val template = "Hello {{first_name}}, we have a special offer for you from {{business_name}}. Contact us at {{business_phone}} for details."
        val unknown = renderer.validateTemplateTokens(template)
        assertTrue("Expected 0 unknown tokens, found: $unknown", unknown.isEmpty())

        val customer = CustomerEntity(
            fullName = "Priya Patel",
            rawPhoneNumber = "9876543210",
            normalizedPhoneE164 = "+919876543210"
        )
        val profile = BusinessProfileEntity(
            businessName = "ApnaSandesh Store",
            ownerDisplayName = "Manager",
            businessCategory = "Retail",
            communicationPhone = "+919876500000"
        )

        val rendered = renderer.render(
            rawTemplate = template,
            customer = customer,
            profile = profile,
            appendSignature = false
        )

        assertEquals("Hello Priya, we have a special offer for you from ApnaSandesh Store. Contact us at +919876500000 for details.", rendered)
    }

    @Test
    fun testUnicodeAndHindiMessagePersonalization() {
        val hindiTemplate = "नमस्ते {{first_name}} जी 🙏\n{{business_name}} में आपका स्वागत है। संपर्क: {{business_phone}}"
        val unknown = renderer.validateTemplateTokens(hindiTemplate)
        assertTrue("Expected 0 unknown tokens, found: $unknown", unknown.isEmpty())

        val customer = CustomerEntity(
            fullName = "रोहन शर्मा",
            rawPhoneNumber = "9876543210",
            normalizedPhoneE164 = "+919876543210"
        )
        val profile = BusinessProfileEntity(
            businessName = "टेक्स्टफ्लो",
            ownerDisplayName = "दुकानदार",
            businessCategory = "रिटेल",
            communicationPhone = "+919876500000"
        )

        val rendered = renderer.render(
            rawTemplate = hindiTemplate,
            customer = customer,
            profile = profile,
            appendSignature = false
        )

        assertTrue(rendered.contains("नमस्ते रोहन जी 🙏"))
        assertTrue(rendered.contains("टेक्स्टफ्लो में आपका स्वागत है।"))
        assertTrue(rendered.contains("संपर्क: +919876500000"))
        val calc = segmentCalculator.calculate(rendered)
        assertTrue(calc.isUcs2Unicode)
    }

    // 8. Campaign State Machine Pause, Resume, Stop, and Retry Tests
    @Test
    fun testStateMachineSupportsPauseResumeStopAndRetry() {
        // RUNNING can transition to PAUSED or STOPPED
        assertTrue(stateMachine.canTransitionCampaign(CampaignStatus.RUNNING, CampaignStatus.PAUSED))
        assertTrue(stateMachine.canTransitionCampaign(CampaignStatus.RUNNING, CampaignStatus.STOPPED))
        assertTrue(stateMachine.canTransitionCampaign(CampaignStatus.RUNNING, CampaignStatus.COMPLETED))
        assertTrue(stateMachine.canTransitionCampaign(CampaignStatus.RUNNING, CampaignStatus.COMPLETED_WITH_ERRORS))

        // PAUSED can transition to RUNNING or STOPPED
        assertTrue(stateMachine.canTransitionCampaign(CampaignStatus.PAUSED, CampaignStatus.RUNNING))
        assertTrue(stateMachine.canTransitionCampaign(CampaignStatus.PAUSED, CampaignStatus.STOPPED))

        // STOPPED, COMPLETED_WITH_ERRORS, and FAILED can transition back to QUEUED/RUNNING for retry
        assertTrue(stateMachine.canTransitionCampaign(CampaignStatus.STOPPED, CampaignStatus.QUEUED))
        assertTrue(stateMachine.canTransitionCampaign(CampaignStatus.COMPLETED_WITH_ERRORS, CampaignStatus.QUEUED))
        assertTrue(stateMachine.canTransitionCampaign(CampaignStatus.FAILED, CampaignStatus.QUEUED))
    }

    // 9. Comprehensive Dynamic Variable Categories Test
    @Test
    fun testAllVariableCategoriesResolvedAccurately() {
        val template = """
            Dear {{first_name}} {{last_name}},
            Your company: {{customer_company}}, Phone: {{customer_phone}}, Email: {{customer_email}}.
            From {{business_name}} (Phone: {{business_phone}}, Web: {{business_website}}, Addr: {{business_address}}).
            Order #{{order_id}} for {{product_name}} (Amount: {{invoice_amount}}).
            Status: {{payment_status}}, Due: {{payment_due_date}}, Pay: {{payment_link}}.
            Appt on {{appointment_date}} at {{appointment_time}} with {{service_provider}}.
            Custom info: {{custom_field_1}} / {{custom_field_2}}.
            {{business_signature}}
        """.trimIndent()

        val customer = CustomerEntity(
            fullName = "Ananya Sharma",
            rawPhoneNumber = "+919876543210",
            normalizedPhoneE164 = "+919876543210",
            email = "ananya@example.com",
            company = "Sharma Tech",
            customField1 = "VIP Gold",
            customField2 = "GSTIN29AAAA",
            lastOrderId = "ORD-9901",
            lastOrderAmount = "4500.00",
            lastOrderDate = "2026-10-20",
            lastAppointmentDate = "2026-10-25",
            lastAppointmentTime = "10:30 AM",
            lastServiceName = "Full Audit"
        )

        val profile = BusinessProfileEntity(
            businessName = "ApnaSandesh Systems",
            ownerDisplayName = "Nikhil",
            businessCategory = "Technology",
            communicationPhone = "+919800000000",
            businessAddress = "Brigade Rd, Bangalore",
            website = "https://apnasandesh.trustoryx.digital",
            currencySymbol = "₹",
            defaultMessageSignature = "- Team ApnaSandesh"
        )

        val rendered = renderer.render(
            rawTemplate = template,
            customer = customer,
            profile = profile,
            appendSignature = false
        )

        assertTrue(rendered.contains("Dear Ananya Sharma"))
        assertTrue(rendered.contains("Your company: Sharma Tech"))
        assertTrue(rendered.contains("Phone: +919876543210"))
        assertTrue(rendered.contains("Email: ananya@example.com"))
        assertTrue(rendered.contains("From ApnaSandesh Systems"))
        assertTrue(rendered.contains("Phone: +919800000000"))
        assertTrue(rendered.contains("Web: https://apnasandesh.trustoryx.digital"))
        assertTrue(rendered.contains("Addr: Brigade Rd, Bangalore"))
        assertTrue(rendered.contains("Order #ORD-9901"))
        assertTrue(rendered.contains("4500.00"))
        assertTrue(rendered.contains("Custom info: VIP Gold / GSTIN29AAAA"))
        assertTrue(rendered.contains("- Team ApnaSandesh"))
        assertFalse(rendered.contains("{{"))
    }

    // 10. Unique Messages Generated for Each Customer
    @Test
    fun testUniqueMessagesRenderedPerCustomerWithoutCrosstalk() {
        val template = "Namaste {{first_name}} ji! Your order #{{order_id}} for ₹{{invoice_amount}} is ready at {{business_name}}."

        val customerA = CustomerEntity(
            fullName = "Rahul Verma",
            rawPhoneNumber = "9876511111",
            normalizedPhoneE164 = "+919876511111",
            lastOrderId = "ORD-001",
            lastOrderAmount = "1200.00"
        )
        val customerB = CustomerEntity(
            fullName = "Priya Patel",
            rawPhoneNumber = "9876522222",
            normalizedPhoneE164 = "+919876522222",
            lastOrderId = "ORD-002",
            lastOrderAmount = "3500.00"
        )
        val profile = BusinessProfileEntity(
            businessName = "Sharma Electronics",
            ownerDisplayName = "Sharma Ji",
            businessCategory = "Retail",
            communicationPhone = "+919876500000"
        )

        val messageA = renderer.render(template, customerA, profile, appendSignature = false)
        val messageB = renderer.render(template, customerB, profile, appendSignature = false)

        // Customer A check
        assertTrue(messageA.contains("Rahul"))
        assertTrue(messageA.contains("ORD-001"))
        assertTrue(messageA.contains("1200.00"))
        assertFalse(messageA.contains("Priya"))
        assertFalse(messageA.contains("ORD-002"))

        // Customer B check
        assertTrue(messageB.contains("Priya"))
        assertTrue(messageB.contains("ORD-002"))
        assertTrue(messageB.contains("3500.00"))
        assertFalse(messageB.contains("Rahul"))
        assertFalse(messageB.contains("ORD-001"))
    }

    // 11. Safe Fallbacks for Incomplete Customer and Business Profiles
    @Test
    fun testSafeFallbacksForMissingInformation() {
        val template = "Hello {{first_name}}, visit {{business_website}} or call {{business_phone}} for {{product_name}}."
        val emptyCustomer = CustomerEntity(
            fullName = "",
            rawPhoneNumber = "9876500000",
            normalizedPhoneE164 = "+919876500000"
        )
        val emptyProfile = BusinessProfileEntity(
            businessName = "Local Shop",
            ownerDisplayName = "Owner",
            businessCategory = "Retail",
            communicationPhone = ""
        )

        val rendered = renderer.render(template, emptyCustomer, emptyProfile, appendSignature = false)
        assertTrue(rendered.contains("Hello Valued Customer"))
        assertFalse(rendered.contains("{{"))
    }

    // 12. GSM-7 vs Unicode Detection & Segment Calculation
    @Test
    fun testSmsSegmentCalculationGsmVsUnicode() {
        // Plain ASCII
        val gsmText = "Special offer for our loyal customer! Get 20% discount on all purchases today only."
        assertTrue(segmentCalculator.isGsm7Compliant(gsmText))
        val gsmCalc = segmentCalculator.calculate(gsmText)
        assertEquals(com.apnasandesh.app.core.model.SmsEncoding.GSM_7BIT, gsmCalc.encoding)
        assertFalse(gsmCalc.isUcs2Unicode)
        assertEquals(1, gsmCalc.segmentCount)

        // Hindi Text (Unicode UCS-2)
        val hindiText = "आपके पसंदीदा स्टोर पर 20% की विशेष छूट! आज ही विजिट करें।"
        assertFalse(segmentCalculator.isGsm7Compliant(hindiText))
        val nonGsm = segmentCalculator.findNonGsmCharacters(hindiText)
        assertTrue(nonGsm.isNotEmpty())
        val ucs2Calc = segmentCalculator.calculate(hindiText)
        assertEquals(com.apnasandesh.app.core.model.SmsEncoding.UCS_2, ucs2Calc.encoding)
        assertTrue(ucs2Calc.isUcs2Unicode)

        // Emoji causes UCS-2
        val emojiText = "Hello! 🎉 Don't miss out."
        assertFalse(segmentCalculator.isGsm7Compliant(emojiText))
        val emojiCalc = segmentCalculator.calculate(emojiText)
        assertEquals(com.apnasandesh.app.core.model.SmsEncoding.UCS_2, emojiCalc.encoding)
        assertTrue(emojiCalc.isUcs2Unicode)
    }
}

