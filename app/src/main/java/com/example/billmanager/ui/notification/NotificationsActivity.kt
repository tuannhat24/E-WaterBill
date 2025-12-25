package com.example.billmanager.ui.notification

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R

class NotificationsActivity : AppCompatActivity() {

    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var adapter: NotificationAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        setControl()
        setEvent()
    }

    override fun onResume() {
        super.onResume()
        // Gọi load lại dữ liệu khi vào màn hình
        notificationViewModel.loadNotifications()
    }

    private fun setControl() {
        recyclerView = findViewById(R.id.recyclerViewNotifications)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        adapter = NotificationAdapter(emptyList()) { notification ->
            if (!notification.isRead) {
                // Đánh dấu đã đọc
                notificationViewModel.markAsRead(notification)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        tvTitle = findViewById(R.id.tvTitle)
        tvTitle.text = "Thông báo"
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setEvent() {
        notificationViewModel.notifications.observe(this) { notifications ->
            adapter.setNotifications(notifications)

            if (notifications.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyStateLayout.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyStateLayout.visibility = View.GONE
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}