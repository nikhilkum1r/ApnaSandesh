package com.localreach.bizsms.data.customer

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.localreach.bizsms.core.common.DispatcherProvider
import com.localreach.bizsms.core.database.CampaignDao
import com.localreach.bizsms.core.database.CustomerDao
import com.localreach.bizsms.core.database.CustomerEntity
import com.localreach.bizsms.core.database.CustomerGroupEntity
import com.localreach.bizsms.core.database.GroupDao
import com.localreach.bizsms.core.database.GroupMembershipEntity
import com.localreach.bizsms.core.database.SuppressionEntryEntity
import com.localreach.bizsms.domain.validation.CsvCustomerParser
import com.localreach.bizsms.domain.validation.CsvImportSummary
import com.localreach.bizsms.domain.validation.PhoneNormalizer
import com.localreach.bizsms.domain.validation.PhoneValidationResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val customerDao: CustomerDao,
    private val groupDao: GroupDao,
    private val campaignDao: CampaignDao,
    private val phoneNormalizer: PhoneNormalizer,
    private val csvCustomerParser: CsvCustomerParser,
    private val dispatchers: DispatcherProvider
) {
    fun observeCustomers(): Flow<List<CustomerEntity>> = customerDao.observeAllCustomers()
    fun observeGroups(): Flow<List<CustomerGroupEntity>> = groupDao.observeGroups()
    fun observeSuppressedNumbers(): Flow<List<SuppressionEntryEntity>> = customerDao.observeSuppressionList()

    suspend fun addOrUpdateCustomer(
        existingId: String?,
        fullName: String,
        rawPhone: String,
        defaultCountryCode: String,
        tagsCsv: String,
        notes: String,
        consentStatus: String,
        consentSource: String
    ): Result<CustomerEntity> = withContext(dispatchers.io) {
        if (fullName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Customer name cannot be blank."))
        }
        when (val normalized = phoneNormalizer.normalize(rawPhone, defaultCountryCode)) {
            is PhoneValidationResult.Invalid -> {
                Result.failure(IllegalArgumentException(normalized.reason))
            }
            is PhoneValidationResult.Valid -> {
                val phone = normalized.e164Phone
                if (customerDao.isPhoneSuppressed(phone) && consentStatus != "OPTED_OUT") {
                    return@withContext Result.failure(
                        IllegalStateException("This phone number is on the permanent Opt-Out Suppression List.")
                    )
                }

                // Check for duplicate normalized phone with another customer
                val existingCustomerWithPhone = customerDao.findByNormalizedPhone(phone)
                if (existingCustomerWithPhone != null && (existingId == null || existingCustomerWithPhone.id != existingId)) {
                    return@withContext Result.failure(
                        IllegalArgumentException("A customer with phone number $phone already exists (${existingCustomerWithPhone.fullName}).")
                    )
                }

                val entity = CustomerEntity(
                    id = existingId ?: UUID.randomUUID().toString(),
                    fullName = fullName.trim(),
                    rawPhoneNumber = rawPhone.trim(),
                    normalizedPhoneE164 = phone,
                    isPhoneValid = true,
                    tagsCsv = tagsCsv.trim(),
                    notes = notes.trim(),
                    consentStatus = consentStatus,
                    consentSource = consentSource,
                    consentTimestampMillis = System.currentTimeMillis(),
                    isOptedOut = consentStatus == "OPTED_OUT",
                    optedOutAtMillis = if (consentStatus == "OPTED_OUT") System.currentTimeMillis() else null
                )

                customerDao.upsertCustomerWithConsentAudit(
                    customer = entity,
                    auditNote = "Manual customer save with status $consentStatus via $consentSource"
                )

                if (consentStatus == "OPTED_OUT") {
                    recordOptOut(entity.id, entity.normalizedPhoneE164, "Marked Opted-Out in Customer Directory")
                }

                Result.success(entity)
            }
        }
    }

    suspend fun recordOptOut(customerId: String, normalizedPhone: String, reason: String) =
        withContext(dispatchers.io) {
            customerDao.permanentlyOptOutCustomer(customerId, normalizedPhone, reason)
            campaignDao.suppressPendingRecipientsByPhone(normalizedPhone)
        }

    suspend fun deleteCustomer(customerId: String) = withContext(dispatchers.io) {
        customerDao.deleteCustomerById(customerId)
    }

    suspend fun createGroup(name: String, description: String) = withContext(dispatchers.io) {
        if (name.isNotBlank()) {
            groupDao.insertGroup(CustomerGroupEntity(name = name.trim(), description = description.trim()))
        }
    }

    suspend fun updateGroup(id: String, name: String, description: String) = withContext(dispatchers.io) {
        groupDao.updateGroup(CustomerGroupEntity(id = id, name = name.trim(), description = description.trim()))
    }

    suspend fun deleteGroup(groupId: String) = withContext(dispatchers.io) {
        groupDao.deleteGroup(groupId)
    }

    suspend fun assignToGroup(groupId: String, customerId: String) = withContext(dispatchers.io) {
        groupDao.assignCustomerToGroup(GroupMembershipEntity(groupId = groupId, customerId = customerId))
    }

    suspend fun removeFromGroup(groupId: String, customerId: String) = withContext(dispatchers.io) {
        groupDao.removeCustomerFromGroup(groupId, customerId)
    }

    suspend fun getMembershipsForGroup(groupId: String): List<GroupMembershipEntity> = withContext(dispatchers.io) {
        groupDao.getMembershipsForGroup(groupId)
    }

    /**
     * Resolves a contact selected via the zero-permission Android system contact picker.
     */
    suspend fun importFromSystemContactUri(
        contactUri: Uri,
        defaultCountryCode: String
    ): Result<CustomerEntity> = withContext(dispatchers.io) {
        val resolver = context.contentResolver
        var contactId: String? = null
        var displayName = ""

        resolver.query(
            contactUri,
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                contactId = cursor.getString(0)
                displayName = cursor.getString(1).orEmpty()
            }
        }

        if (contactId == null) {
            return@withContext Result.failure(IllegalStateException("Could not read selected contact."))
        }

        var rawPhone: String? = null
        resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )?.use { phoneCursor ->
            if (phoneCursor.moveToFirst()) {
                rawPhone = phoneCursor.getString(0)
            }
        }

        if (rawPhone.isNullOrBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Selected contact has no associated phone number.")
            )
        }

        addOrUpdateCustomer(
            existingId = null,
            fullName = displayName.ifBlank { "Imported Contact" },
            rawPhone = rawPhone!!,
            defaultCountryCode = defaultCountryCode,
            tagsCsv = "contact-picker",
            notes = "Imported via system contact picker",
            consentStatus = "UNKNOWN",
            consentSource = "CONTACT_PICKER"
        )
    }

    suspend fun importCustomersFromCsvUri(
        uri: Uri,
        defaultCountryCode: String
    ): Result<CsvImportSummary> = withContext(dispatchers.io) {
        runCatching {
            val suppressed = customerDao.observeSuppressionList().first().map { it.normalizedPhoneE164 }.toSet()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val summary = csvCustomerParser.parseCsvStream(stream, defaultCountryCode, suppressed)
                summary.validCustomers.forEach { customer ->
                    customerDao.upsertCustomerWithConsentAudit(
                        customer = customer,
                        auditNote = "Imported from CSV file"
                    )
                }
                summary
            } ?: error("Unable to open selected CSV file")
        }
    }

    suspend fun exportCustomersToCsvUri(uri: Uri): Result<Int> = withContext(dispatchers.io) {
        runCatching {
            val all = customerDao.observeAllCustomers().first()
            context.contentResolver.openOutputStream(uri)?.use { out ->
                OutputStreamWriter(out, Charsets.UTF_8).use { writer ->
                    writer.write("Name,Phone_E164,Tags,Consent_Status,Consent_Source,Notes\n")
                    all.forEach { c ->
                        writer.write(
                            "\"${c.fullName.replace("\"", "\"\"")}\"," +
                                "\"${c.normalizedPhoneE164}\"," +
                                "\"${c.tagsCsv.replace("\"", "\"\"")}\"," +
                                "\"${c.consentStatus}\"," +
                                "\"${c.consentSource}\"," +
                                "\"${c.notes.replace("\"", "\"\"")}\"\n"
                        )
                    }
                }
            }
            all.size
        }
    }
}
