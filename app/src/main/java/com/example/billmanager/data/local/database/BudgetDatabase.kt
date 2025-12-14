package com.example.billmanager.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.billmanager.data.local.dao.BudgetDao
import com.example.billmanager.data.local.entity.Budget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Budget::class], version = 1, exportSchema = false)
abstract class BudgetDatabase : RoomDatabase() {

    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: BudgetDatabase? = null

        fun getDatabase(context: Context): BudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudgetDatabase::class.java,
                    "budget_module_db" // Tên file DB riêng cho module này
                )
                    .addCallback(BudgetDatabaseCallback()) // Tự tạo dữ liệu mẫu khi cài app
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Class này giúp tạo dữ liệu mẫu ngay khi database được khởi tạo lần đầu
        private class BudgetDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.budgetDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: BudgetDao) {
                // Xóa hết dữ liệu cũ (nếu muốn)
                // dao.deleteAll()

                // Thêm dữ liệu mẫu để test
                val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

                // Mẫu: Budget Điện 500k, Nước 200k
                dao.insertOrUpdateBudget(Budget(type = 1, amountLimit = 500000.0, month = currentMonth, year = currentYear))
                dao.insertOrUpdateBudget(Budget(type = 2, amountLimit = 200000.0, month = currentMonth, year = currentYear))
            }
        }
    }
}