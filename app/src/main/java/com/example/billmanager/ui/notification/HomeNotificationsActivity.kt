package com.example.billmanager.ui.notification

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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.example.billmanager.data.local.entity.NotificationEntity
import com.example.billmanager.data.model.NotificationType
import com.example.billmanager.ui.setting.NotificationSetting
import com.google.android.material.snackbar.Snackbar

class HomeNotificationsActivity : AppCompatActivity() {

    // Khai báo biến
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var adapter: NotificationAdapter

    // Controls
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var btnSettings: ImageButton
    private lateinit var btnMarkAllRead: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_notifications)

        // Khởi tạo ViewModel trước
        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        setControl()
        setEvent()
    }

    private fun setControl() {
        // 1. Ánh xạ View
        recyclerView = findViewById(R.id.recyclerViewNotifications)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        btnSettings = findViewById(R.id.btnSettings)
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead)

        // 2. Cấu hình RecyclerView & Adapter
        adapter = NotificationAdapter(emptyList()) { notification ->
            // Xử lý khi click vào item: Đánh dấu đã đọc
            if (!notification.isRead) {
                val updated = notification.copy(isRead = true)
                notificationViewModel.update(updated)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setEvent() {
        // 1. Lắng nghe dữ liệu từ ViewModel
        notificationViewModel.allNotifications.observe(this) { notifications ->
            // Cập nhật dữ liệu cho Adapter
            adapter.setNotifications(notifications)

            // Xử lý hiển thị Empty State
            if (notifications.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyStateLayout.visibility = View.VISIBLE

                // (Tùy chọn) Thêm dữ liệu mẫu nếu DB trống trơn để test
                addSampleDataIfEmpty()
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyStateLayout.visibility = View.GONE
            }
        }

        // 2. Sự kiện Click nút Cài đặt
        btnSettings.setOnClickListener {
            val intent = Intent(this, NotificationSetting::class.java)
            startActivity(intent)
        }

        // 3. Sự kiện Click nút Đọc tất cả
        btnMarkAllRead.setOnClickListener {
            notificationViewModel.markAllRead()
            Toast.makeText(this, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show()
        }

        // 4. Cấu hình vuốt để xóa (Swipe to Delete)
        setupSwipeToDelete()
    }

    private fun setupSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                // Lấy item cần xóa thông qua adapter
                val notificationToDelete = adapter.getNotificationAt(position)

                // Gọi ViewModel để xóa khỏi DB
                notificationViewModel.delete(notificationToDelete)

                // Hiện Snackbar cho phép Hoàn tác (Undo)
                Snackbar.make(recyclerView, "Đã xóa thông báo", Snackbar.LENGTH_LONG)
                    .setAction("Hoàn tác") {
                        notificationViewModel.insert(notificationToDelete)
                    }.show()
            }

            // Vẽ nền đỏ khi vuốt
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

        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun addSampleDataIfEmpty() {
        // Chỉ để test: Thêm 1 thông báo chào mừng nếu chưa có gì
        val sample = NotificationEntity(
            title = "Chào mừng bạn",
            message = "Hệ thống quản lý hóa đơn đã sẵn sàng.",
            type = NotificationType.SUCCESS,
            timestamp = System.currentTimeMillis()
        )
        notificationViewModel.insert(sample)
    }
}