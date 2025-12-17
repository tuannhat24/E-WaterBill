package com.example.billmanager.ui.budget

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.repository.BudgetRepository
import com.example.billmanager.utils.BudgetUtils
import com.google.android.material.textfield.TextInputEditText

class BudgetActivity : AppCompatActivity() {
    private lateinit var viewModel: BudgetViewModel
    private lateinit var edtElectric: TextInputEditText
    private lateinit var edtWater: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var progressElectric: ProgressBar
    private lateinit var progressWater: ProgressBar
    private lateinit var tvElectricStatus: TextView
    private lateinit var tvWaterStatus: TextView
    private lateinit var tvElectricAlert: TextView
    private lateinit var tvWaterAlert: TextView
    private lateinit var tvElectricAdvice: TextView
    private lateinit var tvWaterAdvice: TextView
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        viewModel = ViewModelProvider(this)[BudgetViewModel::class.java]

        setControl()
        setEvent()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRealUsage()
    }

    private fun setControl() {
        edtElectric = findViewById(R.id.edtElectricBudget)
        edtWater = findViewById(R.id.edtWaterBudget)
        btnSave = findViewById(R.id.btnSaveBudget)
        progressElectric = findViewById(R.id.progressElectric)
        progressWater = findViewById(R.id.progressWater)
        tvElectricStatus = findViewById(R.id.tvElectricStatus)
        tvWaterStatus = findViewById(R.id.tvWaterStatus)
        tvElectricAlert = findViewById(R.id.tvElectricAlert)
        tvWaterAlert = findViewById(R.id.tvWaterAlert)
        tvElectricAdvice = findViewById(R.id.tvElectricAdvice)
        tvWaterAdvice = findViewById(R.id.tvWaterAdvice)
        tvTitle = findViewById(R.id.tvTitle)
        tvTitle.text = "Quản lý hạn mức"
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setEvent() {
        btnSave.setOnClickListener {
            val electricLimit = edtElectric.text.toString().toDoubleOrNull() ?: 0.0
            val waterLimit = edtWater.text.toString().toDoubleOrNull() ?: 0.0

            if (electricLimit > 0) viewModel.saveBudget(1, electricLimit)
            if (waterLimit > 0) viewModel.saveBudget(2, waterLimit)

            Toast.makeText(this, "Đã lưu hạn mức chi tiêu!", Toast.LENGTH_SHORT).show()
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun observeData() {
        // 1. Quan sát Tiền Điện Thực Tế
        viewModel.spentElectric.observe(this) { spent ->
            // Lấy Budget Điện để so sánh
            val budget = viewModel.getBudgetForElectric().value
            val limit = budget?.amountLimit ?: 0.0
            if (budget != null) edtElectric.setText(String.format("%.0f", limit))

            updateProgressUI(1, spent, limit, progressElectric, tvElectricStatus, tvElectricAlert, tvElectricAdvice)
        }

        // Khi Budget Điện thay đổi (User vừa lưu xong), cập nhật lại UI
        viewModel.getBudgetForElectric().observe(this) { budget ->
            val limit = budget?.amountLimit ?: 0.0
            val spent = viewModel.spentElectric.value ?: 0.0
            if (budget != null) edtElectric.setText(String.format("%.0f", limit))

            updateProgressUI(1, spent, limit, progressElectric, tvElectricStatus, tvElectricAlert, tvElectricAdvice)
        }

        // 2. Quan sát Tiền Nước Thực Tế
        viewModel.spentWater.observe(this) { spent ->
            val budget = viewModel.getBudgetForWater().value
            val limit = budget?.amountLimit ?: 0.0
            if (budget != null) edtWater.setText(String.format("%.0f", limit))

            updateProgressUI(2, spent, limit, progressWater, tvWaterStatus, tvWaterAlert, tvWaterAdvice)
        }

        // Khi Budget Nước thay đổi
        viewModel.getBudgetForWater().observe(this) { budget ->
            val limit = budget?.amountLimit ?: 0.0
            val spent = viewModel.spentWater.value ?: 0.0
            if (budget != null) edtWater.setText(String.format("%.0f", limit))

            updateProgressUI(2, spent, limit, progressWater, tvWaterStatus, tvWaterAlert, tvWaterAdvice)
        }
    }

    private fun updateProgressUI(
        type: Int,
        currentUsed: Double,
        limit: Double,
        progressBar: ProgressBar,
        tvStatus: TextView,
        tvAlert: TextView,
        tvAdvice: TextView
    ) {
        val progress = BudgetUtils.calculateProgress(currentUsed, limit)
        val color = BudgetUtils.getProgressColor(progress)
        val typeName = if (type == 1) "Điện" else "Nước"

        tvStatus.text = "Đã dùng: ${String.format("%,.0f", currentUsed)}đ / ${String.format("%,.0f", limit)}đ"
        tvAlert.text = BudgetUtils.getAlertMessage(progress, limit, typeName)
        tvAlert.setTextColor(color)

        progressBar.progress = progress
        progressBar.progressTintList = ColorStateList.valueOf(color)

        // Hiển thị lời khuyên
        val adviceText = BudgetUtils.getAdvice(progress, type)
        if (adviceText.isNotEmpty()) {
            tvAdvice.text = adviceText
            tvAdvice.visibility = android.view.View.VISIBLE
        } else {
            tvAdvice.visibility = android.view.View.GONE
        }
    }
}