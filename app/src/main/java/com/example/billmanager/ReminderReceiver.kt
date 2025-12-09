package com.example.billmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Random

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val helper = NotificationHelper(context)
        val pref = context.getSharedPreferences("NotificationSetting", Context.MODE_PRIVATE)
        val daysBefore = pref.getInt("daysBefore", 3)

        val random = Random()
        val shouldShowElectric = random.nextBoolean() // Giả lập ngẫu nhiên có hóa đơn điện
        val shouldShowWater = true // Giả lập luôn có hóa đơn nước để test

        // 1. Kiểm tra và thông báo Điện
        if (shouldShowElectric) {
            val electricNoti = Notification(
                id = System.currentTimeMillis().toInt(),
                type = NotificationType.ELECTRIC,
                title = "Hóa đơn Tiền Điện",
                message = "Hóa đơn điện tháng này sắp hết hạn trong $daysBefore ngày nữa. Vui lòng thanh toán sớm.",
                time = "Ngay bây giờ",
                isRead = false
            )
            // Lưu vào storage để hiển thị trong list
            saveNotificationToStorage(context, electricNoti)

            // Bắn Push Notification
            helper.showNotification(
                title = electricNoti.title,
                message = electricNoti.message,
                id = 1001,
                type = NotificationType.ELECTRIC
            )
        }

        // 2. Kiểm tra và thông báo Nước
        if (shouldShowWater) {
            val waterNoti = Notification(
                id = System.currentTimeMillis().toInt() + 1,
                type = NotificationType.WATER,
                title = "Hóa đơn Tiền Nước",
                message = "Đã có hóa đơn nước kỳ mới. Tổng tiền: 120.000đ.",
                time = "Ngay bây giờ",
                isRead = false
            )
            saveNotificationToStorage(context, waterNoti)

            helper.showNotification(
                title = waterNoti.title,
                message = waterNoti.message,
                id = 1002,
                type = NotificationType.WATER
            )
        }
    }

    // Hàm phụ trợ để lưu thông báo mới sinh ra vào SharedPreferences (để list cập nhật)
    private fun saveNotificationToStorage(context: Context, notification: Notification) {
        val sharedPref = context.getSharedPreferences("NotificationData", Context.MODE_PRIVATE)
        val gson = com.google.gson.Gson()
        val json = sharedPref.getString("notifications", null)
        val type = object : com.google.gson.reflect.TypeToken<MutableList<Notification>>() {}.type

        val list: MutableList<Notification> = if (json != null) {
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }

        list.add(0, notification) // Thêm vào đầu danh sách

        // Giới hạn lưu 20 thông báo gần nhất để tránh nặng máy
        if (list.size > 20) list.removeAt(list.size - 1)

        sharedPref.edit().putString("notifications", gson.toJson(list)).apply()
    }
}