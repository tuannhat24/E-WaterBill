package com.example.billmanager.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class UserSession(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveUser(email: String) {
        prefs.edit {
            putString("email", email)
        }
    }

    fun getUserEmail(): String? {
        return prefs.getString("email", null)
    }

    fun clearSession() {
        prefs.edit { clear() }
    }

    fun isLoggedIn(): Boolean {
        return getUserEmail() != null
    }
}
