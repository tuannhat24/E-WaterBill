package com.example.billmanager.ui.notification

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.google.android.material.snackbar.Snackbar

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

        // Khởi tạo ViewModel
        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        setControl()
        setEvent()
    }

    private fun setControl() {
        recyclerView = findViewById(R.id.recyclerViewNotifications)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        adapter = NotificationAdapter(emptyList()) { notification ->
            if (!notification.isRead) {
                val updated = notification.copy(isRead = true)
                notificationViewModel.update(updated)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        tvTitle = findViewById(R.id.tvTitle)
        tvTitle.text = "Thông báo"
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setEvent() {
        // Quan sát dữ liệu từ Room Database
        notificationViewModel.allNotifications.observe(this) { notifications ->
            adapter.setNotifications(notifications)

            // Ẩn/Hiện Empty State
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

        setupSwipeToDelete()
    }

    private fun setupSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val notificationToDelete = adapter.getNotificationAt(position)

                // Xóa khỏi Database
                notificationViewModel.delete(notificationToDelete)

                Snackbar.make(recyclerView, "Đã xóa thông báo", Snackbar.LENGTH_LONG)
                    .setAction("Hoàn tác") {
                        notificationViewModel.insert(notificationToDelete)
                    }.show()
            }

            override fun onChildDraw(
                c: Canvas,
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                action: Int,
                active: Boolean
            ) {
                val background = ColorDrawable(Color.RED)
                val itemView = vh.itemView
                if (dX < 0) { // Chỉ vẽ khi vuốt trái
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                } else {
                    background.setBounds(0, 0, 0, 0)
                }
                background.draw(c)
                super.onChildDraw(c, rv, vh, dX, dY, action, active)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }
}