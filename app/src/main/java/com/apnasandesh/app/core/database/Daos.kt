package com.apnasandesh.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT COUNT(*) FROM customers")
    suspend fun countCustomers(): Int

    @Query("SELECT * FROM customers ORDER BY fullName COLLATE NOCASE ASC")
    fun observeAllCustomers(): Flow<List<CustomerEntity>>

    @Query("""
        SELECT c.* FROM customers c
        LEFT JOIN suppression_entries s ON c.normalizedPhoneE164 = s.normalizedPhoneE164 AND s.isActive = 1
        WHERE s.id IS NULL
          AND c.isOptedOut = 0
          AND c.consentStatus != 'OPTED_OUT'
          AND c.isPhoneValid = 1
          AND (:requirePromotionalOptIn = 0 OR c.consentStatus = 'GRANTED')
        ORDER BY c.fullName COLLATE NOCASE ASC
    """)
    suspend fun getEligibleCustomers(requirePromotionalOptIn: Boolean): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE normalizedPhoneE164 = :phone LIMIT 1")
    suspend fun findByNormalizedPhone(phone: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomerById(customerId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsentRecord(record: ConsentRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppressionEntry(entry: SuppressionEntryEntity)

    @Query("SELECT * FROM suppression_entries WHERE isActive = 1 ORDER BY suppressedAtMillis DESC")
    fun observeSuppressionList(): Flow<List<SuppressionEntryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM suppression_entries WHERE normalizedPhoneE164 = :phone AND isActive = 1)")
    suspend fun isPhoneSuppressed(phone: String): Boolean

    @Transaction
    suspend fun upsertCustomerWithConsentAudit(
        customer: CustomerEntity,
        auditNote: String
    ) {
        val existing = findByNormalizedPhone(customer.normalizedPhoneE164)
        val targetCustomer = if (existing == null) {
            insertCustomer(customer)
            customer
        } else {
            val updated = customer.copy(id = existing.id, createdAtMillis = existing.createdAtMillis)
            updateCustomer(updated)
            updated
        }
        insertConsentRecord(
            ConsentRecordEntity(
                customerId = targetCustomer.id,
                normalizedPhoneE164 = targetCustomer.normalizedPhoneE164,
                previousStatus = existing?.consentStatus,
                newStatus = targetCustomer.consentStatus,
                source = targetCustomer.consentSource,
                note = auditNote
            )
        )
    }

    @Transaction
    suspend fun permanentlyOptOutCustomer(
        customerId: String,
        normalizedPhone: String,
        reason: String
    ) {
        val now = System.currentTimeMillis()
        val existing = findById(customerId)
        if (existing != null) {
            updateCustomer(
                existing.copy(
                    isOptedOut = true,
                    consentStatus = "OPTED_OUT",
                    optedOutAtMillis = now,
                    updatedAtMillis = now
                )
            )
            insertConsentRecord(
                ConsentRecordEntity(
                    customerId = existing.id,
                    normalizedPhoneE164 = normalizedPhone,
                    previousStatus = existing.consentStatus,
                    newStatus = "OPTED_OUT",
                    source = existing.consentSource,
                    note = "Permanent Opt-Out: $reason",
                    recordedAtMillis = now
                )
            )
        }
        insertSuppressionEntry(
            SuppressionEntryEntity(
                normalizedPhoneE164 = normalizedPhone,
                customerId = customerId,
                reason = reason,
                suppressedAtMillis = now,
                isActive = true
            )
        )
    }
}

@Dao
interface GroupDao {
    @Query("SELECT * FROM customer_groups ORDER BY name COLLATE NOCASE ASC")
    fun observeGroups(): Flow<List<CustomerGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGroup(group: CustomerGroupEntity)

    @Update
    suspend fun updateGroup(group: CustomerGroupEntity)

    @Query("DELETE FROM customer_groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun assignCustomerToGroup(membership: GroupMembershipEntity)

    @Query("DELETE FROM group_memberships WHERE groupId = :groupId AND customerId = :customerId")
    suspend fun removeCustomerFromGroup(groupId: String, customerId: String)

    @Query("SELECT * FROM group_memberships WHERE customerId = :customerId")
    suspend fun getMembershipsForCustomer(customerId: String): List<GroupMembershipEntity>

    @Query("SELECT * FROM group_memberships WHERE groupId = :groupId")
    suspend fun getMembershipsForGroup(groupId: String): List<GroupMembershipEntity>

    @Query("""
        SELECT c.* FROM customers c
        INNER JOIN group_memberships gm ON c.id = gm.customerId
        LEFT JOIN suppression_entries s ON c.normalizedPhoneE164 = s.normalizedPhoneE164 AND s.isActive = 1
        WHERE gm.groupId = :groupId
          AND s.id IS NULL
          AND c.isOptedOut = 0
          AND c.consentStatus != 'OPTED_OUT'
          AND c.isPhoneValid = 1
    """)
    suspend fun getEligibleCustomersInGroup(groupId: String): List<CustomerEntity>
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM message_templates ORDER BY isFavorite DESC, isSystemDefault DESC, title ASC")
    fun observeTemplates(): Flow<List<MessageTemplateEntity>>

    @Query("SELECT COUNT(*) FROM message_templates")
    suspend fun countTemplates(): Int

    @Query("SELECT * FROM message_templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateById(id: String): MessageTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTemplate(template: MessageTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTemplates(templates: List<MessageTemplateEntity>)

    @Query("DELETE FROM message_templates WHERE id = :id AND isSystemDefault = 0")
    suspend fun deleteCustomTemplate(id: String)

    @Query("UPDATE message_templates SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM message_templates WHERE isSystemDefault = 1")
    suspend fun deleteAllSystemDefaultTemplates()
}

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns ORDER BY createdAtMillis DESC")
    fun observeCampaigns(): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM campaigns WHERE id = :campaignId LIMIT 1")
    suspend fun getCampaignById(campaignId: String): CampaignEntity?

    @Query("SELECT * FROM campaign_recipients WHERE campaignId = :campaignId ORDER BY recipientNameSnapshot ASC")
    fun observeCampaignRecipients(campaignId: String): Flow<List<CampaignRecipientEntity>>

    @Query("SELECT * FROM campaign_recipients WHERE campaignId = :campaignId ORDER BY recipientNameSnapshot ASC")
    suspend fun getCampaignRecipientsSnapshot(campaignId: String): List<CampaignRecipientEntity>

    @Query("SELECT * FROM campaign_recipients WHERE id = :recipientRowId LIMIT 1")
    suspend fun getCampaignRecipientById(recipientRowId: String): CampaignRecipientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCampaign(campaign: CampaignEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCampaignRecipients(recipients: List<CampaignRecipientEntity>)

    @Query("DELETE FROM campaign_recipients WHERE campaignId = :campaignId")
    suspend fun clearDraftRecipients(campaignId: String)

    @Query("DELETE FROM campaigns WHERE id = :campaignId")
    suspend fun deleteCampaignById(campaignId: String)

    @Query("""
        UPDATE campaign_recipients
        SET resolvedMessageBody = :newMessage,
            characterCount = :charCount,
            estimatedSegments = :segments,
            encodingType = :encoding,
            updatedAtMillis = :timestamp
        WHERE id = :recipientId
    """)
    suspend fun updateRecipientResolvedMessage(
        recipientId: String,
        newMessage: String,
        charCount: Int,
        segments: Int,
        encoding: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE campaign_recipients
        SET state = 'CANCELLED',
            lastErrorCode = 'OPT_OUT_SUPPRESSION',
            lastErrorMessage = 'Suppressed due to customer opt-out',
            updatedAtMillis = :timestamp
        WHERE normalizedPhoneSnapshot = :normalizedPhone
          AND state IN ('DRAFT', 'VALIDATED', 'AWAITING_CONFIRMATION', 'QUEUED')
    """)
    suspend fun suppressPendingRecipientsByPhone(normalizedPhone: String, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE campaign_recipients
        SET state = :newState,
            lastErrorCode = :errorCode,
            lastErrorMessage = :errorMessage,
            updatedAtMillis = :timestamp
        WHERE id = :recipientRowId
    """)
    suspend fun updateRecipientState(
        recipientRowId: String,
        newState: String,
        errorCode: String? = null,
        errorMessage: String? = null,
        timestamp: Long = System.currentTimeMillis()
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSendAttempt(attempt: SendAttemptEntity)

    @Query("""
        UPDATE campaigns
        SET status = :status,
            confirmedAtMillis = COALESCE(:confirmedAt, confirmedAtMillis),
            completedAtMillis = COALESCE(:completedAt, completedAtMillis),
            updatedAtMillis = :now
        WHERE id = :campaignId
    """)
    suspend fun updateCampaignStatus(
        campaignId: String,
        status: String,
        confirmedAt: Long? = null,
        completedAt: Long? = null,
        now: Long = System.currentTimeMillis()
    )

    @Transaction
    suspend fun replaceDraftCampaignWithRecipients(
        campaign: CampaignEntity,
        recipients: List<CampaignRecipientEntity>
    ) {
        upsertCampaign(campaign)
        clearDraftRecipients(campaign.id)
        insertCampaignRecipients(recipients)
    }

    @Transaction
    suspend fun recordSendAttemptAndTransitionState(
        recipientRowId: String,
        campaignId: String,
        newState: String,
        transportMode: String,
        subscriptionId: Int?,
        errorCode: String? = null,
        errorMessage: String? = null
    ) {
        val now = System.currentTimeMillis()
        updateRecipientState(
            recipientRowId = recipientRowId,
            newState = newState,
            errorCode = errorCode,
            errorMessage = errorMessage,
            timestamp = now
        )
        insertSendAttempt(
            SendAttemptEntity(
                campaignRecipientId = recipientRowId,
                campaignId = campaignId,
                transportUsed = transportMode,
                subscriptionId = subscriptionId,
                outcomeState = newState,
                errorCode = errorCode,
                errorDetail = errorMessage,
                startedAtMillis = now,
                finishedAtMillis = now
            )
        )
    }

    @Query("""
        UPDATE campaign_recipients
        SET state = 'UNKNOWN',
            lastErrorCode = 'PROCESS_TERMINATED',
            lastErrorMessage = 'App restarted while submission was in-flight; marked UNKNOWN to prevent duplicate send.',
            updatedAtMillis = :timestamp
        WHERE state = 'SUBMITTED' AND updatedAtMillis < :staleCutoffMillis
    """)
    suspend fun reconcileStaleSubmittedRecipients(
        staleCutoffMillis: Long,
        timestamp: Long = System.currentTimeMillis()
    ): Int

    @Query("SELECT * FROM send_attempts WHERE campaignId = :campaignId ORDER BY startedAtMillis DESC")
    fun observeSendAttempts(campaignId: String): Flow<List<SendAttemptEntity>>

    @Query("SELECT * FROM campaign_recipients WHERE campaignId = :campaignId AND state IN ('QUEUED', 'DRAFT', 'VALIDATED', 'AWAITING_CONFIRMATION') ORDER BY recipientNameSnapshot ASC")
    suspend fun getPendingRecipients(campaignId: String): List<CampaignRecipientEntity>

    @Query("SELECT * FROM campaign_recipients WHERE campaignId = :campaignId AND state = 'FAILED' ORDER BY recipientNameSnapshot ASC")
    suspend fun getFailedRecipients(campaignId: String): List<CampaignRecipientEntity>

    @Query("""
        UPDATE campaign_recipients 
        SET state = 'QUEUED', 
            lastErrorCode = NULL, 
            lastErrorMessage = NULL, 
            updatedAtMillis = :timestamp 
        WHERE campaignId = :campaignId AND state = 'FAILED'
    """)
    suspend fun resetFailedRecipientsToQueued(campaignId: String, timestamp: Long = System.currentTimeMillis()): Int

    @Query("""
        UPDATE campaign_recipients
        SET state = 'CANCELLED',
            lastErrorCode = 'CAMPAIGN_STOPPED',
            lastErrorMessage = 'Campaign stopped by user',
            updatedAtMillis = :timestamp
        WHERE campaignId = :campaignId AND state IN ('DRAFT', 'VALIDATED', 'AWAITING_CONFIRMATION', 'QUEUED')
    """)
    suspend fun stopPendingRecipients(campaignId: String, timestamp: Long = System.currentTimeMillis()): Int

    @Query("SELECT * FROM campaigns WHERE status IN ('RUNNING', 'IN_PROGRESS') LIMIT 1")
    suspend fun getActiveRunningCampaign(): CampaignEntity?

    @Query("""
        UPDATE campaigns 
        SET status = 'PAUSED', 
            updatedAtMillis = :now 
        WHERE status IN ('RUNNING', 'IN_PROGRESS')
    """)
    suspend fun reconcileRunningCampaignsToPaused(now: Long = System.currentTimeMillis()): Int
}

@Dao
interface ProfileAndSettingsDao {
    @Query("SELECT * FROM business_profiles WHERE id = 'default_profile' LIMIT 1")
    fun observeBusinessProfile(): Flow<BusinessProfileEntity?>

    @Query("SELECT * FROM business_profiles WHERE id = 'default_profile' LIMIT 1")
    suspend fun getBusinessProfile(): BusinessProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBusinessProfile(profile: BusinessProfileEntity)

    @Query("SELECT * FROM app_settings WHERE id = 'app_settings' LIMIT 1")
    fun observeAppSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 'app_settings' LIMIT 1")
    suspend fun getAppSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppSettings(settings: AppSettingsEntity)
}
