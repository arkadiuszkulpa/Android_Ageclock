package com.ageclock.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.View
import android.widget.RemoteViews
import com.ageclock.app.MainActivity
import com.ageclock.app.R
import com.ageclock.app.data.local.AgeclockDatabase
import com.ageclock.app.data.local.SettingsDataStore
import com.ageclock.app.data.model.Person
import com.ageclock.app.util.AgeCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class AgeWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val MAX_WIDGET_PEOPLE = 4

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            people: List<Person>,
            aiEnabled: Boolean = false,
            messageIndex: Int = 0
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

                    // Try to use AI message if available and enabled
                    val displayText = if (aiEnabled && !person.aiMessages.isNullOrEmpty()) {
                        try {
                            val messages = Json.decodeFromString<List<String>>(person.aiMessages)
                            if (messages.isNotEmpty()) {
                                // Rotate message based on stored message index (increments on each app resume)
                                messages[messageIndex % messages.size]
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            null
                        }
                    } else {
                        null
                    }

                    val personView = RemoteViews(context.packageName, R.layout.widget_person_item)

                    if (displayText != null) {
                        // Use AI message directly (no HTML formatting needed)
                        personView.setTextViewText(R.id.person_info, displayText)
                    } else {
                        // Fall back to standard format
                        val ageText = age.formatCompact(person.displayUnits)
                        val combinedText = Html.fromHtml(
                            "<b>${person.name}</b> $ageText",
                            Html.FROM_HTML_MODE_COMPACT
                        )
                        personView.setTextViewText(R.id.person_info, combinedText)
                    }

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
                    val settingsDataStore = SettingsDataStore(context)
                    val aiEnabled = settingsDataStore.aiMessagesEnabled.first()
                    val messageIndex = settingsDataStore.getMessageIndex()

                    appWidgetIds.forEach { appWidgetId ->
                        updateAppWidget(context, appWidgetManager, appWidgetId, widgetPeople, aiEnabled, messageIndex)
                    }
                }
            }
        }

    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AgeclockDatabase.getInstance(context)
            val widgetPeople = database.personDao().getWidgetPeopleSync()
            val settingsDataStore = SettingsDataStore(context)
            val aiEnabled = settingsDataStore.aiMessagesEnabled.first()
            val messageIndex = settingsDataStore.getMessageIndex()

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, widgetPeople, aiEnabled, messageIndex)
            }

            // Updates are handled by WorkManager via WidgetUpdateScheduler
            // Widget updates every 15 minutes minimum (WorkManager limitation)
        }
    }

    override fun onEnabled(context: Context) {
        // Schedule periodic updates via WorkManager (for daily/midnight updates)
        WidgetUpdateScheduler.scheduleWidgetUpdates(context)
    }

    override fun onDisabled(context: Context) {
        // Cancel all scheduled updates
        WidgetUpdateScheduler.cancelWidgetUpdates(context)
    }
}
