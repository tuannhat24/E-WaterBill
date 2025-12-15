package com.example.billmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.ui.auth.login.LoginActivity
import com.example.billmanager.ui.auth.profile.ProfileActivity
import com.example.billmanager.ui.budget.BudgetActivity
import com.example.billmanager.ui.history_bill.BillListActivity // Mod 2: List
import com.example.billmanager.ui.input.InputBillActivity       // Mod 2: Input
import com.example.billmanager.ui.notification.NotificationsActivity
import com.example.billmanager.ui.notification.NotificationViewModel
import com.example.billmanager.ui.prediction.PredictionActivity
import com.example.billmanager.ui.settings.SettingsActivity
import com.example.billmanager.utils.ReminderReceiver
import com.example.billmanager.utils.UserSession

class MainActivity : AppCompatActivity() {

    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var userSession: UserSession
    private lateinit var db: AppDatabase

    // Controls
    private lateinit var imgAvatar: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserDesc: TextView
    private lateinit var tvUnreadCount: TextView
    private lateinit var btnOpenSettings: ImageButton

    // Cards
    private lateinit var cardInputBill: CardView
    private lateinit var cardBillList: CardView
    private lateinit var cardBudget: CardView
    private lateinit var cardNotifications: CardView
    private lateinit var cardPrediction: CardView
    private lateinit var cardLocation: CardView
    private lateinit var btnTestNotify: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Init Core
        db = AppDatabase.getInstance(this)
        userSession = UserSession(this)
        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        // Check Login (Module 1)
        if (!userSession.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setControl()
        setEvent()
        loadUserProfile()
        observeNotifications()
    }

    override fun onResume() {
        super.onResume()
        // Load lại profile mỗi khi quay lại
        if (userSession.isLoggedIn()) {
            loadUserProfile()
        }
    }

    private fun setControl() {
        imgAvatar = findViewById(R.id.imgAvatar)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserDesc = findViewById(R.id.tvUserDesc)
        tvUnreadCount = findViewById(R.id.tvUnreadCount)
        btnOpenSettings = findViewById(R.id.btnOpenSettings)

        cardInputBill = findViewById(R.id.cardInputBill)
        cardBillList = findViewById(R.id.cardBillList)
        cardBudget = findViewById(R.id.cardBudget)
        cardNotifications = findViewById(R.id.cardNotifications)
        cardPrediction = findViewById(R.id.cardPrediction)
        cardLocation = findViewById(R.id.cardLocation)
        btnTestNotify = findViewById(R.id.btnTestNotify)
    }

    // Load dữ liệu từ Module 1 hiển thị lên Dashboard
    private fun loadUserProfile() {
        val email = userSession.getUserEmail()
        val user = db.userDao().findByEmail(email)

        if (user != null) {
            tvUserName.text = "Xin chào, ${user.fullName}!"
            tvUserDesc.text = user.email
        } else {
            tvUserName.text = "Xin chào!"
        }
    }

    private fun setEvent() {
        // --- MODULE 1: PROFILE ---
        imgAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // --- MODULE 2: BILL MANAGEMENT ---
        cardInputBill.setOnClickListener {
            startActivity(Intent(this, InputBillActivity::class.java))
        }
        cardBillList.setOnClickListener {
            startActivity(Intent(this, BillListActivity::class.java))
        }

        // --- MODULE 5: LOCATION ---
        cardLocation.setOnClickListener {
            Toast.makeText(this, "Module 5: Địa điểm & Backup (Đang phát triển)", Toast.LENGTH_SHORT).show()
        }

        // --- MODULE 4: NOTIFICATION & BUDGET ---
        btnOpenSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        cardBudget.setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }
        cardNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        cardPrediction.setOnClickListener {
            startActivity(Intent(this, PredictionActivity::class.java))
        }

        // Developer Test
        btnTestNotify.setOnClickListener {
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