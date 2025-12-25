package com.example.billmanager.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.datastore.AppDataStore
import com.example.billmanager.ui.location.LocationActivity
import com.example.billmanager.ui.setting.NotificationSetting
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var appDataStore: AppDataStore
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnNavNotification: LinearLayout
    private lateinit var btnNavLocationBackup: LinearLayout
    private lateinit var switchDarkMode: Switch
    private lateinit var btnClearData: Button
    private lateinit var tvVersion: TextView
    private lateinit var btnNavHelp: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        appDataStore = AppDataStore(this)

        setControl()
        loadSettings()
        setEvent()
    }

    private fun setControl() {
        tvTitle = findViewById(R.id.tvTitle)
        if (::tvTitle.isInitialized) tvTitle.text = "Cài đặt"
        btnBack = findViewById(R.id.btnBack)

        btnNavNotification = findViewById(R.id.btnNavNotificationSettings)
        btnNavLocationBackup = findViewById(R.id.btnNavLocationBackup)
        switchDarkMode = findViewById(R.id.switchDarkMode)
        btnClearData = findViewById(R.id.btnClearData)
        tvVersion = findViewById(R.id.tvVersion)
        btnNavHelp = findViewById(R.id.btnNavHelp)
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            // Load trạng thái Dark Mode từ DataStore
            val isDark = appDataStore.darkModeFlow.first()
            switchDarkMode.isChecked = isDark
        }
    }

    private fun setEvent() {
        btnBack.setOnClickListener { finish() }

        btnNavNotification.setOnClickListener {
            startActivity(Intent(this, NotificationSetting::class.java))
        }

        btnNavLocationBackup.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }

        btnNavHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                // Lưu vào DataStore
                appDataStore.setDarkMode(isChecked)

                // Áp dụng chế độ ngay lập tức
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }

        btnClearData.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cảnh báo")
                .setMessage("Bạn có chắc muốn xóa TOÀN BỘ dữ liệu không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa hết") { _, _ ->
                    val db = AppDatabase.getInstance(this)
                    db.clearAllTables() // Xóa sạch dữ liệu Room
                    Toast.makeText(this, "Đã xóa dữ liệu! Hãy khởi động lại App.", Toast.LENGTH_LONG).show()
                    // Tùy chọn: Thoát app hoặc restart
                    finishAffinity()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }
}