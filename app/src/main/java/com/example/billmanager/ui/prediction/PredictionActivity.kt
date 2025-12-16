package com.example.billmanager.ui.prediction

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.repository.BudgetRepository
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class PredictionActivity : AppCompatActivity() {

    private lateinit var viewModel: PredictionViewModel

    // Controls
    private lateinit var lineChart: LineChart
    private lateinit var tvPredictedAmount: TextView
    private lateinit var tvConfidence: TextView
    private lateinit var tvElectricPred: TextView
    private lateinit var tvWaterPred: TextView
    private lateinit var tvComparisonMessage: TextView
    private lateinit var tvSuggestion: TextView
    private lateinit var btnAdjust: Button
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction)

        // Init ViewModel
        val db = AppDatabase.getDatabase(this)
        val repo = BudgetRepository(db.budgetDao())
        val factory = PredictionViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[PredictionViewModel::class.java]

        setControl()
        setEvent()

        // Kích hoạt tính toán
        viewModel.calculatePrediction()

        observeData()
    }

    private fun setControl() {
        lineChart = findViewById(R.id.lineChartPrediction)
        tvPredictedAmount = findViewById(R.id.tvPredictedAmount)
        tvConfidence = findViewById(R.id.tvConfidence)
        tvElectricPred = findViewById(R.id.tvElectricPred)
        tvWaterPred = findViewById(R.id.tvWaterPred)
        tvComparisonMessage = findViewById(R.id.tvComparisonMessage)
        tvSuggestion = findViewById(R.id.tvSuggestion)
        btnAdjust = findViewById(R.id.btnAdjustBudget)
        tvTitle = findViewById(R.id.tvTitle)
        tvTitle.text = "🔮 Dự Đoán Chi Tiêu Tháng Tới"
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setEvent() {
        btnAdjust.setOnClickListener {
            Toast.makeText(this, "Điều chỉnh Hạn mức ngay", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun observeData() {
        viewModel.predictionData.observe(this) { result ->
            // 1. Hiển thị số tiền
            tvPredictedAmount.text = "${String.format("%,.0f", result.totalAmount)} đ"
            tvConfidence.text = "Độ tin cậy: ${result.confidence}"
            tvElectricPred.text = "⚡ Điện: ${String.format("%,.0f", result.electricAmount)} đ"
            tvWaterPred.text = "💧 Nước: ${String.format("%,.0f", result.waterAmount)} đ"

            // 2. So sánh với Budget
            val diff = result.totalAmount - result.budgetTotal
            if (diff > 0) {
                tvComparisonMessage.text = "⚠️ DỰ BÁO VƯỢT HẠN MỨC: ${String.format("%,.0f", diff)} đ"
                tvComparisonMessage.setTextColor(Color.RED)
                tvSuggestion.text = "Gợi ý: Cần cắt giảm ngay hoặc tăng hạn mức ngân sách!"
            } else {
                tvComparisonMessage.text = "✅ Dự báo nằm trong hạn mức"
                tvComparisonMessage.setTextColor(Color.parseColor("#4CAF50"))
                tvSuggestion.text = "Tuyệt vời! Hãy duy trì mức sử dụng này."
            }

            // 3. Vẽ biểu đồ
            setupChart(result.historyElectric, result.electricAmount)
        }
    }

    private fun setupChart(history: List<Double>, prediction: Double) {
        val entries = ArrayList<Entry>()

        // Thêm dữ liệu lịch sử
        history.forEachIndexed { index, value ->
            entries.add(Entry(index.toFloat(), value.toFloat()))
        }

        // Thêm điểm dự đoán (tháng tiếp theo)
        val nextMonthIndex = history.size.toFloat()
        entries.add(Entry(nextMonthIndex, prediction.toFloat()))

        val dataSet = LineDataSet(entries, "Xu hướng Tiền Điện")
        dataSet.color = Color.BLUE
        dataSet.valueTextSize = 12f
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f

        // Làm nét đứt cho đoạn dự đoán (Optional - logic nâng cao: vẽ 2 đường, 1 liền, 1 đứt)
        // Ở đây vẽ đơn giản 1 đường liền trước.

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Format trục X
        val months = arrayOf("T1", "T2", "T3", "T4", "T5", "T6(Dự báo)") // Fake label
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(months)
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.granularity = 1f

        lineChart.description.isEnabled = false
        lineChart.animateX(1000)
        lineChart.invalidate()
    }
}