package com.apnasandesh.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.apnasandesh.app.core.database.CampaignDao
import com.apnasandesh.app.core.database.DatabaseSeeder
import com.apnasandesh.app.data.campaign.CampaignReconciliationWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ApnaSandeshApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var databaseSeeder: DatabaseSeeder
    @Inject lateinit var campaignDao: CampaignDao

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            runCatching {
                databaseSeeder.ensureSeeded()
            }.onFailure { e ->
                android.util.Log.e("ApnaSandeshApp", "Failed during database seeding", e)
            }
            runCatching {
                // Recovery after restart: any campaigns left in RUNNING/IN_PROGRESS are safely paused
                campaignDao.reconcileRunningCampaignsToPaused()
            }.onFailure { e ->
                android.util.Log.e("ApnaSandeshApp", "Failed during campaign reconciliation", e)
            }
        }
        runCatching {
            val reconciliationRequest = PeriodicWorkRequestBuilder<CampaignReconciliationWorker>(
                6, TimeUnit.HOURS
            ).build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "campaign_stale_submission_reconciliation",
                ExistingPeriodicWorkPolicy.KEEP,
                reconciliationRequest
            )
        }.onFailure { e ->
            android.util.Log.e("ApnaSandeshApp", "Failed scheduling reconciliation worker", e)
        }
    }
}
