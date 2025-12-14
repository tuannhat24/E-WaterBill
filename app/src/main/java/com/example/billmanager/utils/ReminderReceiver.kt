package com.example.billmanager.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.NotificationEntity
import com.example.billmanager.data.model.NotificationType
import com.example.billmanager.data.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val helper = NotificationHelper(context)

        // Khởi tạo Repository từ Room Database
        val database = AppDatabase.getDatabase(context)
        val repository = NotificationRepository(database.notificationDao())

        // Logic Random giả lập thông báo (Giữ lại để test)
        val random = Random().nextInt(100)

        CoroutineScope(Dispatchers.IO).launch {
            val noti: NotificationEntity
            val notiId: Int

            when {
                random < 30 -> { // 30% cơ hội ra cảnh báo Budget
                    noti = NotificationEntity(
                        title = "⚠️ Cảnh báo chi tiêu",
                        message = "Bạn đã dùng vượt 90% hạn mức Điện tháng này!",
                        type = NotificationType.WARNING,
                        timestamp = System.currentTimeMillis()
                    )
                    notiId = 1000
                }

                random < 65 -> { // 35% cơ hội ra hóa đơn Điện
                    noti = NotificationEntity(
                        title = "Hóa đơn Tiền Điện",
                        message = "Hóa đơn điện tháng này đã có. Vui lòng kiểm tra.",
                        type = NotificationType.ELECTRIC,
                        timestamp = System.currentTimeMillis()
                    )
                    notiId = 1001
                }

                else -> { // 35% cơ hội ra hóa đơn Nước
                    noti = NotificationEntity(
                        title = "Hóa đơn Tiền Nước",
                        message = "Đã có hóa đơn nước kỳ mới.",
                        type = NotificationType.WATER,
                        timestamp = System.currentTimeMillis()
                    )
                    notiId = 1002
                }
            }

            // Lưu vào Database
            repository.insert(noti)

            // Hiển thị thông báo lên thanh trạng thái
            helper.showNotification(noti.title, noti.message, notiId, noti.type)
        }
    }
}