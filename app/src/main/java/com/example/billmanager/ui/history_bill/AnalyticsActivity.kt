package com.example.billmanager.ui.history_bill

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.example.billmanager.data.repository.HoaDonRepository
import com.example.billmanager.utils.UserSession
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
    private lateinit var rgType: RadioGroup
    private lateinit var tvTotalYearElectric: TextView
    private lateinit var tvTotalYearWater: TextView
    private lateinit var tvTotalYearAll: TextView
    private lateinit var repository: HoaDonRepository
    private var listDien: List<HoaDonEntity> = listOf()
    private var listNuoc: List<HoaDonEntity> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        val db = AppDatabase.getInstance(this)
        repository = HoaDonRepository(db.hoaDonDao())

        setControl()
        setupChart()
        loadAnalyticsData()
        setEvent()
    }

    private fun setControl() {
        chart = findViewById(R.id.chartAnalytics)
        tvAvg3Months = findViewById(R.id.tvAvg3Months)
        tvMaxMonth = findViewById(R.id.tvMaxMonth)
        tvInsightText = findViewById(R.id.tvInsightText)
        tvTitle = findViewById(R.id.tvTitle)
        btnBack = findViewById(R.id.btnBack)
        rgType = findViewById(R.id.rgType)
        tvTotalYearElectric = findViewById(R.id.tvTotalYearElectric)
        tvTotalYearWater = findViewById(R.id.tvTotalYearWater)
        tvTotalYearAll = findViewById(R.id.tvTotalYearAll)

        tvTitle.text = "Phân tích & Thống kê"
    }

    private fun setEvent() {
        btnBack.setOnClickListener { finish() }

        // Sự kiện chuyển đổi nút Điện / Nước
        rgType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbElectric) {
                // Hiển thị dữ liệu Điện
                if (listDien.isNotEmpty()) {
                    updateChart(listDien, "Điện")
                    calculateInsights(listDien, "Điện")
                } else {
                    chart.clear()
                    tvInsightText.text = "Chưa có dữ liệu Điện năm nay."
                }
            } else {
                // Hiển thị dữ liệu Nước
                if (listNuoc.isNotEmpty()) {
                    updateChart(listNuoc, "Nước")
                    calculateInsights(listNuoc, "Nước")
                } else {
                    chart.clear()
                    tvInsightText.text = "Chưa có dữ liệu Nước năm nay."
                }
            }
        }
    }

    private fun loadAnalyticsData() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val userSession = UserSession(this)
        val email = userSession.getUserEmail() ?: ""

        // Lấy tất cả hóa đơn của user (đảm bảo tính bảo mật)
        val allBills = repository.getBillsByUser(email)

        // Tách list Điện và Nước theo năm hiện tại
        listDien = allBills.filter { it.loai == "Điện" && it.nam == currentYear }.sortedBy { it.thang }
        listNuoc = allBills.filter { it.loai == "Nước" && it.nam == currentYear }.sortedBy { it.thang }

        // 3. Hiển thị mặc định (Điện)
        if (listDien.isNotEmpty()) {
            updateChart(listDien, "Điện")
            calculateInsights(listDien, "Điện")
        } else {
            tvInsightText.text = "Chưa có dữ liệu hóa đơn năm $currentYear."
        }

        // 4. Cập nhật bảng Tổng kết năm
        updateYearlySummary(listDien, listNuoc)
    }

    private fun updateChart(list: List<HoaDonEntity>, type: String) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        list.forEachIndexed { index, bill ->
            entries.add(BarEntry(index.toFloat(), bill.tongTien.toFloat()))
            labels.add("T${bill.thang}")
        }

        val labelChart = if (type == "Điện") "Chi phí Điện (VNĐ)" else "Chi phí Nước (VNĐ)"
        val colorChart = if (type == "Điện") Color.parseColor("#2196F3") else Color.parseColor("#03A9F4")

        val dataSet = BarDataSet(entries, labelChart)
        dataSet.color = colorChart
        dataSet.valueTextSize = 10f

        val data = BarData(dataSet)
        chart.data = data

        // Format trục X
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.description.isEnabled = false
        chart.animateY(1000)
        chart.invalidate() // Refresh chart
    }

    private fun calculateInsights(list: List<HoaDonEntity>, type: String) {
        val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        // Trung bình 3 tháng gần nhất
        val last3 = list.sortedByDescending { it.thang }.take(3)
        val avg = if (last3.isNotEmpty()) last3.map { it.tongTien }.average() else 0.0
        tvAvg3Months.text = formatVND.format(avg)

        // Tháng cao nhất
        val maxBill = list.maxByOrNull { it.tongTien }
        if (maxBill != null) {
            tvMaxMonth.text = "T${maxBill.thang}\n${formatVND.format(maxBill.tongTien)}"
        } else {
            tvMaxMonth.text = "-"
        }

        // Insight Text
        if (list.size >= 2) {
            val last = list.last()
            val prev = list[list.size - 2]

            val trend = if (last.tongTien > prev.tongTien) "TĂNG" else "GIẢM"
            val diff = abs(last.tongTien - prev.tongTien)

            tvInsightText.text = "($type): Tháng ${last.thang} có xu hướng $trend " +
                    "${formatVND.format(diff)} so với tháng trước."
        } else if (list.isNotEmpty()) {
            tvInsightText.text = "($type): Cần thêm dữ liệu tháng sau để so sánh xu hướng."
        }
    }

    private fun updateYearlySummary(dien: List<HoaDonEntity>, nuoc: List<HoaDonEntity>) {
        val totalDien = dien.sumOf { it.tongTien }
        val totalNuoc = nuoc.sumOf { it.tongTien }
        val grandTotal = totalDien + totalNuoc

        val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        tvTotalYearElectric.text = formatVND.format(totalDien)
        tvTotalYearWater.text = formatVND.format(totalNuoc)
        tvTotalYearAll.text = formatVND.format(grandTotal)
    }

    private fun setupChart() {
        chart.setNoDataText("Vui lòng chọn loại hóa đơn để xem.")
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
    }
}