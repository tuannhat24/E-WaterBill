package com.example.billmanager.ui.prediction

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
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

    // Controls
    private lateinit var lineChart: LineChart
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var tvPredictedAmount: TextView
    private lateinit var tvElectricPred: TextView
    private lateinit var tvWaterPred: TextView
    private lateinit var tvComparisonMessage: TextView
    private lateinit var btnAdjustBudget: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction)

        // Init ViewModel
        viewModel = ViewModelProvider(this)[PredictionViewModel::class.java]

        setControl()
        setupChartConfig()

        // Gọi hàm tính toán
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

            // 2. Vẽ biểu đồ (Vẽ đường tiền Điện làm mẫu chính)
            updateChart(result.historyElectric, result.electricPredicted)
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

    private fun updateChart(history: List<Double>, prediction: Double) {
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        // 1. Dữ liệu Lịch sử
        history.forEachIndexed { index, value ->
            entries.add(Entry(index.toFloat(), value.toFloat()))
            labels.add("T${index + 1}") // Nhãn giả lập T1, T2... (Cần logic lấy tháng thật nếu muốn xịn hơn)
        }

        // 2. Dữ liệu Dự báo (Điểm cuối cùng)
        val nextIndex = history.size
        entries.add(Entry(nextIndex.toFloat(), prediction.toFloat()))
        labels.add("Dự báo")

        // Tạo Dataset
        val dataSet = LineDataSet(entries, "Xu hướng Điện (VNĐ)")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setCircleColor(Color.BLUE)

        // Highlight điểm dự báo (Optional)
        // dataSet.circleColors = ... (Logic nâng cao để đổi màu điểm cuối)

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Cập nhật nhãn trục X
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        lineChart.animateX(1000)
        lineChart.invalidate() // Refresh
    }
}