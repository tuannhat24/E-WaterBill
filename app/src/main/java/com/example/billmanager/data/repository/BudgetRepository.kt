package com.example.billmanager.data.repository

import androidx.lifecycle.LiveData
import com.example.billmanager.data.local.dao.BudgetDao
import com.example.billmanager.data.local.entity.Budget

class BudgetRepository(private val budgetDao: BudgetDao) {

    fun getBudget(month: Int, year: Int, type: Int, email: String): LiveData<Budget?> {
        return budgetDao.getBudgetByUser(month, year, type, email)
    }

    fun getBudgetSync(month: Int, year: Int, type: Int, email: String): Budget? {
        return budgetDao.getBudgetSyncByUser(month, year, type, email)
    }

    suspend fun saveBudget(budget: Budget) {
        val existing = budgetDao.getBudgetSyncByUser(
            budget.month,
            budget.year,
            budget.typeId,
            budget.userEmail
        )

        if (existing != null) {
            budgetDao.update(budget.copy(id = existing.id))
        } else {
            budgetDao.insert(budget)
        }
    }
}