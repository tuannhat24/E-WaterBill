package com.example.billmanager.ui.budget

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.billmanager.R
import com.example.billmanager.data.local.database.BudgetDatabase
import com.example.billmanager.data.repository.BudgetRepository
import com.example.billmanager.utils.BudgetUtils
import com.google.android.material.textfield.TextInputEditText
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import android.graphics.Color
import android.widget.ImageButton

class BudgetActivity : AppCompatActivity() {

    // Khai báo ViewModel
    private lateinit var viewModel: BudgetViewModel

    // Khai báo View Controls
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
    private lateinit var barChart: BarChart
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        val database = BudgetDatabase.getDatabase(this)
        val repository = BudgetRepository(database.budgetDao())
        val factory = BudgetViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[BudgetViewModel::class.java]

        setControl()
        setEvent()
        setupChart()
        observeData()
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
        barChart = findViewById(R.id.barChartBudget)
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

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun observeData() {
        // Quan sát Budget Điện
        viewModel.getBudgetForElectric().observe(this) { budget ->
            if (budget != null) {
                edtElectric.setText(String.format("%.0f", budget.amountLimit)) // Hiển thị số không có .0
                updateProgressUI(1, budget.amountLimit, progressElectric, tvElectricStatus, tvElectricAlert)
            }
        }

        // Quan sát Budget Nước
        viewModel.getBudgetForWater().observe(this) { budget ->
            if (budget != null) {
                edtWater.setText(String.format("%.0f", budget.amountLimit))
                updateProgressUI(2, budget.amountLimit, progressWater, tvWaterStatus, tvWaterAlert)
            }
        }
    }

    private fun updateProgressUI(
        type: Int,
        limit: Double,
        progressBar: ProgressBar,
        tvStatus: TextView,
        tvAlert: TextView
    ) {
        val currentUsed = viewModel.getCurrentUsage(type)
        val progress = BudgetUtils.calculateProgress(currentUsed, limit)
        val color = BudgetUtils.getProgressColor(progress)
        val typeName = if (type == 1) "Điện" else "Nước"

        tvStatus.text = "Đã dùng: ${String.format("%,.0f", currentUsed)}đ / ${String.format("%,.0f", limit)}đ"
        tvAlert.text = BudgetUtils.getAlertMessage(progress, limit, typeName)
        tvAlert.setTextColor(color)

        progressBar.progress = progress
        progressBar.progressTintList = ColorStateList.valueOf(color)

        // Xử lý Lời khuyên (Advice)
        //Điện (type=1), Nước (type=2)
        val tvAdvice = if (type == 1) tvElectricAdvice else tvWaterAdvice
        val adviceText = BudgetUtils.getAdvice(progress, type)

        if (adviceText.isNotEmpty()) {
            tvAdvice.text = adviceText
            tvAdvice.visibility = android.view.View.VISIBLE
        } else {
            tvAdvice.visibility = android.view.View.GONE
        }
    }

    private fun setupChart() {
        // 1. Tạo dữ liệu giả lập cho 4 tháng (Tháng 9, 10, 11, 12)
        // Cột 1: Hạn mức (Budget), Cột 2: Thực tế (Actual)
        val budgetEntries = ArrayList<BarEntry>()
        val actualEntries = ArrayList<BarEntry>()

        // Tháng 9: Đặt 500k, Dùng 480k
        budgetEntries.add(BarEntry(0f, 500000f))
        actualEntries.add(BarEntry(0f, 480000f))

        // Tháng 10: Đặt 500k, Dùng 520k (Vượt)
        budgetEntries.add(BarEntry(1f, 500000f))
        actualEntries.add(BarEntry(1f, 520000f))

        // Tháng 11: Đặt 600k, Dùng 550k
        budgetEntries.add(BarEntry(2f, 600000f))
        actualEntries.add(BarEntry(2f, 550000f))

        // Tháng 12: Đặt 600k, Dùng 610k (Vượt)
        budgetEntries.add(BarEntry(3f, 600000f))
        actualEntries.add(BarEntry(3f, 610000f))

        // 2. Tạo DataSet
        val setBudget = BarDataSet(budgetEntries, "Hạn Mức")
        setBudget.color = Color.parseColor("#4CAF50") // Màu Xanh lá
        setBudget.valueTextSize = 10f

        val setActual = BarDataSet(actualEntries, "Thực Tế")
        setActual.color = Color.parseColor("#FF9800") // Màu Cam
        setActual.valueTextSize = 10f

        // 3. Đưa vào BarData
        val data = BarData(setBudget, setActual)
        data.barWidth = 0.3f // Độ rộng cột

        // 4. Cấu hình Chart
        barChart.data = data
        barChart.description.isEnabled = false // Tắt dòng description

        // Group bars (Gom nhóm cột)
        // (fromX, groupSpace, barSpace)
        barChart.groupBars(0f, 0.4f, 0.0f)

        // Label trục X (Tháng)
        val months = arrayOf("T9", "T10", "T11", "T12")
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(months)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(true)

        // Animation
        barChart.animateY(1500)
        barChart.invalidate() // Refresh chart
    }
}