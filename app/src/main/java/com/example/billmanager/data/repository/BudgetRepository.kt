package com.example.billmanager.data.repository

import androidx.lifecycle.LiveData
import com.example.billmanager.data.local.dao.BudgetDao
import com.example.billmanager.data.local.entity.Budget

class BudgetRepository(private val budgetDao: BudgetDao) {

    fun getBudget(month: Int, year: Int, type: Int): LiveData<Budget?> {
        return budgetDao.getBudgetByMonth(month, year, type)
    }

    fun getBudgetSync(month: Int, year: Int, type: Int): Budget? {
        return budgetDao.getBudgetSync(month, year, type)
    }

    suspend fun saveBudget(budget: Budget) {
        //Xóa cái cũ (tránh trùng lặp ID)
        budgetDao.deleteBudgetByMonth(budget.month, budget.year, budget.type)
        budgetDao.insertBudget(budget)
    }
}