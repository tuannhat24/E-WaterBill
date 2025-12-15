package com.example.billmanager.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.billmanager.R
import com.example.billmanager.data.local.datastore.AppDataStore
import com.example.billmanager.ui.setting.NotificationSetting
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var appDataStore: AppDataStore

    // Controls
    private lateinit var btnNavNotification: LinearLayout
    private lateinit var btnNavBudget: LinearLayout
    private lateinit var btnClearData: Button
    private lateinit var tvVersion: TextView
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        appDataStore = AppDataStore(this)

        setControl()
        setEvent()
    }

    private fun setControl() {
        btnNavNotification = findViewById(R.id.btnNavNotificationSettings)
        btnNavBudget = findViewById(R.id.btnNavBudgetSettings)
        btnClearData = findViewById(R.id.btnClearData)
        tvVersion = findViewById(R.id.tvVersion)
        tvTitle = findViewById(R.id.tvTitle)
        tvTitle.text = "Cài đặt"
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setEvent() {
        btnBack.setOnClickListener {
            finish()
        }

        // Chuyển sang màn hình NotificationSetting
        btnNavNotification.setOnClickListener {
            val intent = Intent(this, NotificationSetting::class.java)
            startActivity(intent)
        }

        // Chuyển sang màn hình Budget Settings
        btnNavBudget.setOnClickListener {
            // Ví dụ: Mở lại BudgetActivity hoặc 1 màn hình cài đặt budget riêng
            // val intent = Intent(this, BudgetSettingActivity::class.java)
            // startActivity(intent)
            Toast.makeText(this, "Tính năng đang cập nhật", Toast.LENGTH_SHORT).show()
        }

        // Xóa dữ liệu (Reset)
        btnClearData.setOnClickListener {
            lifecycleScope.launch {
                // TODO: Gọi lệnh xóa database của Notification và Budget
                // database.clearAllTables()
                Toast.makeText(this@SettingsActivity, "Đã reset dữ liệu (demo)", Toast.LENGTH_SHORT).show()
            }
        }
    }
}