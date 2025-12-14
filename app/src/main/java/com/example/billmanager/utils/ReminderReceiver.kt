package com.example.billmanager.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.NotificationEntity
import com.example.billmanager.data.model.NotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val helper = NotificationHelper(context)
        val database = AppDatabase.getDatabase(context)
        val repository = com.example.billmanager.data.repository.NotificationRepository(database.notificationDao())

        // MOCK LOGIC: Giả lập sinh thông báo Điện/Nước
        val random = Random()
        val showElectric = random.nextBoolean()

        // Cần chạy coroutine vì thao tác DB không được chạy trên Main Thread
        CoroutineScope(Dispatchers.IO).launch {
            if (showElectric) {
                val noti = NotificationEntity(
                    title = "Hóa đơn Tiền Điện",
                    message = "Hóa đơn điện tháng này sắp hết hạn.",
                    type = NotificationType.ELECTRIC,
                    timestamp = System.currentTimeMillis()
                )
                repository.insert(noti) // Lưu vào Room

                // Show Push Notification
                helper.showNotification(noti.title, noti.message, 1001, NotificationType.ELECTRIC)
            } else {
                val noti = NotificationEntity(
                    title = "Hóa đơn Tiền Nước",
                    message = "Đã có hóa đơn nước kỳ mới.",
                    type = NotificationType.WATER,
                    timestamp = System.currentTimeMillis()
                )
                repository.insert(noti)
                helper.showNotification(noti.title, noti.message, 1002, NotificationType.WATER)
            }
        }
    }

    // Hàm phụ trợ để lưu thông báo mới sinh ra vào SharedPreferences (để list cập nhật)
//    private fun saveNotificationToStorage(context: Context, notification: Notification) {
//        val sharedPref = context.getSharedPreferences("NotificationData", Context.MODE_PRIVATE)
//        val gson = Gson()
//        val json = sharedPref.getString("notifications", null)
//        val type = object : TypeToken<MutableList<Notification>>() {}.type
//
//        val list: MutableList<Notification> = if (json != null) {
//            gson.fromJson(json, type)
//        } else {
//            mutableListOf()
//        }
//
//        list.add(0, notification) // Thêm vào đầu danh sách
//
//        // Giới hạn lưu 20 thông báo gần nhất để tránh nặng máy
//        if (list.size > 20) list.removeAt(list.size - 1)
//
//        sharedPref.edit().putString("notifications", gson.toJson(list)).apply()
//    }
}