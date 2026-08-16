package com.apptime.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import java.util.Calendar

class AppUsageHelper(private val context: Context) {

    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getUsageStats(period: TimePeriod, excludedPackages: Set<String> = emptySet()): List<AppUsage> {
        if (!hasUsagePermission()) return emptyList()
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val pm = context.packageManager
        val (startTime, endTime) = getTimeRange(period)

        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        return stats
            .filter { it.totalTimeInForeground > 0 && it.packageName !in excludedPackages }
            .mapNotNull { stat ->
                runCatching {
                    val appInfo = pm.getApplicationInfo(stat.packageName, 0)
                    AppUsage(
                        packageName = stat.packageName,
                        appName = pm.getApplicationLabel(appInfo).toString(),
                        totalTimeMs = stat.totalTimeInForeground
                    )
                }.getOrNull()
            }
            .sortedByDescending { it.totalTimeMs }
    }

    fun getInstalledApps(): List<AppUsage> {
        val pm = context.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map { AppUsage(it.packageName, pm.getApplicationLabel(it).toString(), 0L) }
            .sortedBy { it.appName }
    }

    companion object {
        fun getTimeRange(period: TimePeriod): Pair<Long, Long> {
            val cal = Calendar.getInstance()
            val end = cal.timeInMillis
            when (period) {
                TimePeriod.TODAY -> {
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                }
                TimePeriod.WEEK -> {
                    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                }
                TimePeriod.MONTH -> {
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                }
            }
            return Pair(cal.timeInMillis, end)
        }

        fun isAlarmStale(lastTriggeredAt: Long, period: TimePeriod): Boolean {
            if (lastTriggeredAt == 0L) return true
            return lastTriggeredAt < getTimeRange(period).first
        }
    }
}
