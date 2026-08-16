package com.apptime

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_MONITOR, getString(R.string.channel_monitor_name), NotificationManager.IMPORTANCE_LOW).apply {
                description = getString(R.string.channel_monitor_desc)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ALARM, getString(R.string.channel_alarm_name), NotificationManager.IMPORTANCE_HIGH).apply {
                description = getString(R.string.channel_alarm_desc)
                enableVibration(true)
            }
        )
    }

    companion object {
        const val CHANNEL_MONITOR = "usage_monitor"
        const val CHANNEL_ALARM = "alarm_alerts"
    }
}
