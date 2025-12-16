package com.example.billmanager.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.Budget
import com.example.billmanager.data.repository.BudgetRepository
import com.example.billmanager.data.repository.HoaDonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val budgetRepo: BudgetRepository
    private val billRepo: HoaDonRepository

    private val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val currentYear = calendar.get(Calendar.YEAR)

    private val _spentElectric = MutableLiveData<Double>()
    val spentElectric: LiveData<Double> = _spentElectric

    private val _spentWater = MutableLiveData<Double>()
    val spentWater: LiveData<Double> = _spentWater

    init {
        val db = AppDatabase.getInstance(application)
        budgetRepo = BudgetRepository(db.budgetDao())
        billRepo = HoaDonRepository(db.hoaDonDao())

        loadRealUsage()
    }

    // Lấy Budget cấu hình (LiveData từ Room)
    fun getBudgetForElectric(): LiveData<Budget?> = budgetRepo.getBudget(currentMonth, currentYear, 1)
    fun getBudgetForWater(): LiveData<Budget?> = budgetRepo.getBudget(currentMonth, currentYear, 2)

    // Lưu Budget
    fun saveBudget(type: Int, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val budget = Budget(
                type = type,
                amountLimit = amount,
                month = currentMonth,
                year = currentYear
            )
            budgetRepo.saveBudget(budget)
            loadRealUsage()
        }
    }

    // Hàm tính tiền thực tế
    fun loadRealUsage() {
        viewModelScope.launch(Dispatchers.IO) {
            val allBills = billRepo.getAll()

            // Tổng tiền điện tháng này
            val totalElec = allBills.filter {
                it.loai == "Điện" && it.thang == currentMonth && it.nam == currentYear
            }.sumOf { it.tongTien }

            // Tổng tiền nước tháng này
            val totalWater = allBills.filter {
                it.loai == "Nước" && it.thang == currentMonth && it.nam == currentYear
            }.sumOf { it.tongTien }

            _spentElectric.postValue(totalElec.toDouble())
            _spentWater.postValue(totalWater.toDouble())
        }
    }
}