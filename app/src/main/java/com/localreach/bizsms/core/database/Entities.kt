package com.localreach.bizsms.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "business_profiles")
data class BusinessProfileEntity(
    @PrimaryKey val id: String = "default_profile",
    val businessName: String,
    val ownerDisplayName: String,
    val businessCategory: String,
    val communicationPhone: String,
    val businessAddress: String? = null,
    val defaultCountryCallingCode: String = "+91",
    val defaultMessageSignature: String = "",
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["normalizedPhoneE164"], unique = true),
        Index(value = ["isOptedOut"]),
        Index(value = ["fullName"])
    ]
)
data class CustomerEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val fullName: String,
    val rawPhoneNumber: String,
    val normalizedPhoneE164: String,
    val isPhoneValid: Boolean = true,
    val tagsCsv: String = "",
    val consentStatus: String = "UNKNOWN",
    val consentSource: String = "MANUAL_ENTRY",
    val consentTimestampMillis: Long = System.currentTimeMillis(),
    val isOptedOut: Boolean = false,
    val optedOutAtMillis: Long? = null,
    val notes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "customer_groups",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class CustomerGroupEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "group_memberships",
    primaryKeys = ["groupId", "customerId"],
    foreignKeys = [
        ForeignKey(
            entity = CustomerGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["groupId"]),
        Index(value = ["customerId"])
    ]
)
data class GroupMembershipEntity(
    val groupId: String,
    val customerId: String,
    val addedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "message_templates",
    indices = [
        Index(value = ["category"])
    ]
)
data class MessageTemplateEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val bodyTemplate: String,
    val isPromotional: Boolean = false,
    val isSystemDefault: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "campaigns",
    foreignKeys = [
        ForeignKey(
            entity = MessageTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["templateId"]),
        Index(value = ["status"]),
        Index(value = ["createdAtMillis"])
    ]
)
data class CampaignEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val templateId: String? = null,
    val rawMessageTemplate: String,
    val transportType: String = "COMPOSER_HANDOFF",
    val selectedSubscriptionId: Int? = null,
    val status: String = "DRAFT",
    val totalRecipients: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val confirmedAtMillis: Long? = null,
    val completedAtMillis: Long? = null
)

@Entity(
    tableName = "campaign_recipients",
    foreignKeys = [
        ForeignKey(
            entity = CampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["campaignId", "customerId"], unique = true),
        Index(value = ["campaignId"]),
        Index(value = ["customerId"]),
        Index(value = ["state"])
    ]
)
data class CampaignRecipientEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val campaignId: String,
    val customerId: String,
    val recipientNameSnapshot: String,
    val normalizedPhoneSnapshot: String,
    val resolvedMessageBody: String,
    val characterCount: Int = 0,
    val estimatedSegments: Int = 1,
    val encodingType: String = "GSM_7BIT",
    val state: String = "DRAFT",
    val lastErrorCode: String? = null,
    val lastErrorMessage: String? = null,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "send_attempts",
    foreignKeys = [
        ForeignKey(
            entity = CampaignRecipientEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignRecipientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["campaignRecipientId"]),
        Index(value = ["campaignId"])
    ]
)
data class SendAttemptEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val campaignRecipientId: String,
    val campaignId: String,
    val transportUsed: String,
    val subscriptionId: Int? = null,
    val outcomeState: String,
    val errorCode: String? = null,
    val errorDetail: String? = null,
    val startedAtMillis: Long = System.currentTimeMillis(),
    val finishedAtMillis: Long? = null
)

@Entity(
    tableName = "consent_records",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["normalizedPhoneE164"])
    ]
)
data class ConsentRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val normalizedPhoneE164: String,
    val previousStatus: String? = null,
    val newStatus: String,
    val source: String,
    val note: String = "",
    val recordedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "suppression_entries",
    indices = [
        Index(value = ["normalizedPhoneE164"], unique = true),
        Index(value = ["customerId"])
    ]
)
data class SuppressionEntryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val normalizedPhoneE164: String,
    val customerId: String? = null,
    val reason: String = "USER_OPT_OUT",
    val suppressedAtMillis: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: String = "app_settings",
    val onboardingCompleted: Boolean = false,
    val preferredTransport: String = "COMPOSER_HANDOFF",
    val directSimPolicyAcknowledged: Boolean = false,
    val directSimFeatureEnabled: Boolean = false,
    val selectedSimSubscriptionId: Int? = null,
    val indiaTraiComplianceMode: Boolean = true,
    val appendSignatureByDefault: Boolean = true,
    val themeMode: String = "SYSTEM",
    val updatedAtMillis: Long = System.currentTimeMillis()
)
