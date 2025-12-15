package com.example.billmanager.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.billmanager.data.local.dao.HoaDonDao
import com.example.billmanager.data.local.entity.HoaDonEntity

@Database(
    entities = [HoaDonEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun hoaDonDao(): HoaDonDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hoa_don_db"
                ).allowMainThreadQueries() // cho đồ án
                    .build().also { INSTANCE = it }
            }
    }
}


