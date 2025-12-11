package com.example.billmanager.ui.setting

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.billmanager.R
import com.example.billmanager.utils.ReminderScheduler
import com.google.android.material.appbar.MaterialToolbar
import java.util.Calendar

class NotificationSetting : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var switchReminder: Switch
    private lateinit var sliderDays: SeekBar
    private lateinit var tvDaysBefore: TextView
    private lateinit var tvTime: TextView
    private lateinit var btnPickTime: ImageView
    private lateinit var tvSampleNotification: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_setting)

        setControl()
        loadSavedSettings()
        setEvent()
    }

    private fun setControl() {
        toolbar = findViewById(R.id.toolbar)
        switchReminder = findViewById(R.id.switchReminder)
        sliderDays = findViewById(R.id.sliderDays)
        tvDaysBefore = findViewById(R.id.tvDaysBefore)
        tvTime = findViewById(R.id.tvTime)
        btnPickTime = findViewById(R.id.btnPickTime)
        tvSampleNotification = findViewById(R.id.tvSampleNotification)
    }

    private fun setEvent() {
        // Sự kiện nút Back
        toolbar.setNavigationOnClickListener { finish() }

        // Sự kiện Bật/Tắt nhắc nhở
        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            updateUIState(isChecked)
            saveBoolean("reminderEnabled", isChecked)

            if (isChecked) {
                // Lấy giờ hiện tại trên UI để đặt lịch
                scheduleCurrentTime()
            } else {
                ReminderScheduler.cancelDailyReminder(this)
            }
        }

        // Sự kiện thay đổi thanh trượt số ngày
        sliderDays.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val days = progress + 1
                tvDaysBefore.text = days.toString()
                updateSampleNotification(days)

                if (fromUser) saveInt("daysBefore", days)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Sự kiện chọn giờ
        btnPickTime.setOnClickListener { showTimePicker() }
    }

    // --- CÁC HÀM XỬ LÝ LOGIC RIÊNG (PRIVATE) ---

    private fun updateUIState(isEnabled: Boolean) {
        sliderDays.isEnabled = isEnabled
        btnPickTime.isEnabled = isEnabled
        // Có thể thay đổi màu sắc hoặc alpha nếu cần để người dùng biết là đang disable
        btnPickTime.alpha = if (isEnabled) 1.0f else 0.5f
    }

    private fun loadSavedSettings() {
        val pref = getSharedPreferences("NotificationSetting", Context.MODE_PRIVATE)

        val enabled = pref.getBoolean("reminderEnabled", false)
        val days = pref.getInt("daysBefore", 3)
        val time = pref.getString("reminderTime", "8:00 AM")

        // Gán dữ liệu lên View
        switchReminder.isChecked = enabled
        sliderDays.progress = days - 1
        tvDaysBefore.text = days.toString()
        tvTime.text = time

        // Update trạng thái enable/disable của các nút con
        updateUIState(enabled)

        // Update text mẫu
        updateSampleNotification(days)
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val dialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                // Format giờ hiển thị (AM/PM)
                val amPm = if (selectedHour >= 12) "PM" else "AM"
                val displayHour = when {
                    selectedHour > 12 -> selectedHour - 12
                    selectedHour == 0 -> 12
                    else -> selectedHour
                }

                val timeString = String.format("%d:%02d %s", displayHour, selectedMinute, amPm)
                tvTime.text = timeString

                // Lưu và đặt lịch lại
                saveString("reminderTime", timeString)
                scheduleCurrentTime()
            },
            currentHour,
            currentMinute,
            false
        )
        dialog.show()
    }

    private fun scheduleCurrentTime() {
        val timeStr = tvTime.text.toString() // "8:00 AM"
        try {
            // Parse chuỗi giờ để lấy hour, minute
            // Split theo dấu ":" và khoảng trắng " "
            val parts = timeStr.split(":", " ")
            if (parts.size >= 3) {
                var hour = parts[0].toInt()
                val minute = parts[1].toInt()
                val amPm = parts[2]

                if (amPm == "PM" && hour != 12) hour += 12
                if (amPm == "AM" && hour == 12) hour = 0

                ReminderScheduler.scheduleDailyReminder(this, hour, minute)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateSampleNotification(days: Int) {
        val sample = "Hóa đơn điện của bạn sẽ đến hạn trong $days ngày.\nSố tiền: 450.000 VND"
        tvSampleNotification.text = sample
    }

    // Helpers lưu SharedPreferences
    private fun saveBoolean(key: String, value: Boolean) {
        getSharedPreferences("NotificationSetting", Context.MODE_PRIVATE)
            .edit().putBoolean(key, value).apply()
    }

    private fun saveInt(key: String, value: Int) {
        getSharedPreferences("NotificationSetting", Context.MODE_PRIVATE)
            .edit().putInt(key, value).apply()
    }

    private fun saveString(key: String, value: String) {
        getSharedPreferences("NotificationSetting", Context.MODE_PRIVATE)
            .edit().putString(key, value).apply()
    }
}