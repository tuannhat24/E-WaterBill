package com.example.billmanager

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.util.Calendar

class NotificationSetting : AppCompatActivity() {

    lateinit var toolbar: MaterialToolbar
    lateinit var switchReminder: Switch
    lateinit var sliderDays: SeekBar
    lateinit var tvDaysBefore: TextView
    lateinit var tvTime: TextView
    lateinit var btnPickTime: ImageView
    lateinit var tvSampleNotification: TextView

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
        // Btn back
        toolbar.setNavigationOnClickListener { finish() }

        // Lắng nghe bật/tắt nhắc nhở
        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            sliderDays.isEnabled = isChecked
            btnPickTime.isEnabled = isChecked
            saveBoolean("reminderEnabled", isChecked)

            if (isChecked) {
                // Lấy giờ từ TextView tvTime
                val timeParts = tvTime.text.split(":", " ")
                var hour = timeParts[0].toInt()
                val minute = timeParts[1].toInt()
                val amPm = timeParts[2]
                if (amPm == "PM" && hour < 12) hour += 12
                if (amPm == "AM" && hour == 12) hour = 0

                ReminderScheduler.scheduleDailyReminder(this, hour, minute)
            } else {
                ReminderScheduler.cancelDailyReminder(this)
            }
        }



        // Lắng nghe thay đổi số ngày
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

        // Lắng nghe chọn giờ
        btnPickTime.setOnClickListener { showTimePicker() }

        updateSampleNotification(sliderDays.progress + 1)
    }

    //  LƯU DỮ LIỆU LOCAL
    private fun saveBoolean(key: String, value: Boolean) {
        val pref = getSharedPreferences("NotificationSetting", MODE_PRIVATE)
        pref.edit().putBoolean(key, value).apply()
    }

    private fun saveInt(key: String, value: Int) {
        val pref = getSharedPreferences("NotificationSetting", MODE_PRIVATE)
        pref.edit().putInt(key, value).apply()
    }

    private fun saveString(key: String, value: String) {
        val pref = getSharedPreferences("NotificationSetting", MODE_PRIVATE)
        pref.edit().putString(key, value).apply()
    }

    //  LOAD DỮ LIỆU LOCAL
    private fun loadSavedSettings() {
        val pref = getSharedPreferences("NotificationSetting", MODE_PRIVATE)

        val enabled = pref.getBoolean("reminderEnabled", false)
        val days = pref.getInt("daysBefore", 3)
        val time = pref.getString("reminderTime", "8:00 AM")

        // Gán lại UI
        switchReminder.isChecked = enabled
        sliderDays.progress = days - 1
        tvDaysBefore.text = days.toString()
        tvTime.text = time

        // Nếu tắt -> disable UI
        sliderDays.isEnabled = enabled
        btnPickTime.isEnabled = enabled
    }

    //  TIME PICKER
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val dialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->

                val amPm = if (selectedHour >= 12) "PM" else "AM"
                val displayHour = when {
                    selectedHour > 12 -> selectedHour - 12
                    selectedHour == 0 -> 12
                    else -> selectedHour
                }

                val timeString = String.format("%d:%02d %s", displayHour, selectedMinute, amPm)
                tvTime.text = timeString

                saveString("reminderTime", timeString)
                applyNewSchedule()
            },
            hour,
            minute,
            false
        )

        dialog.show()
    }

    private fun applyNewSchedule() {
        val pref = getSharedPreferences("NotificationSetting", MODE_PRIVATE)

        val time = pref.getString("reminderTime", "8:00 AM") ?: "8:00 AM"

        val parts = time.split(" ", ":")
        var hour = parts[0].toInt()
        val minute = parts[1].toInt()
        val amPm = parts[2]

        if (amPm == "PM" && hour != 12) hour += 12
        if (amPm == "AM" && hour == 12) hour = 0

        ReminderScheduler.scheduleDailyReminder(this, hour, minute)
    }

    private fun updateSampleNotification(days: Int) {
        val sample = "Hóa đơn điện của bạn sẽ đến hạn trong $days ngày.\nSố tiền: 450.000 VND"
        tvSampleNotification.text = sample
    }
}