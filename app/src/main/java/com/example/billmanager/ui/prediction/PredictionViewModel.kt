package com.example.billmanager.ui.prediction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.billmanager.data.repository.BudgetRepository
import com.example.billmanager.utils.PredictionAlgorithm
import kotlinx.coroutines.launch
import java.util.Calendar

class PredictionViewModel(private val budgetRepo: BudgetRepository) : ViewModel() {

    private val _predictionData = MutableLiveData<PredictionResult>()
    val predictionData: LiveData<PredictionResult> = _predictionData

    // Data Class chứa kết quả trả về cho View
    data class PredictionResult(
        val totalAmount: Double,
        val electricAmount: Double,
        val waterAmount: Double,
        val confidence: String,
        val historyElectric: List<Double>, // Để vẽ biểu đồ
        val budgetTotal: Double // Để so sánh
    )

    fun calculatePrediction() {
        viewModelScope.launch {
            // 1. Giả lập lấy dữ liệu lịch sử từ Module 1 (Bill Database)
            // Trong thực tế: val historyBills = billRepository.getLast6Months()
            val historyElectric = listOf(450000.0, 470000.0, 460000.0, 480000.0, 500000.0) // 5 tháng qua
            val historyWater = listOf(100000.0, 110000.0, 105000.0, 100000.0, 120000.0)

            // 2. Tính toán dự đoán (Dùng Algorithm)
            val predElectric = PredictionAlgorithm.predictNextMonth(historyElectric)
            val predWater = PredictionAlgorithm.predictNextMonth(historyWater)
            val totalPred = predElectric + predWater
            val confidence = PredictionAlgorithm.getConfidenceLevel(historyElectric.size)

            // 3. Lấy Budget hiện tại từ Module 4 (Room DB)
            // (Ở đây lấy LiveData và observe thủ công hoặc lấy value trực tiếp nếu dùng hàm suspend trả về object)
            // giả định budget là 600k (có thể thay bằng logic gọi DB thật)
            val currentBudget = 600000.0

            // 4. Post kết quả
            _predictionData.value = PredictionResult(
                totalAmount = totalPred,
                electricAmount = predElectric,
                waterAmount = predWater,
                confidence = confidence,
                historyElectric = historyElectric,
                budgetTotal = currentBudget
            )
        }
    }
}

// Factory
class PredictionViewModelFactory(private val repo: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PredictionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PredictionViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}