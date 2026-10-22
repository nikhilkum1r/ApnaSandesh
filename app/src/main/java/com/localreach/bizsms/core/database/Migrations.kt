package com.localreach.bizsms.core.database

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
}
