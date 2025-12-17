package com.example.billmanager.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_pref")

class AppDataStore(private val context: Context) {

    companion object {
        val KEY_NOTI_ENABLED = booleanPreferencesKey("noti_enabled")

        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val notiEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_NOTI_ENABLED] ?: true
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DARK_MODE] ?: false
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTI_ENABLED] = enabled }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }
}