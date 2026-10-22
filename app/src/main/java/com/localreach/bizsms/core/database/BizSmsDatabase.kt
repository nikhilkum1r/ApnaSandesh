package com.localreach.bizsms.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        BusinessProfileEntity::class,
        CustomerEntity::class,
        CustomerGroupEntity::class,
        GroupMembershipEntity::class,
        MessageTemplateEntity::class,
        CampaignEntity::class,
        CampaignRecipientEntity::class,
        SendAttemptEntity::class,
        ConsentRecordEntity::class,
        SuppressionEntryEntity::class,
        AppSettingsEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class BizSmsDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun groupDao(): GroupDao
    abstract fun templateDao(): TemplateDao
    abstract fun campaignDao(): CampaignDao
    abstract fun profileAndSettingsDao(): ProfileAndSettingsDao

    suspend fun wipeAllLocalData() {
        clearAllTables()
    }
}
