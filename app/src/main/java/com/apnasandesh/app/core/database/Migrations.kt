package com.apnasandesh.app.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Forward-compatible migration hook preserving customer, consent, and suppression tables
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_campaign_recipients_state` ON `campaign_recipients` (`state`)"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Customers table new personalization & order fields
            db.execSQL("ALTER TABLE `customers` ADD COLUMN `email` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `customers` ADD COLUMN `company` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `customers` ADD COLUMN `customField1` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `customers` ADD COLUMN `customField2` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `customers` ADD COLUMN `lastOrderId` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `customers` ADD COLUMN `lastOrderAmount` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `customers` ADD COLUMN `lastOrderDate` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `customers` ADD COLUMN `lastAppointmentDate` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `customers` ADD COLUMN `lastAppointmentTime` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `customers` ADD COLUMN `lastServiceName` TEXT NOT NULL DEFAULT ''")

            // Business profiles table new branding & regional fields
            db.execSQL("ALTER TABLE `business_profiles` ADD COLUMN `website` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `business_profiles` ADD COLUMN `currencySymbol` TEXT NOT NULL DEFAULT '₹'")
            db.execSQL("ALTER TABLE `business_profiles` ADD COLUMN `preferredLanguage` TEXT NOT NULL DEFAULT 'English'")
            db.execSQL("ALTER TABLE `business_profiles` ADD COLUMN `brandTone` TEXT NOT NULL DEFAULT 'Professional'")

            // Message templates table metadata, language, tone, industry, and favorite status
            db.execSQL("ALTER TABLE `message_templates` ADD COLUMN `description` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `message_templates` ADD COLUMN `language` TEXT NOT NULL DEFAULT 'English'")
            db.execSQL("ALTER TABLE `message_templates` ADD COLUMN `tone` TEXT NOT NULL DEFAULT 'Professional'")
            db.execSQL("ALTER TABLE `message_templates` ADD COLUMN `isFavorite` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `message_templates` ADD COLUMN `industry` TEXT NOT NULL DEFAULT 'General Business'")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_templates_isFavorite` ON `message_templates` (`isFavorite`)")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `campaigns` ADD COLUMN `sendingIntervalSeconds` INTEGER NOT NULL DEFAULT 3")
            db.execSQL("ALTER TABLE `campaigns` ADD COLUMN `scheduledAtMillis` INTEGER DEFAULT NULL")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `business_profiles` ADD COLUMN `upiId` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `business_profiles` ADD COLUMN `googleReviewLink` TEXT NOT NULL DEFAULT ''")
        }
    }
}


