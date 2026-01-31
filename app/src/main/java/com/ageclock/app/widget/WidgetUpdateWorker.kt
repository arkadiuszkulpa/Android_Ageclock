package com.ageclock.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ageclock.app.data.local.AgeclockDatabase

class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, AgeWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isNotEmpty()) {
            val database = AgeclockDatabase.getInstance(context)
            val widgetPeople = database.personDao().getWidgetPeopleSync()

            appWidgetIds.forEach { appWidgetId ->
                AgeWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId, widgetPeople)
            }
        }

        return Result.success()
    }
}
