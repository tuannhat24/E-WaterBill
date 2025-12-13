package com.example.billmanager.utils

import java.util.Calendar

object PredictionAlgorithm {

    /**
     * Thuật toán dự đoán tháng tiếp theo
     * @param historyBills: Danh sách số tiền các tháng trước (theo thứ tự cũ -> mới)
     * @param isSummerSeason: Có phải mùa cao điểm không
     */
    fun predictNextMonth(historyBills: List<Double>): Double {
        if (historyBills.isEmpty()) return 0.0

        // 1. Moving Average (Trung bình động) - Lấy tối đa 3 tháng gần nhất
        val n = 3
        val recentBills = historyBills.takeLast(n)
        var average = recentBills.average()

        // 2. Trọng số xu hướng (Trend Weight)
        // Nếu tháng gần nhất cao hơn trung bình -> Xu hướng tăng -> Cộng thêm 5%
        if (historyBills.last() > average) {
            average *= 1.05
        }

        // 3. Yếu tố mùa vụ (Seasonal Factor)
        // Lấy tháng hiện tại để xem tháng sau có phải mùa hè (T5,6,7) không
        val calendar = Calendar.getInstance()
        val nextMonth = calendar.get(Calendar.MONTH) + 2 // +1 index, +1 next month

        // Ở VN, mùa hè (tháng 5, 6, 7) tiền điện thường tăng cao
        val isSummer = nextMonth in 5..7
        val seasonalFactor = if (isSummer) 1.15 else 1.0 // Tăng 15% nếu vào hè

        return average * seasonalFactor
    }

    // Đánh giá độ tin cậy dựa trên số lượng dữ liệu
    fun getConfidenceLevel(historyCount: Int): String {
        return when {
            historyCount >= 6 -> "Cao (Dữ liệu > 6 tháng)"
            historyCount >= 3 -> "Trung bình"
            else -> "Thấp (Cần thêm dữ liệu)"
        }
    }
}