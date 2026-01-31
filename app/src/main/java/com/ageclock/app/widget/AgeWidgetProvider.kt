package com.ageclock.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.ageclock.app.MainActivity
import com.ageclock.app.R
import com.ageclock.app.data.local.AgeclockDatabase
import com.ageclock.app.data.model.AgeGranularity
import com.ageclock.app.data.model.Person
import com.ageclock.app.util.AgeCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AgeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AgeclockDatabase.getInstance(context)
            val widgetPeople = database.personDao().getWidgetPeopleSync()

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, widgetPeople)
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Schedule periodic updates via WorkManager
        WidgetUpdateScheduler.scheduleWidgetUpdates(context)
    }

    override fun onDisabled(context: Context) {
        // Cancel scheduled updates
        WidgetUpdateScheduler.cancelWidgetUpdates(context)
    }

    companion object {
        private const val MAX_WIDGET_PEOPLE = 4

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            people: List<Person>
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_age)

            // Clear existing views in the container
            views.removeAllViews(R.id.widget_people_container)

            if (people.isEmpty()) {
                // Show empty state
                views.setViewVisibility(R.id.widget_empty_message, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_empty_message, View.GONE)

                // Add each person (limit to MAX_WIDGET_PEOPLE for space)
                val currentTime = System.currentTimeMillis()
                people.take(MAX_WIDGET_PEOPLE).forEach { person ->
                    val age = AgeCalculator.calculateAge(person.dateOfBirth, currentTime)

                    // Don't use TOTAL_SECONDS in widget (battery concern), fall back to TOTAL_MINUTES
                    val displayGranularity = if (person.ageDisplayGranularity == AgeGranularity.TOTAL_SECONDS) {
                        AgeGranularity.TOTAL_MINUTES
                    } else {
                        person.ageDisplayGranularity
                    }

                    val ageText = age.formatCompact(displayGranularity)

                    val personView = RemoteViews(context.packageName, R.layout.widget_person_item)
                    personView.setTextViewText(R.id.person_name, person.name)
                    personView.setTextViewText(R.id.person_age, ageText)

                    views.addView(R.id.widget_people_container, personView)
                }
            }

            // Set up click action to open the main app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun notifyWidgetDataChanged(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, AgeWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val database = AgeclockDatabase.getInstance(context)
                    val widgetPeople = database.personDao().getWidgetPeopleSync()

                    appWidgetIds.forEach { appWidgetId ->
                        updateAppWidget(context, appWidgetManager, appWidgetId, widgetPeople)
                    }
                }
            }
        }
    }
}
