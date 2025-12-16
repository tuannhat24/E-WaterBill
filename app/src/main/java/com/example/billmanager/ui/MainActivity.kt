package com.example.billmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.example.billmanager.data.local.database.UsersDB
import com.example.billmanager.ui.auth.profile.ProfileActivity
import com.example.billmanager.ui.budget.BudgetActivity
import com.example.billmanager.ui.notification.NotificationsActivity
import com.example.billmanager.ui.notification.NotificationViewModel
import com.example.billmanager.ui.prediction.PredictionActivity
import com.example.billmanager.ui.settings.SettingsActivity
import com.example.billmanager.utils.ReminderReceiver
import com.example.billmanager.utils.UserSession

class MainActivity : AppCompatActivity() {

    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var tvUnreadCount: TextView
    private lateinit var tvWelcome: TextView
    lateinit var db: UsersDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        setControl()
        setEvent()
        observeNotifications()
    }

    private fun setControl() {
        tvUnreadCount = findViewById(R.id.tvUnreadCount)
        tvWelcome = findViewById(R.id.tvWelcome)
    }

    private fun setEvent() {
        db = UsersDB.getInstance(this)
        val session = UserSession(this)
        val email = session.getUserEmail()
        //lấy thông tin user theo email
        val currentUser = db.userDao().findByEmail(email)
        tvWelcome.text = "Chào mừng ${currentUser?.fullName}"
        // --- MODULE 1: PROFILE ---
        findViewById<View>(R.id.imgAvatar).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // --- MODULE 2 & 3: BILL MANAGEMENT ---
        findViewById<CardView>(R.id.cardInputBill).setOnClickListener {
            Toast.makeText(this, "Module 2: Nhập chỉ số (Nguyễn Lý Khai Tâm)", Toast.LENGTH_SHORT).show()
        }
        findViewById<CardView>(R.id.cardBillList).setOnClickListener {
            Toast.makeText(this, "Module 3: Danh sách & So sánh (Nguyễn Thành Tài)", Toast.LENGTH_SHORT).show()
        }

        // --- MODULE 5: LOCATION ---
        findViewById<CardView>(R.id.cardLocation).setOnClickListener {
            Toast.makeText(this, "Module 5: Địa điểm & Backup (Bùi Lộc Thành)", Toast.LENGTH_SHORT).show()
        }

        // --- MODULE 4: NOTIFICATION & APP CONFIG (Bùi Nhật Tuấn) ---

        // 1. Settings (Góc phải trên)
        findViewById<ImageButton>(R.id.btnOpenSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // 2. Budget
        findViewById<CardView>(R.id.cardBudget).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }

        // 3. Notifications
        findViewById<CardView>(R.id.cardNotifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        // 4. Prediction
        findViewById<CardView>(R.id.cardPrediction).setOnClickListener {
            startActivity(Intent(this, PredictionActivity::class.java))
        }

        // --- Developer Test ---
        findViewById<Button>(R.id.btnTestNotify).setOnClickListener {
            val intent = Intent(this, ReminderReceiver::class.java)
            sendBroadcast(intent)
            Toast.makeText(this, "Đã gửi Broadcast Notification!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeNotifications() {
        notificationViewModel.allNotifications.observe(this) { notifications ->
            val unreadCount = notifications.count { !it.isRead }
            if (unreadCount > 0) {
                tvUnreadCount.text = if (unreadCount > 99) "99+" else unreadCount.toString()
                tvUnreadCount.visibility = View.VISIBLE
            } else {
                tvUnreadCount.visibility = View.GONE
            }
        }
    }
}