package com.example.billmanager.ui.prediction

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.billmanager.R
import com.example.billmanager.ui.budget.BudgetActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.ArrayList

class PredictionActivity : AppCompatActivity() {
    private lateinit var viewModel: PredictionViewModel
    private lateinit var lineChart: LineChart
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var tvPredictedAmount: TextView
    private lateinit var tvElectricPred: TextView
    private lateinit var tvWaterPred: TextView
    private lateinit var tvComparisonMessage: TextView
    private lateinit var btnAdjustBudget: Button
    private lateinit var rgType: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction)

        viewModel = ViewModelProvider(this)[PredictionViewModel::class.java]

        setControl()
        setupChartConfig()

        viewModel.calculatePrediction()

        setEvent()
        observeData()
    }

    private fun setControl() {
        tvTitle = findViewById(R.id.tvTitle)
        tvTitle.text = "Dự Đoán Chi Tiêu Tháng Tới"

        btnBack = findViewById(R.id.btnBack)

        lineChart = findViewById(R.id.lineChartPrediction)
        tvPredictedAmount = findViewById(R.id.tvPredictedAmount)
        tvElectricPred = findViewById(R.id.tvElectricPred)
        tvWaterPred = findViewById(R.id.tvWaterPred)
        tvComparisonMessage = findViewById(R.id.tvComparisonMessage)
        btnAdjustBudget = findViewById(R.id.btnAdjustBudget)
        rgType = findViewById(R.id.rgPredictionType)
    }

    private fun setEvent() {
        btnBack.setOnClickListener { finish() }

        btnAdjustBudget.setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
            finish()
        }
    }

    private fun observeData() {
        viewModel.predictionData.observe(this) { result ->
            // 1. Hiển thị text
            tvPredictedAmount.text = "${String.format("%,.0f", result.totalPredicted)} đ"
            tvElectricPred.text = "⚡ Điện: ${String.format("%,.0f", result.electricPredicted)} đ"
            tvWaterPred.text = "💧 Nước: ${String.format("%,.0f", result.waterPredicted)} đ"

            tvComparisonMessage.text = result.message
            if (result.message.contains("VƯỢT")) {
                tvComparisonMessage.setTextColor(Color.RED)
            } else {
                tvComparisonMessage.setTextColor(Color.parseColor("#4CAF50"))
            }

            // 2. Vẽ biểu đồ
            drawChart(result.historyElectric, result.electricPredicted, result.realMonths, "Điện")

            // Sự kiện chuyển đổi
            rgType.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId == R.id.rbPredElectric) {
                    drawChart(result.historyElectric, result.electricPredicted, result.realMonths, "Điện")
                } else {
                    drawChart(result.historyWater, result.waterPredicted, result.realMonths, "Nước")
                }
            }
        }
    }

    private fun setupChartConfig() {
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f

        lineChart.axisRight.isEnabled = false
    }

    private fun drawChart(history: List<Double>, prediction: Double, labels: List<String>, type: String) {
        val entries = ArrayList<Entry>()

        // Vẽ dữ liệu lịch sử
        history.forEachIndexed { index, value ->
            entries.add(Entry(index.toFloat(), value.toFloat()))
        }

        // Vẽ điểm dự báo (Nối tiếp điểm cuối cùng)
        if (history.isNotEmpty()) {
            val nextIndex = history.size.toFloat()
            entries.add(Entry(nextIndex, prediction.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Xu hướng $type")
        dataSet.color = if (type == "Điện") Color.BLUE else Color.CYAN
        dataSet.setCircleColor(Color.RED)

        // cấu hình trục x
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels + "Dự báo")
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }
}