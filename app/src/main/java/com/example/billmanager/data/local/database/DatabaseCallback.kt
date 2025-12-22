package com.example.billmanager.data.local.database

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.billmanager.data.local.entity.User

class DatabaseCallback(
    private val context: Context
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        Thread {
            val database = AppDatabase.getInstance(context)
            val userDao = database.userDao()

            if (userDao.countAdmin() == 0) {
                userDao.insert(
                    User(
                        fullName = "Admin",
                        email = "admin@system.com",
                        phoneNumber = "0000000000",
                        password = "admin123@",
                        role = "Admin",
                        isActive = true
                    )
                )
            }
        }.start()
    }
}
