package com.ageclock.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val AI_MESSAGES_ENABLED = booleanPreferencesKey("ai_messages_enabled")
    }

    val aiMessagesEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[AI_MESSAGES_ENABLED] ?: false
        }

    suspend fun setAiMessagesEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AI_MESSAGES_ENABLED] = enabled
        }
    }
}
