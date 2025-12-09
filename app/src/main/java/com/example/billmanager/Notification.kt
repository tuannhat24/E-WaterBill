package com.example.billmanager

data class Notification(
    val id: Int,
    val type: NotificationType,
    val title: String,
    val message: String,
    val time: String,
    var isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class NotificationType {
    WARNING,    // Cảnh báo (vàng)
    WATER,      // Nước (xanh dương)
    ELECTRIC,   // Điện (xanh lá)
    SUCCESS     // Thành công (cam)
}