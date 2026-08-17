package com.apptime.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.apptime.MainActivity
import com.apptime.MainApplication
import com.apptime.data.AlarmScope
import com.apptime.data.AlertType
import com.apptime.data.AppUsageHelper
import com.apptime.data.TimePeriod
import com.apptime.data.datastore.SettingsDataStore
import com.apptime.data.db.AppTimeDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class UsageMonitorService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID_SERVICE, buildServiceNotification())
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        mediaPlayer?.release()
        super.onDestroy()
    }

    private fun startMonitoring() {
        scope.launch {
            while (isActive) {
                runCatching { checkAlarms() }
                delay(60_000L)
            }
        }
    }

    private suspend fun checkAlarms() {
        val dataStore = SettingsDataStore(this)
        if (!dataStore.serviceEnabledFlow.first()) return
        val dao = AppTimeDatabase.getInstance(this).alarmDao()
        val helper = AppUsageHelper(this)
        if (!helper.hasUsagePermission()) return

        val excluded = SettingsDataStore(this).excludedPackagesFlow.first()
        val alarms = dao.getActiveAlarms()
        if (alarms.isEmpty()) return

        val usageCache = mutableMapOf<TimePeriod, List<com.apptime.data.AppUsage>>()

        for (alarm in alarms) {
            val period = runCatching { TimePeriod.valueOf(alarm.period) }.getOrDefault(TimePeriod.TODAY)
            if (!AppUsageHelper.isAlarmStale(alarm.lastTriggeredAt, period)) continue

            val usageList = usageCache.getOrPut(period) { helper.getUsageStats(period, excluded) }
            val alarmScope = runCatching { AlarmScope.valueOf(alarm.scope) }.getOrDefault(AlarmScope.SPECIFIC_APP)

            val usageMs = when (alarmScope) {
                AlarmScope.SPECIFIC_APP -> usageList.find { it.packageName == alarm.packageName }?.totalTimeMs ?: 0L
                AlarmScope.ANY_SINGLE_APP -> usageList.maxOfOrNull { it.totalTimeMs } ?: 0L
                AlarmScope.TOTAL_ALL_APPS -> usageList.sumOf { it.totalTimeMs }
            }

            if (usageMs >= alarm.thresholdMinutes * 60_000L) {
                withContext(Dispatchers.Main) {
                    triggerAlarm(alarm.id, alarm.appName, alarm.thresholdMinutes, alarm.alertType, alarm.alertUri)
                }
                dao.setTriggered(alarm.id, System.currentTimeMillis())
            }
        }
    }

    private fun triggerAlarm(id: Int, appName: String, minutes: Int, alertTypeName: String, alertUri: String?) {
        val h = minutes / 60
        val m = minutes % 60
        val timeStr = when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
        val alertType = runCatching { AlertType.valueOf(alertTypeName) }.getOrDefault(AlertType.NOTIFICATION)

        // Always post a notification
        val pi = PendingIntent.getActivity(
            this, id,
            Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notif = NotificationCompat.Builder(this, MainApplication.CHANNEL_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ $appName — Limit Reached")
            .setContentText("You've spent $timeStr on $appName")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIF_ID_ALARM_BASE + id, notif)

        when (alertType) {
            AlertType.VIBRATE -> vibrate()
            AlertType.RINGTONE -> playRingtone(alertUri)
            AlertType.SONG -> playSong(alertUri)
            AlertType.NOTIFICATION -> Unit
        }
    }

    private fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun playRingtone(uriStr: String?) {
        val uri = uriStr?.let { Uri.parse(it) }
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        runCatching { RingtoneManager.getRingtone(this, uri)?.play() }
    }

    private fun playSong(uriStr: String?) {
        uriStr ?: return
        runCatching {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@UsageMonitorService, Uri.parse(uriStr))
                prepare()
                start()
            }
        }
    }

    private fun buildServiceNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, MainApplication.CHANNEL_MONITOR)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("AppTime is active")
            .setContentText("Monitoring app usage in the background")
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIF_ID_SERVICE = 1
        private const val NOTIF_ID_ALARM_BASE = 1000

        fun startService(context: Context) {
            val intent = Intent(context, UsageMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
