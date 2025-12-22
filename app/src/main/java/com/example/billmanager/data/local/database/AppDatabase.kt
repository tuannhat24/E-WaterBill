package com.example.billmanager.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.example.billmanager.data.local.dao.BudgetDao
import com.example.billmanager.data.local.dao.HoaDonDao
import com.example.billmanager.data.local.dao.NotificationDao
import com.example.billmanager.data.local.dao.UserDao
import com.example.billmanager.data.local.entity.Budget
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.example.billmanager.data.local.entity.NotificationEntity
import com.example.billmanager.data.local.entity.User
import com.example.billmanager.data.local.dao.LocationDao
import com.example.billmanager.data.local.entity.LocationEntity

@Database(
    entities = [
        User::class,
        HoaDonEntity::class,
        Budget::class,
        NotificationEntity::class,
        LocationEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class) // Để xử lý NotificationType, Date...
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun hoaDonDao(): HoaDonDao
    abstract fun budgetDao(): BudgetDao
    abstract fun notificationDao(): NotificationDao
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bill_manager_full.db"
                )
                    .addCallback(DatabaseCallback(context))
                    .fallbackToDestructiveMigration() // Reset DB nếu đổi version (để tránh crash khi dev)
                    .allowMainThreadQueries()         // Chỉ dùng cho đồ án/test nhanh
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Hàm hỗ trợ tương thích code cũ (nếu code cũ gọi getDatabase)
        fun getDatabase(context: Context): AppDatabase = getInstance(context)
    }
}