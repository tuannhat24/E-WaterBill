package com.example.billmanager.ui.history_bill

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.example.billmanager.data.repository.HoaDonRepository
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var chart: BarChart
    private lateinit var tvAvg3Months: TextView
    private lateinit var tvMaxMonth: TextView
    private lateinit var tvInsightText: TextView
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton

    private lateinit var repository: HoaDonRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        val db = AppDatabase.getInstance(this)
        repository = HoaDonRepository(db.hoaDonDao())

        setControl()
        setupChart()
        loadAnalyticsData()
    }

    private fun setControl() {
        chart = findViewById(R.id.chartAnalytics)
        tvAvg3Months = findViewById(R.id.tvAvg3Months)
        tvMaxMonth = findViewById(R.id.tvMaxMonth)
        tvInsightText = findViewById(R.id.tvInsightText)

        tvTitle = findViewById(R.id.tvTitle)
        tvTitle.text = "Phân tích & Thống kê"
        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }
    }

    private fun loadAnalyticsData() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // Lấy dữ liệu Điện (Mặc định phân tích Điện trước)
        val listDien = repository.getBillsByYear("Điện", currentYear)

        if (listDien.isNotEmpty()) {
            updateChart(listDien)
            calculateInsights(listDien)
        } else {
            tvInsightText.text = "Chưa có dữ liệu hóa đơn Điện năm $currentYear để phân tích."
        }
    }

    private fun updateChart(list: List<HoaDonEntity>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        list.forEachIndexed { index, bill ->
            entries.add(BarEntry(index.toFloat(), bill.tongTien.toFloat()))
            labels.add("T${bill.thang}")
        }

        val dataSet = BarDataSet(entries, "Chi phí Điện (VNĐ)")
        dataSet.color = Color.parseColor("#2196F3")
        dataSet.valueTextSize = 10f

        val data = BarData(dataSet)
        chart.data = data

        // Format trục X
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.description.isEnabled = false
        chart.animateY(1000)
        chart.invalidate()
    }

    private fun calculateInsights(list: List<HoaDonEntity>) {
        val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        // 1. Trung bình 3 tháng gần nhất
        val last3 = list.sortedByDescending { it.thang }.take(3)
        val avg = if (last3.isNotEmpty()) last3.map { it.tongTien }.average() else 0.0
        tvAvg3Months.text = formatVND.format(avg)

        // 2. Tháng cao nhất
        val maxBill = list.maxByOrNull { it.tongTien }
        if (maxBill != null) {
            tvMaxMonth.text = "T${maxBill.thang}\n${formatVND.format(maxBill.tongTien)}"
        }

        // 3. Insight Text
        if (list.size >= 2) {
            val last = list.last()
            val prev = list[list.size - 2]

            val trend = if (last.tongTien > prev.tongTien) "TĂNG" else "GIẢM"
            val diff = abs(last.tongTien - prev.tongTien)

            tvInsightText.text = "🤖 Insight: Chi phí tháng ${last.thang} có xu hướng $trend " +
                    "${formatVND.format(diff)} so với tháng trước."
        }
    }

    private fun setupChart() {
        chart.setNoDataText("Đang tải dữ liệu...")
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
    }
}