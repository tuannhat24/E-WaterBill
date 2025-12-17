package com.example.billmanager.ui.prediction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.example.billmanager.data.repository.BudgetRepository
import com.example.billmanager.data.repository.HoaDonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class PredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val billRepo: HoaDonRepository
    private val budgetRepo: BudgetRepository

    // LiveData chứa kết quả dự báo
    private val _predictionData = MutableLiveData<PredictionResult>()
    val predictionData: LiveData<PredictionResult> = _predictionData

    // Data Class chứa kết quả trả về cho UI
    data class PredictionResult(
        val totalPredicted: Double,
        val electricPredicted: Double,
        val waterPredicted: Double,
        val budgetTotal: Double,
        val historyElectric: List<Double>,
        val historyWater: List<Double>,
        val message: String
    )

    init {
        val db = AppDatabase.getInstance(application)
        billRepo = HoaDonRepository(db.hoaDonDao())
        budgetRepo = BudgetRepository(db.budgetDao())
    }

    fun calculatePrediction() {
        viewModelScope.launch(Dispatchers.IO) {
            val allBills = billRepo.getAll()

            // 1. Lấy dữ liệu lịch sử
            val electricHistory = getHistoryData(allBills, "Điện")
            val waterHistory = getHistoryData(allBills, "Nước")

            // 2. Dự báo tháng tới
            val predElectric = calculateMovingAverage(electricHistory)
            val predWater = calculateMovingAverage(waterHistory)
            val totalPred = predElectric + predWater

            // 3. Lấy Budget hiện tại (Sử dụng hàm Sync chuẩn từ Repository)
            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)

            // Lấy ngân sách Điện + Nước
            val elecBudget = budgetRepo.getBudgetSync(month, year, 1)?.amountLimit ?: 0.0
            val waterBudget = budgetRepo.getBudgetSync(month, year, 2)?.amountLimit ?: 0.0
            val budgetTotal = elecBudget + waterBudget

            // 4. Tạo thông báo
            val msg = if (budgetTotal > 0 && totalPred > budgetTotal) {
                "⚠️ DỰ BÁO VƯỢT NGÂN SÁCH: ${String.format("%,.0f", totalPred - budgetTotal)}đ"
            } else if (budgetTotal > 0) {
                "✅ Dự báo nằm trong hạn mức an toàn."
            } else {
                "ℹ️ Chưa thiết lập hạn mức (Budget) tháng này."
            }

            // 5. Post kết quả
            _predictionData.postValue(
                PredictionResult(
                    totalPredicted = totalPred,
                    electricPredicted = predElectric,
                    waterPredicted = predWater,
                    budgetTotal = budgetTotal,
                    historyElectric = electricHistory,
                    historyWater = waterHistory,
                    message = msg
                )
            )
        }
    }

    // Hàm lấy danh sách tổng tiền 6 tháng gần nhất
    private fun getHistoryData(allBills: List<HoaDonEntity>, type: String): List<Double> {
        return allBills
            .filter { it.loai == type }
            .sortedWith(compareBy({ it.nam }, { it.thang })) // Sort cũ -> mới
            .takeLast(6) // Lấy 6 tháng cuối
            .map { it.tongTien.toDouble() }
    }

    // Thuật toán trung bình động (Moving Average)
    private fun calculateMovingAverage(history: List<Double>): Double {
        if (history.isEmpty()) return 0.0
        val last3Months = history.takeLast(3)
        return last3Months.average()
    }
}