package com.example.billmanager.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.billmanager.data.local.entity.Budget

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND type = :type LIMIT 1")
    fun getBudgetByMonth(month: Int, year: Int, type: Int): LiveData<Budget?>

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND type = :type LIMIT 1")
    fun getBudgetSync(month: Int, year: Int, type: Int): Budget?
    // --------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBudget(budget: Budget)

    @Query("DELETE FROM budgets WHERE month = :month AND year = :year AND type = :type")
    fun deleteBudgetByMonth(month: Int, year: Int, type: Int)

    @Query("SELECT * FROM budgets")
    fun getAllBudgetsSync(): List<Budget>
}