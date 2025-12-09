package com.example.billmanager

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HomeNotificationsActivity : AppCompatActivity() {

    lateinit var recyclerViewNotifications: RecyclerView
    lateinit var emptyStateLayout: LinearLayout
    lateinit var btnSettings: ImageButton
    lateinit var btnMarkAllRead: ImageButton
    lateinit var notificationAdapter: NotificationAdapter

    val notifications = mutableListOf<Notification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_notifications)

        setControl()
        setEvent()
    }

    private fun setControl() {
        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        btnSettings = findViewById(R.id.btnSettings)
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead)
    }

    private fun setEvent() {
        // Load data
        loadNotificationsFromStorage()
        if (notifications.isEmpty()) addSampleNotifications()

        // Init Adapter
        notificationAdapter = NotificationAdapter(notifications) { notification ->
            if (!notification.isRead) {
                notification.isRead = true
                saveNotificationsToStorage()
                notificationAdapter.notifyDataSetChanged()
            }
            // Logic mở chi tiết hóa đơn có thể đặt ở đây
        }

        recyclerViewNotifications.layoutManager = LinearLayoutManager(this)
        recyclerViewNotifications.adapter = notificationAdapter

        // SETUP SWIPE TO DELETE (VUỐT ĐỂ XÓA)
        setupSwipeToDelete()

        updateUI()

        btnSettings.setOnClickListener {
            startActivity(Intent(this, NotificationSetting::class.java))
        }

        btnMarkAllRead.setOnClickListener {
            markAllAsRead()
        }
    }

    // Cấu hình vuốt trái/phải để xóa
    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedItem = notifications[position]

                // Xóa khỏi list và update UI
                notifications.removeAt(position)
                notificationAdapter.notifyItemRemoved(position)
                saveNotificationsToStorage()
                updateUI()

                // Hiện Snackbar cho phép hoàn tác
                Snackbar.make(recyclerViewNotifications, "Đã xóa thông báo", Snackbar.LENGTH_LONG)
                    .setAction("Hoàn tác") {
                        notifications.add(position, deletedItem)
                        notificationAdapter.notifyItemInserted(position)
                        saveNotificationsToStorage()
                        updateUI()
                    }.show()
            }

            // Vẽ nền màu đỏ khi vuốt
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val background = ColorDrawable(Color.RED)
                val itemView = viewHolder.itemView

                if (dX > 0) { // Vuốt phải
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                } else if (dX < 0) { // Vuốt trái
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                } else {
                    background.setBounds(0, 0, 0, 0)
                }
                background.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerViewNotifications)
    }

    private fun markAllAsRead() {
        var hasUnread = false
        notifications.forEach {
            if(!it.isRead) {
                it.isRead = true
                hasUnread = true
            }
        }
        if(hasUnread){
            saveNotificationsToStorage()
            notificationAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addSampleNotifications() {
        // Dữ liệu mẫu chuẩn hóa đơn Điện/Nước
        notifications.add(Notification(
            id = 1, type = NotificationType.WARNING,
            title = "Cảnh báo cắt điện",
            message = "Hóa đơn điện tháng 10 quá hạn 5 ngày. Vui lòng thanh toán ngay.",
            time = "5 phút trước", isRead = false
        ))
        notifications.add(Notification(
            id = 2, type = NotificationType.WATER,
            title = "Hóa đơn Nước (Kỳ 10/2025)",
            message = "Tổng tiền: 85.000đ. Hạn thanh toán: 20/10/2025",
            time = "1 giờ trước", isRead = false
        ))
        notifications.add(Notification(
            id = 3, type = NotificationType.ELECTRIC,
            title = "Hóa đơn Điện (Kỳ 10/2025)",
            message = "Tổng tiền: 1.250.000đ. Hạn thanh toán: 15/10/2025",
            time = "2 giờ trước", isRead = false
        ))
        saveNotificationsToStorage()
    }

    private fun updateUI() {
        if (notifications.isEmpty()) {
            recyclerViewNotifications.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            recyclerViewNotifications.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
        }
    }

    private fun saveNotificationsToStorage() {
        val sharedPref = getSharedPreferences("NotificationData", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(notifications)
        editor.putString("notifications", json)
        editor.apply()
    }

    private fun loadNotificationsFromStorage() {
        val sharedPref = getSharedPreferences("NotificationData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPref.getString("notifications", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Notification>>() {}.type
            val loadedList: MutableList<Notification> = gson.fromJson(json, type)
            notifications.clear()
            notifications.addAll(loadedList)
            // Sắp xếp: Mới nhất lên đầu
            notifications.sortByDescending { it.timestamp }
        }
    }

    // Hàm test nhanh notification (được gọi từ onCreate cũ - có thể bỏ hoặc giữ để test)
    fun testNotificationNow() {
        val notiHelper = NotificationHelper(this)
        notiHelper.showNotification("Test Hệ Thống", "Đây là thông báo test", 999, NotificationType.SUCCESS)
    }

    override fun onResume() {
        super.onResume()
        loadNotificationsFromStorage()
        notificationAdapter.notifyDataSetChanged()
        updateUI()

        // Xóa badge count trên icon app (nếu có logic xử lý badge)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}