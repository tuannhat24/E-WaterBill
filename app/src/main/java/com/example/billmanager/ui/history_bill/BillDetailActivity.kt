package com.example.billmanager.ui.history_bill

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.example.billmanager.data.repository.HoaDonRepository
import com.example.billmanager.ui.input.InputBillActivity
import com.example.billmanager.utils.BillCalculator
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class BillDetailActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var tvBillTitle: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvOldIndex: TextView
    private lateinit var tvNewIndex: TextView
    private lateinit var tvUsage: TextView
    private lateinit var tvBreakdown: TextView
    private lateinit var tvComparison: TextView
    private lateinit var btnMarkPaid: Button
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button
    private lateinit var btnShare: Button
    private lateinit var currentBill: HoaDonEntity
    private lateinit var repository: HoaDonRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_detail)

        // Lấy dữ liệu từ Intent
        if (intent.hasExtra("BILL_DATA")) {
            currentBill = intent.getSerializableExtra("BILL_DATA") as HoaDonEntity
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy dữ liệu hóa đơn", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = AppDatabase.getInstance(this)
        repository = HoaDonRepository(db.hoaDonDao())

        setControl()
        loadData()
        setEvent()
    }

    private fun setControl() {
        tvTitle = findViewById(R.id.tvTitle)
        btnBack = findViewById(R.id.btnBack)
        tvBillTitle = findViewById(R.id.tvBillTitle)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvStatus = findViewById(R.id.tvStatus)
        tvOldIndex = findViewById(R.id.tvOldIndex)
        tvNewIndex = findViewById(R.id.tvNewIndex)
        tvUsage = findViewById(R.id.tvUsage)
        tvBreakdown = findViewById(R.id.tvBreakdown)
        tvComparison = findViewById(R.id.tvComparison)
        btnMarkPaid = findViewById(R.id.btnMarkPaid)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)
        btnShare = findViewById(R.id.btnShare)
    }

    private fun setEvent() {
        // Back
        btnBack.setOnClickListener { finish() }

        // Thanh toán
        btnMarkPaid.setOnClickListener {
            currentBill.trangThai = "Đã thanh toán"
            repository.update(currentBill)
            Toast.makeText(this, "Đã cập nhật trạng thái thanh toán!", Toast.LENGTH_SHORT).show()
            loadData() // Refresh lại giao diện (ẩn nút thanh toán)
        }

        // Xóa
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Xóa hóa đơn?")
                .setMessage("Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa") { _, _ ->
                    repository.delete(currentBill)
                    Toast.makeText(this, "Đã xóa thành công", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        // Sửa
        btnEdit.setOnClickListener {
            val intent = Intent(this, InputBillActivity::class.java)
            intent.putExtra("BILL_EDIT", currentBill)
            startActivity(intent)
            finish()
        }

        // Chia sẻ
        btnShare.setOnClickListener {
            val shareText =
                "Hóa đơn ${currentBill.loai} T${currentBill.thang}/${currentBill.nam}\n" +
                        "Tổng tiền: ${tvTotalAmount.text}\n" +
                        "Trạng thái: ${currentBill.trangThai}"
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, shareText)
            startActivity(Intent.createChooser(intent, "Chia sẻ qua"))
        }
    }
    private fun loadData() {
        val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        // --- CẬP NHẬT HEADER & INFO ---
        tvTitle.text = "Chi tiết hóa đơn" // Set tiêu đề Header
        tvBillTitle.text = "${currentBill.loai} - Tháng ${currentBill.thang}/${currentBill.nam}"
        tvTotalAmount.text = formatVND.format(currentBill.tongTien)
        tvStatus.text = currentBill.trangThai

        tvOldIndex.text = "Cũ: ${currentBill.chiSoDau}"
        tvNewIndex.text = "Mới: ${currentBill.chiSoCuoi}"
        val donVi = if (currentBill.loai == "Điện") "kWh" else "m³"
        tvUsage.text = "Tiêu thụ: ${currentBill.soLuong} $donVi"

        // --- HIỂN THỊ BREAKDOWN (CHI TIẾT TÍNH TIỀN) ---
        if (currentBill.loai == "Điện") {
            val details = BillCalculator.tinhTienDien(currentBill.chiSoDau, currentBill.chiSoCuoi)
            val builder = StringBuilder()
            details.chiTiet.forEach { line -> builder.append(line).append("\n") }
            builder.append("\nVAT (8%): ${formatVND.format(details.vat)}")
            tvBreakdown.text = builder.toString()
        } else {
            val details = BillCalculator.tinhTienNuoc(currentBill.chiSoDau, currentBill.chiSoCuoi)
            tvBreakdown.text = "Tiền nước: ${formatVND.format(details.tienNuoc)}\n" +
                    "Phí BVMT: ${formatVND.format(details.tienDVTN)}\n" +
                    "VAT: ${formatVND.format(details.vatNuoc + details.vatDVTN)}"
        }

        // --- SO SÁNH VỚI THÁNG TRƯỚC ---
        compareWithPreviousMonth()

        // --- TRẠNG THÁI NÚT BẤM ---
        if (currentBill.trangThai == "Đã thanh toán") {
            btnMarkPaid.visibility = View.GONE
            tvStatus.setTextColor(Color.parseColor("#4CAF50")) // Xanh lá

            // Nếu đã thanh toán thì ẩn nút Sửa (hoặc làm mờ) để tránh sai lệch
            btnEdit.isEnabled = false
            btnEdit.alpha = 0.5f
        } else {
            btnMarkPaid.visibility = View.VISIBLE
            tvStatus.setTextColor(Color.parseColor("#F44336")) // Đỏ
            btnEdit.isEnabled = true
            btnEdit.alpha = 1.0f
        }
    }

    // Logic so sánh tháng trước
    private fun compareWithPreviousMonth() {
        var prevMonth = currentBill.thang - 1
        var prevYear = currentBill.nam
        if (prevMonth == 0) {
            prevMonth = 12
            prevYear -= 1
        }

        // Gọi Repository lấy hóa đơn tháng trước
        val prevBill = repository.getBillByMonth(currentBill.loai, prevMonth, prevYear)
        val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        if (prevBill != null) {
            val diff = currentBill.tongTien - prevBill.tongTien
            val percent =
                if (prevBill.tongTien > 0) (diff.toDouble() / prevBill.tongTien) * 100 else 0.0

            val status = if (diff > 0) "TĂNG" else "GIẢM"
            val icon = if (diff > 0) "🔺" else "🔻"
            val color = if (diff > 0) "#F44336" else "#4CAF50" // Tăng thì đỏ, Giảm thì xanh

            tvComparison.text =
                "$icon $status ${String.format("%.1f", abs(percent))}% so với tháng trước\n" +
                        "(${formatVND.format(abs(diff))})"
            tvComparison.setTextColor(Color.parseColor(color))
        } else {
            tvComparison.text = "Không có dữ liệu tháng $prevMonth/$prevYear để so sánh."
            tvComparison.setTextColor(Color.GRAY)
        }
    }
}