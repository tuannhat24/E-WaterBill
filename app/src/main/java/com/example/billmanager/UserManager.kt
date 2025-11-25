package com.example.billmanager
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson

import android.content.Context

object UserManager {
    private const val PREF_NAME = "user_prefs"
    private const val KEY_CURRENT_USER  = "KEY_CURRENT_USER"
    private const val KEY_USERS = "user_list"

    //lấy danh sách user
    fun getUsers(context: Context): MutableList<User> {
        val shared = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = shared.getString(KEY_USERS, null) ?: return mutableListOf()

        val type = object : TypeToken<MutableList<User>>() {}.type
        return Gson().fromJson(json, type)
    }
    // Lấy user hiện tại
    fun getCurrentUser(context: Context): User? {
        val shared = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = shared.getString(KEY_CURRENT_USER, null) ?: return null
        return Gson().fromJson(json, User::class.java)
    }
    // Lưu danh sách user vào SharedPreferences
    fun saveUsers(context: Context, users: MutableList<User>) {
        val shared = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = shared.edit()

        val json = Gson().toJson(users)
        editor.putString(KEY_USERS, json)
        editor.apply()
    }
    //lưu user hiện tại
    fun saveCurrentUser(context: Context, user: User) {
        val shared = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(user)
        shared.edit().putString(KEY_CURRENT_USER, json).apply()
    }

    // Thêm 1 user vào danh sách
    fun addUser(context: Context, user: User) {
        val users = getUsers(context)
        users.add(user)
        saveUsers(context, users)
    }

    //đăng xuất
    fun removeUser(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().remove(KEY_USERS).apply()
    }

    //update user
    fun updateUser(context: Context) {
        val shared = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}