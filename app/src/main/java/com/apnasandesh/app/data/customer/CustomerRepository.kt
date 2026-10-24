package com.apnasandesh.app.data.customer

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.apnasandesh.app.core.common.DispatcherProvider
import com.apnasandesh.app.core.database.CampaignDao
import com.apnasandesh.app.core.database.CustomerDao
import com.apnasandesh.app.core.database.CustomerEntity
import com.apnasandesh.app.core.database.CustomerGroupEntity
import com.apnasandesh.app.core.database.GroupDao
import com.apnasandesh.app.core.database.GroupMembershipEntity
import com.apnasandesh.app.core.database.SuppressionEntryEntity
import com.apnasandesh.app.domain.validation.CsvCustomerParser
import com.apnasandesh.app.domain.validation.CsvImportSummary
import com.apnasandesh.app.domain.validation.PhoneNormalizer
import com.apnasandesh.app.domain.validation.PhoneValidationResult
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
        consentSource: String,
        email: String = "",
        company: String = "",
        customField1: String = "",
        customField2: String = "",
        lastOrderId: String = "",
        lastOrderAmount: String = "",
        lastOrderDate: String = "",
        lastAppointmentDate: String = "",
        lastAppointmentTime: String = "",
        lastServiceName: String = ""
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

                val existing = if (existingId != null) customerDao.findById(existingId) else null

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
                    optedOutAtMillis = if (consentStatus == "OPTED_OUT") System.currentTimeMillis() else null,
                    email = email.ifBlank { existing?.email ?: "" },
                    company = company.ifBlank { existing?.company ?: "" },
                    customField1 = customField1.ifBlank { existing?.customField1 ?: "" },
                    customField2 = customField2.ifBlank { existing?.customField2 ?: "" },
                    lastOrderId = lastOrderId.ifBlank { existing?.lastOrderId ?: "" },
                    lastOrderAmount = lastOrderAmount.ifBlank { existing?.lastOrderAmount ?: "" },
                    lastOrderDate = lastOrderDate.ifBlank { existing?.lastOrderDate ?: "" },
                    lastAppointmentDate = lastAppointmentDate.ifBlank { existing?.lastAppointmentDate ?: "" },
                    lastAppointmentTime = lastAppointmentTime.ifBlank { existing?.lastAppointmentTime ?: "" },
                    lastServiceName = lastServiceName.ifBlank { existing?.lastServiceName ?: "" }
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
        customerDao.deleteCustomerWithRelations(customerId)
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

    data class DeviceContactItem(
        val id: String,
        val displayName: String,
        val phoneNumber: String,
        val photoUri: String? = null
    )

    suspend fun readDeviceContacts(): List<DeviceContactItem> = withContext(dispatchers.io) {
        val list = mutableListOf<DeviceContactItem>()
        val seenPhones = mutableSetOf<String>()
        val resolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            ),
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} COLLATE NOCASE ASC"
        )
        cursor?.use {
            val idCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
            val numCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            while (it.moveToNext()) {
                val id = if (idCol != -1) it.getString(idCol) ?: "" else ""
                val name = if (nameCol != -1) it.getString(nameCol) ?: "Contact" else "Contact"
                val num = if (numCol != -1) it.getString(numCol) ?: "" else ""
                val photo = if (photoCol != -1) it.getString(photoCol) else null
                val cleanNum = num.replace("\\s+".toRegex(), "")
                if (cleanNum.isNotBlank() && seenPhones.add(cleanNum)) {
                    list.add(DeviceContactItem(id, name, num, photo))
                }
            }
        }
        list
    }

    suspend fun importSelectedDeviceContacts(
        contacts: List<DeviceContactItem>,
        defaultCountryCode: String,
        tagsCsv: String = "device-contacts"
    ): Int = withContext(dispatchers.io) {
        var count = 0
        contacts.forEach { contact ->
            val res = addOrUpdateCustomer(
                existingId = null,
                fullName = contact.displayName.ifBlank { "Imported Contact" },
                rawPhone = contact.phoneNumber,
                defaultCountryCode = defaultCountryCode,
                tagsCsv = tagsCsv,
                notes = "Imported from device contacts",
                consentStatus = "UNKNOWN",
                consentSource = "DEVICE_CONTACTS"
            )
            if (res.isSuccess) count++
        }
        count
    }

    suspend fun exportCustomersToCsvUri(uri: Uri): Result<Int> = withContext(dispatchers.io) {
        runCatching {
            val all = customerDao.observeAllCustomers().first()
            context.contentResolver.openOutputStream(uri)?.use { out ->
                OutputStreamWriter(out, Charsets.UTF_8).use { writer ->
                    writer.write("Name,Phone_E164,Tags,Consent_Status,Consent_Source,Notes,Email,Company,Custom_Field_1,Custom_Field_2,Last_Order_Id,Last_Order_Amount,Last_Order_Date\n")
                    all.forEach { c ->
                        writer.write(
                            "\"${c.fullName.replace("\"", "\"\"")}\"," +
                                "\"${c.normalizedPhoneE164}\"," +
                                "\"${c.tagsCsv.replace("\"", "\"\"")}\"," +
                                "\"${c.consentStatus}\"," +
                                "\"${c.consentSource}\"," +
                                "\"${c.notes.replace("\"", "\"\"")}\"," +
                                "\"${c.email.replace("\"", "\"\"")}\"," +
                                "\"${c.company.replace("\"", "\"\"")}\"," +
                                "\"${c.customField1.replace("\"", "\"\"")}\"," +
                                "\"${c.customField2.replace("\"", "\"\"")}\"," +
                                "\"${c.lastOrderId.replace("\"", "\"\"")}\"," +
                                "\"${c.lastOrderAmount.replace("\"", "\"\"")}\"," +
                                "\"${c.lastOrderDate.replace("\"", "\"\"")}\"\n"
                        )
                    }
                }
            }
            all.size
        }
    }
}
