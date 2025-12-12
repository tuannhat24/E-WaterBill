package com.example.billmanager.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.billmanager.data.local.entity.Budget

@Dao
interface BudgetDao {
    // Lấy budget của tháng cụ thể
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND type = :type LIMIT 1")
    fun getBudgetByMonth(month: Int, year: Int, type: Int): LiveData<Budget?>

    // Thêm hoặc cập nhật budget
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: Budget)

    // Xóa budget cũ của loại (điện/nước) trong tháng/năm cụ thể
    @Query("DELETE FROM budgets WHERE month = :month AND year = :year AND type = :type")
    suspend fun deleteBudgetByMonth(month: Int, year: Int, type: Int)

    // Lấy tất cả budget (để vẽ biểu đồ sau này)
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): LiveData<List<Budget>>
}