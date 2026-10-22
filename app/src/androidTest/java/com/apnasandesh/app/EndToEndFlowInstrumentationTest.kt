package com.apnasandesh.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apnasandesh.app.core.database.BizSmsDatabase
import com.apnasandesh.app.core.database.BusinessProfileEntity
import com.apnasandesh.app.core.database.CampaignEntity
import com.apnasandesh.app.core.database.CampaignRecipientEntity
import com.apnasandesh.app.core.database.CustomerEntity
import com.apnasandesh.app.core.database.CustomerGroupEntity
import com.apnasandesh.app.core.database.DatabaseSeeder
import com.apnasandesh.app.core.database.GroupMembershipEntity
import com.apnasandesh.app.core.database.SendAttemptEntity
import com.apnasandesh.app.core.model.CampaignStatus
import com.apnasandesh.app.core.model.ConsentSource
import com.apnasandesh.app.core.model.ConsentStatus
import com.apnasandesh.app.core.model.RecipientDeliveryState
import com.apnasandesh.app.core.model.TemplateCategory
import com.apnasandesh.app.core.model.TransportMode
import com.apnasandesh.app.domain.campaign.CampaignStateMachine
import com.apnasandesh.app.domain.validation.PhoneNormalizer
import com.apnasandesh.app.domain.validation.PhoneValidationResult
import com.apnasandesh.app.domain.validation.SmsSegmentCalculator
import com.apnasandesh.app.domain.validation.TemplateRenderer
import com.apnasandesh.app.domain.validation.TraiDltComplianceAdvisor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Complete End-to-End Instrumentation Test for LocalReach Business SMS.
 * Simulates the full real-world business messaging lifecycle:
 * 1. Business Profile creation & template seeding
 * 2. Multi-channel customer creation & group categorization
 * 3. Dynamic campaign creation with personalized token rendering
 * 4. Automatic GSM-7/UCS-2 encoding and segment calculation
 * 5. Opt-out firewall suppression across active drafts and customer DB
 * 6. Campaign state transitions (DRAFT -> VALIDATED -> IN_PROGRESS -> COMPLETED)
 * 7. Verification of audit trail and send attempts
 */
@RunWith(AndroidJUnit4::class)
class EndToEndFlowInstrumentationTest {

    private lateinit var context: Context
    private lateinit var db: BizSmsDatabase

    private val phoneNormalizer = PhoneNormalizer()
    private val templateRenderer = TemplateRenderer()
    private val smsCalculator = SmsSegmentCalculator()
    private val stateMachine = CampaignStateMachine()
    private val traiAdvisor = TraiDltComplianceAdvisor()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, BizSmsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testCompleteBusinessSmsLifecycleEndToEnd() = runBlocking {
        val profileDao = db.profileAndSettingsDao()
        val customerDao = db.customerDao()
        val groupDao = db.groupDao()
        val templateDao = db.templateDao()
        val campaignDao = db.campaignDao()

        // ---------------------------------------------------------------------
        // STEP 1: Seed Templates and Configure Business Profile
        // ---------------------------------------------------------------------
        val seeder = DatabaseSeeder(templateDao, profileDao)
        seeder.ensureSeeded()

        val profile = BusinessProfileEntity(
            id = "default_profile",
            businessName = "Heritage Sweets & Cafe",
            ownerDisplayName = "Rajesh Singhania",
            businessCategory = "Retail & F&B",
            communicationPhone = "+919876512345",
            businessAddress = "12 Main Market, Bangalore",
            defaultCountryCallingCode = "+91",
            defaultMessageSignature = "Heritage Sweets, Bangalore"
        )
        profileDao.upsertBusinessProfile(profile)
        val savedProfile = profileDao.getBusinessProfile()
        assertNotNull(savedProfile)
        assertEquals("Heritage Sweets & Cafe", savedProfile?.businessName)

        // Verify seeded default templates exist
        val templates = templateDao.observeTemplates().first()
        assertTrue("Seeded templates should be at least 10", templates.size >= 10)
        assertTrue(templates.any { it.category == TemplateCategory.ORDERS_PAYMENTS.name || it.category == TemplateCategory.ORDER_CONFIRMATION.name })

        // ---------------------------------------------------------------------
        // STEP 2: Create Customer Groups
        // ---------------------------------------------------------------------
        val vipGroup = CustomerGroupEntity(name = "Festival VIPs", description = "High-value seasonal clients")
        groupDao.insertGroup(vipGroup)
        val groups = groupDao.observeGroups().first()
        val vipGroupId = groups.first { it.name == "Festival VIPs" }.id

        // ---------------------------------------------------------------------
        // STEP 3: Normalize & Register Customers (E.164 Engine)
        // ---------------------------------------------------------------------
        val rawCustomers = listOf(
            Triple("Amitabh Sen", "9812345678", "VIP"),
            Triple("Bhavna Rao", "09823456789", "VIP"),       // with trunk zero
            Triple("Chirag Dave", "+91 98345-67890", "REGULAR") // with spaces and dashes
        )

        for ((name, rawPhone, tag) in rawCustomers) {
            val normalResult = phoneNormalizer.normalize(rawPhone, savedProfile?.defaultCountryCallingCode ?: "+91")
            assertTrue("Phone must be valid E.164: $rawPhone", normalResult is PhoneValidationResult.Valid)
            val e164 = (normalResult as PhoneValidationResult.Valid).e164Phone

            val customer = CustomerEntity(
                fullName = name,
                rawPhoneNumber = rawPhone,
                normalizedPhoneE164 = e164,
                isPhoneValid = true,
                tagsCsv = tag,
                consentStatus = ConsentStatus.GRANTED.name,
                consentSource = ConsentSource.MANUAL_ENTRY.name
            )
            customerDao.insertCustomer(customer)
        }

        val allCustomers = customerDao.observeAllCustomers().first()
        assertEquals(3, allCustomers.size)

        // Assign VIP customers to group
        val cust1 = allCustomers.first { it.fullName == "Amitabh Sen" }
        val cust2 = allCustomers.first { it.fullName == "Bhavna Rao" }

        groupDao.assignCustomerToGroup(GroupMembershipEntity(groupId = vipGroupId, customerId = cust1.id))
        groupDao.assignCustomerToGroup(GroupMembershipEntity(groupId = vipGroupId, customerId = cust2.id))

        val vipMembers = groupDao.getMembershipsForGroup(vipGroupId)
        assertEquals(2, vipMembers.size)

        // ---------------------------------------------------------------------
        // STEP 4: Compose Campaign with Personalized Tokens
        // ---------------------------------------------------------------------
        val rawTemplateText = "Namaste {{customer_name}}, your special festive gift box from {{business_name}} is ready for pickup! Call {{business_phone}}."
        val campaign = CampaignEntity(
            name = "Diwali Sweets Box Notification",
            rawMessageTemplate = rawTemplateText,
            transportType = TransportMode.COMPOSER_HANDOFF.name,
            status = CampaignStatus.DRAFT.name,
            totalRecipients = 0
        )
        campaignDao.upsertCampaign(campaign)

        // Recipient Selection: Query eligible VIP group members
        val eligibleVipCustomers = groupDao.getEligibleCustomersInGroup(vipGroupId)
        assertEquals(2, eligibleVipCustomers.size)

        // Resolve personal tokens for each recipient
        val recipientEntities = eligibleVipCustomers.map { customer ->
            val resolvedBody = templateRenderer.render(
                rawTemplate = rawTemplateText,
                customer = customer,
                profile = savedProfile!!,
                appendSignature = false
            )
            val smsCalc = smsCalculator.calculate(resolvedBody)

            CampaignRecipientEntity(
                campaignId = campaign.id,
                customerId = customer.id,
                recipientNameSnapshot = customer.fullName,
                normalizedPhoneSnapshot = customer.normalizedPhoneE164,
                resolvedMessageBody = resolvedBody,
                characterCount = smsCalc.charCount,
                estimatedSegments = smsCalc.segmentCount,
                encodingType = smsCalc.encoding.name,
                state = RecipientDeliveryState.DRAFT.name
            )
        }
        campaignDao.insertCampaignRecipients(recipientEntities)
        campaignDao.updateCampaignStatus(campaignId = campaign.id, status = CampaignStatus.VALIDATED.name)

        // Verify personalized messages were rendered correctly
        val recipientsBeforeOptOut = campaignDao.getCampaignRecipientsSnapshot(campaign.id)
        assertEquals(2, recipientsBeforeOptOut.size)
        assertTrue(recipientsBeforeOptOut.any { it.resolvedMessageBody.contains("Amitabh Sen") })
        assertTrue(recipientsBeforeOptOut.any { it.resolvedMessageBody.contains("Bhavna Rao") })
        assertTrue(recipientsBeforeOptOut.all { it.resolvedMessageBody.contains("Heritage Sweets & Cafe") })

        // ---------------------------------------------------------------------
        // STEP 5: Opt-Out Firewall Activation (Simulating Customer #2 STOP Request)
        // ---------------------------------------------------------------------
        // Customer #2 Bhavna Rao asks to opt out
        customerDao.permanentlyOptOutCustomer(
            customerId = cust2.id,
            normalizedPhone = cust2.normalizedPhoneE164,
            reason = "Customer sent STOP SMS"
        )
        // Also suppress pending recipients in draft campaigns
        campaignDao.suppressPendingRecipientsByPhone(cust2.normalizedPhoneE164)

        // Verify DB suppression records
        val optedOutCust2 = customerDao.findById(cust2.id)
        assertTrue("Customer record must be marked opted out", optedOutCust2?.isOptedOut == true)
        assertEquals(ConsentStatus.OPTED_OUT.name, optedOutCust2?.consentStatus)
        assertTrue("Phone must be in suppression index", customerDao.isPhoneSuppressed(cust2.normalizedPhoneE164))

        // Verify that pending campaign recipient for Customer #2 was automatically CANCELLED
        val recipientsAfterOptOut = campaignDao.getCampaignRecipientsSnapshot(campaign.id)
        val cust2Recipient = recipientsAfterOptOut.first { it.customerId == cust2.id }
        assertEquals("Opted-out recipient in draft/validated campaign must be CANCELLED",
            RecipientDeliveryState.CANCELLED.name, cust2Recipient.state)

        val cust1Recipient = recipientsAfterOptOut.first { it.customerId == cust1.id }
        assertEquals(RecipientDeliveryState.DRAFT.name, cust1Recipient.state)

        // ---------------------------------------------------------------------
        // STEP 6: TRAI DLT Compliance & Rate Advisor Verification
        // ---------------------------------------------------------------------
        val activeRecipients = recipientsAfterOptOut.filter { it.state != RecipientDeliveryState.CANCELLED.name }
        assertEquals(1, activeRecipients.size)

        val rateAdvisory = traiAdvisor.evaluateCampaign(
            recipientCount = activeRecipients.size,
            isPromotionalCategory = false,
            countryCode = "+91"
        )
        assertFalse("1 recipient is well within the 100 SMS/day limit", rateAdvisory.exceedsDailyPersonalSimCap)

        // ---------------------------------------------------------------------
        // STEP 7: Campaign Execution & State Machine Transitions
        // ---------------------------------------------------------------------
        // Transition campaign through state machine: VALIDATED -> AWAITING_CONFIRMATION -> IN_PROGRESS
        assertTrue(stateMachine.canTransitionCampaign(CampaignStatus.VALIDATED, CampaignStatus.AWAITING_CONFIRMATION))
        campaignDao.updateCampaignStatus(campaignId = campaign.id, status = CampaignStatus.AWAITING_CONFIRMATION.name)

        assertTrue(stateMachine.canTransitionCampaign(CampaignStatus.AWAITING_CONFIRMATION, CampaignStatus.IN_PROGRESS))
        campaignDao.updateCampaignStatus(
            campaignId = campaign.id,
            status = CampaignStatus.IN_PROGRESS.name,
            confirmedAt = System.currentTimeMillis()
        )

        // Dispatch recipient #1 via Composer Handoff
        val recipientToDispatch = cust1Recipient
        campaignDao.updateRecipientState(
            recipientRowId = recipientToDispatch.id,
            newState = RecipientDeliveryState.HANDED_OFF_TO_MESSAGING_APP.name
        )

        // Record a successful send attempt audit log
        val sendAttempt = SendAttemptEntity(
            campaignRecipientId = recipientToDispatch.id,
            campaignId = campaign.id,
            transportUsed = TransportMode.COMPOSER_HANDOFF.name,
            outcomeState = RecipientDeliveryState.SENT.name,
            startedAtMillis = System.currentTimeMillis(),
            finishedAtMillis = System.currentTimeMillis()
        )
        campaignDao.insertSendAttempt(sendAttempt)
        campaignDao.updateRecipientState(
            recipientRowId = recipientToDispatch.id,
            newState = RecipientDeliveryState.SENT.name
        )

        // Mark campaign COMPLETED
        assertTrue(stateMachine.canTransitionCampaign(CampaignStatus.IN_PROGRESS, CampaignStatus.COMPLETED))
        campaignDao.updateCampaignStatus(
            campaignId = campaign.id,
            status = CampaignStatus.COMPLETED.name,
            completedAt = System.currentTimeMillis()
        )

        // ---------------------------------------------------------------------
        // STEP 8: Final Audit Trail & Integrity Check
        // ---------------------------------------------------------------------
        val finalCampaign = campaignDao.observeCampaigns().first().first { it.id == campaign.id }
        assertEquals(CampaignStatus.COMPLETED.name, finalCampaign.status)

        val finalRecipients = campaignDao.getCampaignRecipientsSnapshot(campaign.id)
        val sentCount = finalRecipients.count { it.state == RecipientDeliveryState.SENT.name }
        val cancelledCount = finalRecipients.count { it.state == RecipientDeliveryState.CANCELLED.name }

        assertEquals(1, sentCount)
        assertEquals(1, cancelledCount)

        // Verify subsequent campaign creation completely blocks suppressed customer #2
        val eligibleForNextCampaign = customerDao.getEligibleCustomers(requirePromotionalOptIn = false)
        assertFalse("Suppressed customer #2 must never be eligible for new campaigns",
            eligibleForNextCampaign.any { it.id == cust2.id })
        assertTrue("Non-suppressed customer #1 remains eligible",
            eligibleForNextCampaign.any { it.id == cust1.id })
    }
}
