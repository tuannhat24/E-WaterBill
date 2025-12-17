package com.example.billmanager.utils

import android.graphics.Color

object BudgetUtils {
    // Tính phần trăm đã sử dụng
    fun calculateProgress(current: Double, limit: Double): Int {
        if (limit == 0.0) return 100
        return ((current / limit) * 100).toInt()
    }

    // Trả về màu sắc dựa trên % sử dụng
    // Xanh (<80%), Vàng (80-90%), Đỏ (>90%)
    fun getProgressColor(progress: Int): Int {
        return when {
            progress < 80 -> Color.parseColor("#4CAF50") // Green
            progress < 90 -> Color.parseColor("#FFC107") // Yellow
            else -> Color.parseColor("#F44336") // Red
        }
    }

    fun getAlertMessage(progress: Int, limit: Double, typeName: String): String {
        return when {
            progress >= 100 -> "❌ Bạn đã vượt hạn mức $typeName!"
            progress >= 90 -> "🚨 Sắp vượt ngân sách $typeName! Chỉ còn ${100 - progress}%"
            progress >= 80 -> "⚠️ Bạn đã dùng 80% hạn mức $typeName"
            else -> "✅ $typeName đang trong tầm kiểm soát"
        }
    }

    fun getAdvice(progress: Int, type: Int): String {
        if (progress < 90) return ""

        return if (type == 1) { // Điện
            "💡 Gợi ý: Tắt bớt đèn, hạn chế dùng điều hòa giờ cao điểm để tránh hóa đơn tăng vọt!"
        } else { // Nước
            "💧 Gợi ý: Kiểm tra rò rỉ nước hoặc tái sử dụng nước rửa rau để tưới cây."
        }
    }
}