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
import java.time.Month
import java.util.Calendar

class PredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val billRepo: HoaDonRepository
    private val budgetRepo: BudgetRepository

    private val _predictionData = MutableLiveData<PredictionResult>()
    val predictionData: LiveData<PredictionResult> = _predictionData

    data class PredictionResult(
        val totalPredicted: Double,
        val electricPredicted: Double,
        val waterPredicted: Double,
        val historyElectric: List<Double>,
        val historyWater: List<Double>,
        val realMonths: List<String>,
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

            // Lấy 6 tháng gần nhất có dữ liệu (Dựa trên hóa đơn Điện làm chuẩn thời gian)
            val sortedBills = allBills.filter { it.loai == "Điện" }
                .sortedWith(compareBy({ it.nam }, { it.thang }))
                .takeLast(6)

            // Tạo danh sách nhãn tháng thật (VD: "T10", "T11")
            val labels = sortedBills.map { "T${it.thang}" }

            // Lấy dữ liệu giá trị tương ứng
            val electricHistory = getHistoryData(allBills, "Điện", 6)
            val waterHistory = getHistoryData(allBills, "Nước", 6)

            // ... (Phần tính toán Moving Average giữ nguyên) ...
            val predElectric = calculateMovingAverage(electricHistory)
            val predWater = calculateMovingAverage(waterHistory)
            val totalPred = predElectric + predWater

            // ... (Phần thông báo giữ nguyên) ...
            val msg = "..."

            _predictionData.postValue(
                PredictionResult(
                    totalPredicted = totalPred,
                    electricPredicted = predElectric,
                    waterPredicted = predWater,
                    historyElectric = electricHistory,
                    historyWater = waterHistory,
                    realMonths = labels,
                    message = msg
                )
            )
        }
    }

    // Hàm lấy danh sách tổng tiền 6 tháng gần nhất
    private fun getHistoryData(allBills: List<HoaDonEntity>, type: String, limit: Int): List<Double> {
        return allBills
            .filter { it.loai == type }
            .sortedWith(compareBy({ it.nam }, { it.thang }))
            .takeLast(limit)
            .map { it.tongTien.toDouble() }
    }

    // Thuật toán trung bình động (Moving Average)
    private fun calculateMovingAverage(history: List<Double>): Double {
        if (history.isEmpty()) return 0.0
        val last3Months = history.takeLast(3)
        return last3Months.average()
    }
}