package com.apnasandesh.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apnasandesh.app.core.database.BizSmsDatabase
import com.apnasandesh.app.core.database.CampaignDao
import com.apnasandesh.app.core.database.CampaignEntity
import com.apnasandesh.app.core.database.CampaignRecipientEntity
import com.apnasandesh.app.core.database.CustomerDao
import com.apnasandesh.app.core.database.CustomerEntity
import com.apnasandesh.app.core.database.CustomerGroupEntity
import com.apnasandesh.app.core.database.GroupDao
import com.apnasandesh.app.core.database.GroupMembershipEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomPersistenceInstrumentationTest {

    private lateinit var db: BizSmsDatabase
    private lateinit var customerDao: CustomerDao
    private lateinit var groupDao: GroupDao
    private lateinit var campaignDao: CampaignDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BizSmsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        customerDao = db.customerDao()
        groupDao = db.groupDao()
        campaignDao = db.campaignDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testCustomerInsertAndPhoneUniqueConstraint() = runBlocking {
        val customer1 = CustomerEntity(
            id = UUID.randomUUID().toString(),
            fullName = "Rohan Sharma",
            rawPhoneNumber = "9876543210",
            normalizedPhoneE164 = "+919876543210",
            isPhoneValid = true,
            consentStatus = "GRANTED"
        )
        customerDao.insertCustomer(customer1)

        val retrieved = customerDao.findByNormalizedPhone("+919876543210")
        assertNotNull(retrieved)
        assertEquals("Rohan Sharma", retrieved?.fullName)

        // Attempt duplicate insert with same normalized phone
        val customer2 = CustomerEntity(
            id = UUID.randomUUID().toString(),
            fullName = "Duplicate Rohan",
            rawPhoneNumber = "09876543210",
            normalizedPhoneE164 = "+919876543210",
            isPhoneValid = true
        )
        try {
            customerDao.insertCustomer(customer2)
            fail("Expected SQLiteConstraintException on duplicate phone insert")
        } catch (e: Exception) {
            assertTrue(e is android.database.sqlite.SQLiteConstraintException)
        }
    }

    @Test
    fun testPermanentOptOutAndSuppressionFirewall() = runBlocking {
        val customer = CustomerEntity(
            id = UUID.randomUUID().toString(),
            fullName = "Priya Patel",
            rawPhoneNumber = "9123456789",
            normalizedPhoneE164 = "+919123456789",
            isPhoneValid = true,
            consentStatus = "GRANTED"
        )
        customerDao.insertCustomer(customer)

        var eligible = customerDao.getEligibleCustomers(requirePromotionalOptIn = false)
        assertEquals(1, eligible.size)

        // Execute permanent opt out transaction
        customerDao.permanentlyOptOutCustomer(
            customerId = customer.id,
            normalizedPhone = customer.normalizedPhoneE164,
            reason = "Customer sent STOP via SMS"
        )

        // Verify Customer entity is updated
        val updated = customerDao.findById(customer.id)
        assertTrue(updated?.isOptedOut == true)
        assertEquals("OPTED_OUT", updated?.consentStatus)

        // Verify Suppression list entry exists
        assertTrue(customerDao.isPhoneSuppressed(customer.normalizedPhoneE164))

        // Verify customer is completely excluded from eligible list
        eligible = customerDao.getEligibleCustomers(requirePromotionalOptIn = false)
        assertEquals(0, eligible.size)
    }

    @Test
    fun testCampaignRecipientUniqueConstraintPerCampaign() = runBlocking {
        val customer = CustomerEntity(
            id = UUID.randomUUID().toString(),
            fullName = "Sunil Verma",
            rawPhoneNumber = "9988776655",
            normalizedPhoneE164 = "+919988776655",
            isPhoneValid = true,
            consentStatus = "GRANTED"
        )
        customerDao.insertCustomer(customer)

        val campaign = CampaignEntity(
            id = UUID.randomUUID().toString(),
            name = "Festive Diwali Sale",
            rawMessageTemplate = "Happy Diwali {{customer_name}}!"
        )
        campaignDao.upsertCampaign(campaign)

        val recipient1 = CampaignRecipientEntity(
            id = UUID.randomUUID().toString(),
            campaignId = campaign.id,
            customerId = customer.id,
            recipientNameSnapshot = customer.fullName,
            normalizedPhoneSnapshot = customer.normalizedPhoneE164,
            resolvedMessageBody = "Happy Diwali Sunil Verma!"
        )
        campaignDao.insertCampaignRecipients(listOf(recipient1))

        // On duplicate (campaignId, customerId), IGNORE strategy or unique constraint prevents duplicate rows
        val duplicateRecipient = CampaignRecipientEntity(
            id = UUID.randomUUID().toString(),
            campaignId = campaign.id,
            customerId = customer.id,
            recipientNameSnapshot = customer.fullName,
            normalizedPhoneSnapshot = customer.normalizedPhoneE164,
            resolvedMessageBody = "Happy Diwali Sunil Verma!"
        )
        campaignDao.insertCampaignRecipients(listOf(duplicateRecipient))

        val recipients = campaignDao.getCampaignRecipientsSnapshot(campaign.id)
        assertEquals(1, recipients.size)
    }

    @Test
    fun testCascadeDeletionOnCustomerGroup() = runBlocking {
        val group = CustomerGroupEntity(name = "VIP Clients")
        groupDao.insertGroup(group)

        val retrievedGroup = groupDao.observeGroups().first().first { it.name == "VIP Clients" }

        val customer = CustomerEntity(
            fullName = "Ananya Roy",
            rawPhoneNumber = "9811223344",
            normalizedPhoneE164 = "+919811223344",
            isPhoneValid = true,
            consentStatus = "GRANTED"
        )
        customerDao.insertCustomer(customer)
        val retrievedCust = customerDao.findByNormalizedPhone("+919811223344")!!

        groupDao.assignCustomerToGroup(
            GroupMembershipEntity(groupId = retrievedGroup.id, customerId = retrievedCust.id)
        )

        var memberships = groupDao.getMembershipsForGroup(retrievedGroup.id)
        assertEquals(1, memberships.size)

        // Delete group and verify membership row cascades
        groupDao.deleteGroup(retrievedGroup.id)
        memberships = groupDao.getMembershipsForGroup(retrievedGroup.id)
        assertEquals(0, memberships.size)

        // Customer itself remains intact
        assertNotNull(customerDao.findById(retrievedCust.id))
    }
}
