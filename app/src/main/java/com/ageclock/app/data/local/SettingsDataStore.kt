package com.ageclock.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val AI_MESSAGES_ENABLED = booleanPreferencesKey("ai_messages_enabled")
        private val MESSAGE_INDEX = intPreferencesKey("message_index")
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

    val messageIndex: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[MESSAGE_INDEX] ?: 0
        }

    suspend fun getMessageIndex(): Int {
        return context.dataStore.data.first()[MESSAGE_INDEX] ?: 0
    }

    suspend fun incrementMessageIndex() {
        context.dataStore.edit { preferences ->
            val current = preferences[MESSAGE_INDEX] ?: 0
            preferences[MESSAGE_INDEX] = current + 1
        }
    }
}
