package com.apnasandesh.app.data.campaign

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apnasandesh.app.MainActivity
import com.apnasandesh.app.R
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

        if (campaign.transportType == "COMPOSER_HANDOFF") {
            showScheduledCampaignNotification(campaign.name)
        } else {
            try {
                CampaignSendingForegroundService.startService(
                    context = appContext,
                    campaignId = campaignId,
                    intervalSeconds = campaign.sendingIntervalSeconds
                )
            } catch (_: Exception) {
                campaignEngine.startCampaign(campaignId, campaign.sendingIntervalSeconds)
            }
        }

        return Result.success()
    }

    private fun showScheduledCampaignNotification(campaignName: String) {
        val channelId = "apnasandesh_scheduled_reminders"
        val notificationManager = appContext.getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Scheduled Campaign Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you when a scheduled campaign is ready to send"
            }
            notificationManager?.createNotificationChannel(channel)
        }

        val openIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_apnasandesh_mark)
            .setContentTitle("Scheduled Campaign Ready")
            .setContentText("Tap to review and dispatch '$campaignName' in your SMS app")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager?.notify(20001, notification)
    }

    companion object {
        const val KEY_CAMPAIGN_ID = "key_scheduled_campaign_id"
    }
}
