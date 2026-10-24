package com.apnasandesh.app.di

import android.content.Context
import androidx.room.Room
import com.apnasandesh.app.core.common.DefaultDispatcherProvider
import com.apnasandesh.app.core.common.DispatcherProvider
import com.apnasandesh.app.core.database.BizSmsDatabase
import com.apnasandesh.app.core.database.CampaignDao
import com.apnasandesh.app.core.database.CustomerDao
import com.apnasandesh.app.core.database.DatabaseMigrations
import com.apnasandesh.app.core.database.GroupDao
import com.apnasandesh.app.core.database.ProfileAndSettingsDao
import com.apnasandesh.app.core.database.TemplateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BizSmsDatabase {
        return Room.databaseBuilder(
            context,
            BizSmsDatabase::class.java,
            "biz_sms_database"
        )
            .addMigrations(
                DatabaseMigrations.MIGRATION_1_2,
                DatabaseMigrations.MIGRATION_2_3,
                DatabaseMigrations.MIGRATION_3_4,
                DatabaseMigrations.MIGRATION_4_5
            )
            .build()
    }

    @Provides
    fun provideCustomerDao(db: BizSmsDatabase): CustomerDao = db.customerDao()

    @Provides
    fun provideGroupDao(db: BizSmsDatabase): GroupDao = db.groupDao()

    @Provides
    fun provideTemplateDao(db: BizSmsDatabase): TemplateDao = db.templateDao()

    @Provides
    fun provideCampaignDao(db: BizSmsDatabase): CampaignDao = db.campaignDao()

    @Provides
    fun provideProfileAndSettingsDao(db: BizSmsDatabase): ProfileAndSettingsDao = db.profileAndSettingsDao()
}
