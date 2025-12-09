package com.example.billmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "bill_channel_default"
        const val CHANNEL_NAME = "Nhắc nhở hóa đơn"
        const val CHANNEL_DESC = "Thông báo về hạn đóng tiền điện, nước"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Hàm hiển thị thông báo nâng cao
    fun showNotification(title: String, message: String, id: Int, type: NotificationType) {
        // Intent khi click vào thông báo -> Mở màn hình danh sách thông báo
        val intent = Intent(context, HomeNotificationsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent cho nút Action: "Xem chi tiết" (Ví dụ: mở màn hình hóa đơn)
        val detailsIntent = Intent(context, MainActivity::class.java) // Thay bằng BillDetailActivity nếu có
        val detailsPendingIntent = PendingIntent.getActivity(
            context, id, detailsIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Chọn icon dựa trên loại thông báo
        val icon = when (type) {
            NotificationType.ELECTRIC -> android.R.drawable.ic_menu_compass // Thay bằng icon sấm sét nếu có
            NotificationType.WATER -> android.R.drawable.ic_menu_myplaces   // Thay bằng icon giọt nước nếu có
            NotificationType.WARNING -> android.R.drawable.ic_dialog_alert
            else -> android.R.drawable.ic_dialog_info
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_view, "Xem chi tiết", detailsPendingIntent) // Action Button 1

        // Nếu là cảnh báo đóng tiền, thêm nút "Đã thanh toán" (Mock action)
        if (type == NotificationType.WARNING || type == NotificationType.ELECTRIC || type == NotificationType.WATER) {
            val payIntent = Intent(context, HomeNotificationsActivity::class.java)
            val payPendingIntent = PendingIntent.getActivity(
                context, id + 1000, payIntent, PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_send, "Thanh toán ngay", payPendingIntent) // Action Button 2
        }

        notificationManager.notify(id, builder.build())
    }
}