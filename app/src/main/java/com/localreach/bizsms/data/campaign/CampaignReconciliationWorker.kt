package com.localreach.bizsms.data.campaign

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.localreach.bizsms.core.common.PrivacySafeLogger
import com.localreach.bizsms.core.database.CampaignDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CampaignReconciliationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val campaignDao: CampaignDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val fifteenMinutesAgo = System.currentTimeMillis() - (15 * 60 * 1000L)
        val reconciledCount = campaignDao.reconcileStaleSubmittedRecipients(fifteenMinutesAgo)
        if (reconciledCount > 0) {
            PrivacySafeLogger.info("Reconciled $reconciledCount stale in-flight recipients to UNKNOWN state.")
        }
        return Result.success()
    }
}
