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
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.datastore.AppDataStore
import com.example.billmanager.ui.auth.login.LoginActivity
import com.example.billmanager.ui.auth.profile.ProfileActivity
import com.example.billmanager.ui.budget.BudgetActivity
import com.example.billmanager.ui.history_bill.BillListActivity // Mod 3: Danh sách & Phân tích
import com.example.billmanager.ui.input.InputBillActivity       // Mod 2: Nhập liệu
import com.example.billmanager.ui.location.LocationActivity
import com.example.billmanager.ui.notification.NotificationsActivity
import com.example.billmanager.ui.notification.NotificationViewModel
import com.example.billmanager.ui.prediction.PredictionActivity
import com.example.billmanager.ui.settings.SettingsActivity
import com.example.billmanager.utils.ReminderReceiver
import com.example.billmanager.utils.UserSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var userSession: UserSession
    private lateinit var db: AppDatabase
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

    // Test
    private lateinit var btnTestNotify: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataStore = AppDataStore(this)
        // Chạy coroutine trên lifecycleScope của Activity để lấy setting
        lifecycleScope.launch {
            val isDark = dataStore.darkModeFlow.first()
            val mode = if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

            // Chỉ set nếu chế độ hiện tại khác với chế độ đã lưu (để tránh nháy màn hình)
            if (AppCompatDelegate.getDefaultNightMode() != mode) {
                AppCompatDelegate.setDefaultNightMode(mode)
            }
        }
        setContentView(R.layout.activity_main)

        // 1. Khởi tạo DB & Session
        db = AppDatabase.getInstance(this)
        userSession = UserSession(this)
        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        // 2. Kiểm tra Login (Module 1)
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
        // Load lại dữ liệu mỗi khi quay lại màn hình chính
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

    private fun loadUserProfile() {
        val email = userSession.getUserEmail()
        // Lưu ý: findByEmail chạy trên Main Thread vì trong AppDatabase đã có allowMainThreadQueries()
        val user = db.userDao().findByEmail(email)

        if (user != null) {
            tvUserName.text = "Xin chào, ${user.fullName}!"
            tvUserDesc.text = user.email
        } else {
            tvUserName.text = "Xin chào!"
            tvUserDesc.text = email ?: "Khách"
        }
    }

    private fun setEvent() {
        // --- MODULE 1: PROFILE ---
        imgAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // --- MODULE 2: BILL MANAGER ---
        cardInputBill.setOnClickListener {
            startActivity(Intent(this, InputBillActivity::class.java))
        }

        // --- MODULE 3: BILL DISPLAY ANALYTICS ---
        cardBillList.setOnClickListener {
            startActivity(Intent(this, BillListActivity::class.java))
        }

        // --- MODULE 5: LOCATION ---
        cardLocation.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }

        // --- MODULE 4: NOTIFICATION & BUDGET & PREDICTION ---
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

        // Developer Test Button
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