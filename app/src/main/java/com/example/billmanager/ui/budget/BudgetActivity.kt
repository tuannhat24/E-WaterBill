package com.example.billmanager.ui.budget

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.utils.BudgetUtils
import com.example.billmanager.utils.UserSession
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class BudgetActivity : AppCompatActivity() {
    private lateinit var viewModel: BudgetViewModel

    // Controls
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

    // Control mới cho bảng lịch sử
    private lateinit var rvHistory: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        viewModel = ViewModelProvider(this)[BudgetViewModel::class.java]

        setControl()
        setEvent()
        observeData()

        // Tải bảng lịch sử ngay khi mở
        loadHistoryTable()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRealUsage()
        loadHistoryTable() // Refresh bảng khi quay lại
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

        // Ánh xạ RecyclerView lịch sử
        rvHistory = findViewById(R.id.rvBudgetHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)
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
        viewModel.spentElectric.observe(this) { spent ->
            val budget = viewModel.getBudgetForElectric().value
            val limit = budget?.amountLimit ?: 0.0
            if (budget != null) edtElectric.setText(String.format("%.0f", limit))

            updateProgressUI(
                1,
                spent,
                limit,
                progressElectric,
                tvElectricStatus,
                tvElectricAlert,
                tvElectricAdvice
            )
        }

        viewModel.getBudgetForElectric().observe(this) { budget ->
            val limit = budget?.amountLimit ?: 0.0
            val spent = viewModel.spentElectric.value ?: 0.0
            if (budget != null) edtElectric.setText(String.format("%.0f", limit))

            updateProgressUI(
                1,
                spent,
                limit,
                progressElectric,
                tvElectricStatus,
                tvElectricAlert,
                tvElectricAdvice
            )
        }

        viewModel.spentWater.observe(this) { spent ->
            val budget = viewModel.getBudgetForWater().value
            val limit = budget?.amountLimit ?: 0.0
            if (budget != null) edtWater.setText(String.format("%.0f", limit))

            updateProgressUI(
                2,
                spent,
                limit,
                progressWater,
                tvWaterStatus,
                tvWaterAlert,
                tvWaterAdvice
            )
        }

        viewModel.getBudgetForWater().observe(this) { budget ->
            val limit = budget?.amountLimit ?: 0.0
            val spent = viewModel.spentWater.value ?: 0.0
            if (budget != null) edtWater.setText(String.format("%.0f", limit))

            updateProgressUI(
                2,
                spent,
                limit,
                progressWater,
                tvWaterStatus,
                tvWaterAlert,
                tvWaterAdvice
            )
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

        tvStatus.text =
            "Đã dùng: ${String.format("%,.0f", currentUsed)}đ / ${String.format("%,.0f", limit)}đ"
        tvAlert.text = BudgetUtils.getAlertMessage(progress, limit, typeName)
        tvAlert.setTextColor(color)

        progressBar.progress = progress
        progressBar.progressTintList = ColorStateList.valueOf(color)

        val adviceText = BudgetUtils.getAdvice(progress, type)
        if (adviceText.isNotEmpty()) {
            tvAdvice.text = adviceText
            tvAdvice.visibility = View.VISIBLE
        } else {
            tvAdvice.visibility = View.GONE
        }
    }

    private fun loadHistoryTable() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(this@BudgetActivity)
            val session = UserSession(this@BudgetActivity)
            val email = session.getUserEmail() ?: ""

            // Lấy hóa đơn của User
            val allBills = db.hoaDonDao().getBillsByUser(email)

            // Gom nhóm theo tháng (Key: "12/2025")
            val grouped = allBills
                .groupBy { "${it.thang}/${it.nam}" }
                .map { (key, bills) ->
                    val dien = bills.filter { it.loai == "Điện" }.sumOf { it.tongTien }
                    val nuoc = bills.filter { it.loai == "Nước" }.sumOf { it.tongTien }
                    // Logic sort để tháng mới nhất lên đầu
                    val parts = key.split("/")
                    val sortKey = parts[1].toInt() * 100 + parts[0].toInt()
                    Triple(sortKey, key, HistoryItem(key, dien, nuoc))
                }
                .sortedByDescending { it.first } // Sắp xếp giảm dần theo thời gian
                .take(6) // Lấy 6 tháng gần nhất
                .map { it.third }

            withContext(Dispatchers.Main) {
                rvHistory.adapter = BudgetHistoryAdapter(grouped)
            }
        }
    }
}
