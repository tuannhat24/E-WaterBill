package com.example.billmanager.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.billmanager.data.local.dao.UserDao
import com.example.billmanager.data.local.entity.User

@Database(entities = [User::class], version = 1)
abstract class UsersDB : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: UsersDB? = null

        fun getInstance(context: Context): UsersDB {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    UsersDB::class.java,
                    "auth_db"
                ).allowMainThreadQueries().build().also {
                    INSTANCE = it
                }
            }
        }
    }
}