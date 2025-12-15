package com.example.billmanager.ui.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.billmanager.data.local.entity.Budget
import com.example.billmanager.data.repository.BudgetRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val currentYear = calendar.get(Calendar.YEAR)

    // LiveData
    fun getBudgetForElectric(): LiveData<Budget?> = repository.getBudget(currentMonth, currentYear, 1)
    fun getBudgetForWater(): LiveData<Budget?> = repository.getBudget(currentMonth, currentYear, 2)

    // Hàm lưu
    fun saveBudget(type: Int, amount: Double) {
        viewModelScope.launch {
            val budget = Budget(
                type = type,
                amountLimit = amount,
                month = currentMonth,
                year = currentYear
            )
            repository.saveBudget(budget)
        }
    }

    // data số tiền đã dùng để test UI
    fun getCurrentUsage(type: Int): Double {
        return if (type == 1) 450000.0 else 120000.0
    }
}

// Class Factory để khởi tạo ViewModel
class BudgetViewModelFactory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}