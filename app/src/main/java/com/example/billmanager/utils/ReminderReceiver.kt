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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // 1. Kiểm tra User đang đăng nhập
        val userSession = UserSession(context)
        if (!userSession.isLoggedIn()) return
        val email = userSession.getUserEmail() ?: return

        // 2. Khởi tạo DB
        val db = AppDatabase.getInstance(context)
        val helper = NotificationHelper(context)

        CoroutineScope(Dispatchers.IO).launch {
            // 3. Lấy danh sách hóa đơn "Chưa thanh toán" của User này
            val unpaidBills = db.hoaDonDao().getBillsByUser(email).filter {
                it.trangThai == "Chưa thanh toán"
            }

            // 4. Nếu có hóa đơn chưa trả thì thông báo
            if (unpaidBills.isNotEmpty()) {
                val count = unpaidBills.size
                val totalMoney = unpaidBills.sumOf { it.tongTien }

                // Định dạng tiền tệ
                val moneyString = String.format("%,d", totalMoney)

                // Nội dung thông báo
                val title = "Nhắc nhở thanh toán"
                val message = "Bạn có $count hóa đơn chưa thanh toán. Tổng: ${moneyString}đ"
                val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

                val notiEntity = NotificationEntity(
                    title = title,
                    message = message,
                    type = "WARNING",
                    date = currentDate,
                    isRead = false,
                    userEmail = email
                )
                db.notificationDao().insert(notiEntity)

                // B. Bắn thông báo lên thanh trạng thái điện thoại
                helper.showNotification(
                    title,
                    message,
                    9999, // ID cố định cho thông báo nhắc nhở (để không bị spam nhiều dòng)
                    NotificationType.WARNING
                )
            }
        }
    }
}