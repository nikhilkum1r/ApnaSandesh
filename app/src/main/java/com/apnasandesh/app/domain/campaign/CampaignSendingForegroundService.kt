package com.apnasandesh.app.domain.campaign

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.apnasandesh.app.MainActivity
import com.apnasandesh.app.R
import com.apnasandesh.app.core.common.PrivacySafeLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CampaignSendingForegroundService : Service() {

    @Inject lateinit var campaignEngine: AutomaticCampaignSendingEngine

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var stateObserverJob: Job? = null

    companion object {
        const val CHANNEL_ID = "apnasandesh_campaign_sending"
        const val NOTIFICATION_ID = 10001
        const val EXTRA_CAMPAIGN_ID = "extra_campaign_id"
        const val EXTRA_INTERVAL_SECONDS = "extra_interval_seconds"
        const val ACTION_START = "com.apnasandesh.app.ACTION_START_CAMPAIGN_SERVICE"
        const val ACTION_STOP = "com.apnasandesh.app.ACTION_STOP_CAMPAIGN_SERVICE"
        const val ACTION_PAUSE = "com.apnasandesh.app.ACTION_PAUSE_CAMPAIGN_SERVICE"
        const val ACTION_RESUME = "com.apnasandesh.app.ACTION_RESUME_CAMPAIGN_SERVICE"

        fun startService(context: Context, campaignId: String, intervalSeconds: Int) {
            val intent = Intent(context, CampaignSendingForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_CAMPAIGN_ID, campaignId)
                putExtra(EXTRA_INTERVAL_SECONDS, intervalSeconds)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, CampaignSendingForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val campaignId = intent.getStringExtra(EXTRA_CAMPAIGN_ID)
                val interval = intent.getIntExtra(EXTRA_INTERVAL_SECONDS, 3)
                if (campaignId != null) {
                    val initialNotification = buildNotification("Preparing campaign...", 0, 0)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        startForeground(
                            NOTIFICATION_ID,
                            initialNotification,
                            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                        )
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(NOTIFICATION_ID, initialNotification, 0)
                    } else {
                        startForeground(NOTIFICATION_ID, initialNotification)
                    }
                    campaignEngine.startCampaign(campaignId, interval)
                    observeEngineState()
                    PrivacySafeLogger.info("Campaign ForegroundService started")
                }
            }
            ACTION_PAUSE -> {
                val state = campaignEngine.liveEngineState.value
                if (state != null) {
                    campaignEngine.pauseCampaign(state.campaignId)
                }
            }
            ACTION_RESUME -> {
                val state = campaignEngine.liveEngineState.value
                if (state != null) {
                    campaignEngine.resumeCampaign(state.campaignId)
                }
            }
            ACTION_STOP -> {
                val state = campaignEngine.liveEngineState.value
                if (state != null && !state.isFinished) {
                    campaignEngine.stopCampaign(state.campaignId)
                }
                stateObserverJob?.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun observeEngineState() {
        stateObserverJob?.cancel()
        stateObserverJob = serviceScope.launch {
            campaignEngine.liveEngineState.collectLatest { state ->
                if (state == null) return@collectLatest

                val notification = buildNotification(
                    text = when {
                        state.isFinished -> "Campaign completed — ${state.sentCount} sent, ${state.failedCount} failed"
                        state.isPaused -> "Campaign paused — ${state.processedCount}/${state.totalRecipients}"
                        state.isRunning -> "Sending ${state.processedCount + 1}/${state.totalRecipients}: ${state.currentRecipientName ?: "..."}"
                        else -> "Campaign queued..."
                    },
                    progress = state.processedCount,
                    max = state.totalRecipients
                )
                val nm = getSystemService(NotificationManager::class.java)
                nm?.notify(NOTIFICATION_ID, notification)

                if (state.isFinished) {
                    // Show completion notification
                    val completionNotification = NotificationCompat.Builder(this@CampaignSendingForegroundService, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_apnasandesh_mark)
                        .setContentTitle("ApnaSandesh Campaign Complete")
                        .setContentText("${state.sentCount} messages sent • ${state.failedCount} failed • ${state.skippedCount} skipped")
                        .setAutoCancel(true)
                        .setContentIntent(createOpenAppIntent())
                        .build()
                    nm?.notify(NOTIFICATION_ID + 1, completionNotification)

                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    private fun buildNotification(text: String, progress: Int, max: Int): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_apnasandesh_mark)
            .setContentTitle("ApnaSandesh — Sending Messages")
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(createOpenAppIntent())

        if (max > 0) {
            builder.setProgress(max, progress, false)
        }

        // Add pause/stop actions
        val pauseIntent = Intent(this, CampaignSendingForegroundService::class.java).apply {
            action = ACTION_PAUSE
        }
        val stopIntent = Intent(this, CampaignSendingForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        builder.addAction(
            android.R.drawable.ic_media_pause, "Pause",
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        )
        builder.addAction(
            android.R.drawable.ic_delete, "Stop",
            PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        )

        return builder.build()
    }

    private fun createOpenAppIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Campaign Sending Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time progress when ApnaSandesh is sending campaign messages"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
