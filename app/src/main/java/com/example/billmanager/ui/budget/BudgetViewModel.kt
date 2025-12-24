package com.example.billmanager.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.Budget
import com.example.billmanager.data.repository.BudgetRepository
import com.example.billmanager.utils.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    // Sử dụng Repository thay vì DAO
    private val budgetRepo = BudgetRepository(db.budgetDao())
    private val billDao = db.hoaDonDao()
    private val userSession = UserSession(application)

    private val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val currentYear = calendar.get(Calendar.YEAR)

    private val _spentElectric = MutableLiveData<Double>()
    val spentElectric: LiveData<Double> = _spentElectric

    private val _spentWater = MutableLiveData<Double>()
    val spentWater: LiveData<Double> = _spentWater

    init {
        loadRealUsage()
    }

    // Lấy Budget cấu hình (Gọi qua Repository + truyền Email)
    fun getBudgetForElectric(): LiveData<Budget?> {
        val email = userSession.getUserEmail() ?: ""
        return budgetRepo.getBudget(currentMonth, currentYear, 1, email)
    }

    fun getBudgetForWater(): LiveData<Budget?> {
        val email = userSession.getUserEmail() ?: ""
        return budgetRepo.getBudget(currentMonth, currentYear, 2, email)
    }

    // Lưu Budget (Gọi qua Repository)
    fun saveBudget(type: Int, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val email = userSession.getUserEmail() ?: ""

            val budget = Budget(
                typeId = type,
                amountLimit = amount,
                month = currentMonth,
                year = currentYear,
                userEmail = email
            )
            // Repository sẽ tự lo việc Check Update/Insert
            budgetRepo.saveBudget(budget)

            loadRealUsage()
        }
    }

    // Hàm tính tiền thực tế
    fun loadRealUsage() {
        viewModelScope.launch(Dispatchers.IO) {
            val email = userSession.getUserEmail() ?: ""

            // Lấy hóa đơn của riêng User
            val allBills = billDao.getBillsByUser(email)

            val totalElec = allBills.filter {
                it.loai == "Điện" && it.thang == currentMonth && it.nam == currentYear
            }.sumOf { it.tongTien }

            val totalWater = allBills.filter {
                it.loai == "Nước" && it.thang == currentMonth && it.nam == currentYear
            }.sumOf { it.tongTien }

            _spentElectric.postValue(totalElec.toDouble())
            _spentWater.postValue(totalWater.toDouble())
        }
    }
}