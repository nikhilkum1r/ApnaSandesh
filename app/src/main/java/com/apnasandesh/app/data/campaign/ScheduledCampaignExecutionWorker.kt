package com.apnasandesh.app.data.campaign

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apnasandesh.app.core.common.PrivacySafeLogger
import com.apnasandesh.app.core.database.CampaignDao
import com.apnasandesh.app.domain.campaign.AutomaticCampaignSendingEngine
import com.apnasandesh.app.domain.campaign.CampaignSendingForegroundService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ScheduledCampaignExecutionWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val campaignDao: CampaignDao,
    private val campaignEngine: AutomaticCampaignSendingEngine
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val campaignId = inputData.getString(KEY_CAMPAIGN_ID) ?: return Result.failure()
        val campaign = campaignDao.getCampaignById(campaignId) ?: return Result.failure()

        PrivacySafeLogger.info("Executing scheduled campaign: ${campaign.name} ($campaignId)")
        try {
            CampaignSendingForegroundService.startService(
                context = appContext,
                campaignId = campaignId,
                intervalSeconds = campaign.sendingIntervalSeconds
            )
        } catch (_: Exception) {
            campaignEngine.startCampaign(campaignId, campaign.sendingIntervalSeconds)
        }

        return Result.success()
    }

    companion object {
        const val KEY_CAMPAIGN_ID = "key_scheduled_campaign_id"
    }
}
