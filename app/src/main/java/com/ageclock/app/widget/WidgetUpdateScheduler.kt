package com.ageclock.app.widget

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ageclock.app.data.local.AgeclockDatabase
import com.ageclock.app.data.model.AgeUnits
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WidgetUpdateScheduler {

    private const val WORK_NAME_DAILY = "age_widget_daily_update"
    private const val WORK_NAME_FREQUENT = "age_widget_frequent_update"

    fun scheduleWidgetUpdates(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Always schedule daily update at midnight for accurate day counts
        val dailyRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(calculateDelayToMidnight(), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME_DAILY,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyRequest
        )

        // Check if any widget person needs frequent updates (has seconds/minutes selected)
        CoroutineScope(Dispatchers.IO).launch {
            val database = AgeclockDatabase.getInstance(context)
            val widgetPeople = database.personDao().getWidgetPeopleSync()

            val needsFrequentUpdates = widgetPeople.any { person ->
                AgeUnits.hasUnit(person.displayUnits, AgeUnits.SECONDS) ||
                        AgeUnits.hasUnit(person.displayUnits, AgeUnits.MINUTES)
            }

            if (needsFrequentUpdates) {
                // Schedule frequent updates (minimum 15 minutes for WorkManager)
                val frequentRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                    15, TimeUnit.MINUTES
                ).build()

                workManager.enqueueUniquePeriodicWork(
                    WORK_NAME_FREQUENT,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    frequentRequest
                )
            } else {
                // Cancel frequent updates if not needed
                workManager.cancelUniqueWork(WORK_NAME_FREQUENT)
            }
        }
    }

    fun cancelWidgetUpdates(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(WORK_NAME_DAILY)
        workManager.cancelUniqueWork(WORK_NAME_FREQUENT)
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
