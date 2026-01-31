package com.ageclock.app.widget

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WidgetUpdateScheduler {

    private const val WORK_NAME = "age_widget_daily_update"

    fun scheduleWidgetUpdates(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Schedule daily update - the widget will also update hourly via updatePeriodMillis
        // but this ensures an update at midnight for accurate day counts
        val dailyRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(calculateDelayToMidnight(), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyRequest
        )
    }

    fun cancelWidgetUpdates(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun calculateDelayToMidnight(): Long {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return midnight.timeInMillis - now.timeInMillis
    }
}
