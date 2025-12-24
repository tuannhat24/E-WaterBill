package com.example.billmanager.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.billmanager.data.local.entity.Budget

@Dao
interface BudgetDao {
    // Lấy hạn mức của User cụ thể
    @Query("SELECT * FROM budget WHERE month = :m AND year = :y AND typeId = :t AND userEmail = :email LIMIT 1")
    fun getBudgetByUser(m: Int, y: Int, t: Int, email: String): LiveData<Budget?>

    // Hàm đồng bộ (cho check notification)
    @Query("SELECT * FROM budget WHERE month = :m AND year = :y AND typeId = :t AND userEmail = :email LIMIT 1")
    fun getBudgetSyncByUser(m: Int, y: Int, t: Int, email: String): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(budget: Budget)

    @Update
    fun update(budget: Budget)
}