package com.localreach.bizsms.di

import android.content.Context
import androidx.room.Room
import com.localreach.bizsms.core.common.DefaultDispatcherProvider
import com.localreach.bizsms.core.common.DispatcherProvider
import com.localreach.bizsms.core.database.BizSmsDatabase
import com.localreach.bizsms.core.database.CampaignDao
import com.localreach.bizsms.core.database.CustomerDao
import com.localreach.bizsms.core.database.DatabaseMigrations
import com.localreach.bizsms.core.database.GroupDao
import com.localreach.bizsms.core.database.ProfileAndSettingsDao
import com.localreach.bizsms.core.database.TemplateDao
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
            .addMigrations(DatabaseMigrations.MIGRATION_1_2)
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
