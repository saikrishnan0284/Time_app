package com.apptime.worker

import android.content.Context
import androidx.work.*
import com.apptime.data.TimePeriod
import com.apptime.data.db.AppTimeDatabase
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ResetAlarmsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dao = AppTimeDatabase.getInstance(applicationContext).alarmDao()
        dao.resetPeriodAlarms(TimePeriod.TODAY.name)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "daily_alarm_reset"

        fun schedule(context: Context) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 1)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val delayMs = cal.timeInMillis - System.currentTimeMillis()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<ResetAlarmsWorker>(1, TimeUnit.DAYS)
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .build()
            )
        }
    }
}
