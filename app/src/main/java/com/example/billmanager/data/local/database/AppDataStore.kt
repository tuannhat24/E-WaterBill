package com.example.billmanager.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Tạo extension property để truy cập DataStore (tên file: settings_pref)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_pref")

class AppDataStore(private val context: Context) {

    // 1. Định nghĩa các KEY
    companion object {
        val KEY_NOTI_ENABLED = booleanPreferencesKey("noti_enabled")
        val KEY_BUDGET_ALERT = booleanPreferencesKey("budget_alert_enabled") // Bật tắt cảnh báo budget
        val KEY_BUDGET_THRESHOLD = intPreferencesKey("budget_threshold")     // Ngưỡng (80, 90, 100)
        val KEY_DEFAULT_VIEW = intPreferencesKey("default_view")             // 0: Dashboard, 1: Bill List
    }

    // 2. Hàm Lấy dữ liệu (Trả về Flow để tự động update UI khi data thay đổi)
    val notiEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_NOTI_ENABLED] ?: true // Mặc định là True (Bật)
    }

    val budgetAlertFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_BUDGET_ALERT] ?: true
    }

    val budgetThresholdFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_BUDGET_THRESHOLD] ?: 80 // Mặc định 80%
    }

    // 3. Hàm Lưu dữ liệu
    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTI_ENABLED] = enabled
        }
    }

    suspend fun setBudgetAlertEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BUDGET_ALERT] = enabled
        }
    }

    suspend fun setBudgetThreshold(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BUDGET_THRESHOLD] = value
        }
    }
}