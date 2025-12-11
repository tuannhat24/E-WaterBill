package com.example.billmanager.ui.tinhtiendiennuoc

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.billmanager.R
import java.text.NumberFormat
import java.util.Locale

class MainTinhTienDienNuoc : AppCompatActivity() {

    private lateinit var edtDienDauKy: EditText
    private lateinit var edtDienCuoiKy: EditText
    private lateinit var edtNuocDauKy: EditText
    private lateinit var edtNuocCuoiKy: EditText

    private lateinit var tvDienTrai: TextView   // hiển thị số kWh (trái)
    private lateinit var tvDienPhai: TextView   // hiển thị tiền (phải)
    private lateinit var tvNuocTrai: TextView   // hiển thị m3 (trái)
    private lateinit var tvNuocPhai: TextView   // hiển thị tiền (phải)

    private lateinit var btnBack: Button
    private lateinit var btnThanhToan: Button
    private lateinit var btnLuu: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tinh_tien_dien_nuoc)

        // Ánh xạ view
        edtDienDauKy = findViewById(R.id.edtDienDauKy)
        edtDienCuoiKy = findViewById(R.id.edtDienCuoiKy)
        edtNuocDauKy = findViewById(R.id.edtNuocDauKy)
        edtNuocCuoiKy = findViewById(R.id.edtNuocCuoiKy)

        tvDienTrai = findViewById(R.id.tvDienTrai)
        tvDienPhai = findViewById(R.id.tvDienPhai)
        tvNuocTrai = findViewById(R.id.tvNuocTrai)
        tvNuocPhai = findViewById(R.id.tvNuocPhai)

        btnBack = findViewById(R.id.btnQuayLai)
        btnThanhToan = findViewById(R.id.btnThanhToan)
        btnLuu = findViewById(R.id.btnLuu)

        // Tự tính khi nhập (TextWatcher)
        addAutoTinh(edtDienDauKy)
        addAutoTinh(edtDienCuoiKy)
        addAutoTinh(edtNuocDauKy)
        addAutoTinh(edtNuocCuoiKy)

        // Nút quay lại reload (tạm)
        btnBack.setOnClickListener { recreate() }

        // Nút lưu (tạm)
        btnLuu.setOnClickListener {
            Toast.makeText(this, "Đã lưu – trạng thái: Chờ thanh toán", Toast.LENGTH_LONG).show()
        }

        // Nút thanh toán
        btnThanhToan.setOnClickListener { showPaymentDialog() }
    }

    // Thêm watcher cho EditText
    private fun addAutoTinh(edit: EditText) {
        edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // mỗi lần thay đổi sẽ tính riêng điện & nước
                tinhTienDien()
                tinhTienNuoc()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Format tiền VNĐ: 100.000₫ (dùng . làm ngăn phần nghìn)
    private fun formatCurrency(amount: Long): String {
        val nf = NumberFormat.getInstance(Locale("vi", "VN"))
        val s = nf.format(amount)
        return "$s đ"
    }

    // Validate chung: trả về Pair(start,end) nếu hợp lệ, hoặc null nếu ko hợp lệ
    private fun validatePair(startTxt: String, endTxt: String, startEdit: EditText, endEdit: EditText): Pair<Int, Int>? {
        val s1 = startTxt.trim()
        val s2 = endTxt.trim()
        if (s1.isEmpty()) {
            startEdit.error = "Không được để trống"
            return null
        }
        if (s2.isEmpty()) {
            endEdit.error = "Không được để trống"
            return null
        }
        // chỉ cho số
        if (!s1.matches(Regex("\\d+"))) {
            startEdit.error = "Chỉ nhập số"
            return null
        }
        if (!s2.matches(Regex("\\d+"))) {
            endEdit.error = "Chỉ nhập số"
            return null
        }

        val v1 = s1.toInt()
        val v2 = s2.toInt()
        if (v2 < v1) {
            endEdit.error = "Chỉ số cuối kỳ phải >= đầu kỳ"
            return null
        }

        return Pair(v1, v2)
    }

    // ---------- TÍNH TIỀN ĐIỆN (chi tiết theo bậc, có VAT 8% ở cuối) ----------
    private fun tinhTienDien() {
        val start = edtDienDauKy.text.toString()
        val end = edtDienCuoiKy.text.toString()

        val pair = validatePair(start, end, edtDienDauKy, edtDienCuoiKy)
        if (pair == null) {
            tvDienTrai.text = ""
            tvDienPhai.text = ""
            return
        }

        val d1 = pair.first
        val d2 = pair.second
        val totalKwh = d2 - d1

        // Bậc EVN (ví dụ mẫu) và đơn giá tương ứng (đồng/kWh) — cập nhật theo bảng hiện hành nếu cần
        val bacs = intArrayOf(50, 50, 100, 100, 100, Int.MAX_VALUE)
        val gias = longArrayOf(1984, 2050, 2380, 2729, 3050, 3151) // lưu ý: dùng giá theo ảnh bạn gửi (ví dụ)

        // Tính phân bậc: trả về danh sách các dòng (bậc, sl, dg, thanhTien)
        val details = mutableListOf<Triple<Int, Long, Long>>() // (soLuong, donGia, thanhTien)
        var remain = totalKwh
        var totalNoTax: Long = 0

        for (i in bacs.indices) {
            if (remain <= 0) break
            val use = minOf(remain, bacs[i])
            val price = gias[i]
            val amount = use.toLong() * price
            details.add(Triple(use, price, amount))
            totalNoTax += amount
            remain -= use
        }

        // Thuế GTGT theo ví dụ ảnh: 8% (điện sinh hoạt thường 8%)
        val vatPercent = 8.0
        val vat = Math.round(totalNoTax * vatPercent / 100.0)
        val totalPay = totalNoTax + vat

        // Hiển thị nhanh ở màn (trái: kWh, phải: tiền tổng)
        tvDienTrai.text = "$totalKwh kWh"
        tvDienPhai.text = formatCurrency(totalPay)

        // Lưu details vào tag của tv để show khi cần (hóa đơn)
        // Dùng string đơn giản: sẽ parse lại khi show hóa đơn
        val sb = StringBuilder()
        sb.append("CHI_TIET")
        for ((sl, dg, tt) in details) {
            sb.append("|").append(sl).append(",").append(dg).append(",").append(tt)
        }
        sb.append("|TOTAL_NO_TAX,").append(totalNoTax)
        sb.append("|VAT,").append(vat)
        sb.append("|TOTAL_PAY,").append(totalPay)
        tvDienPhai.tag = sb.toString()
    }

    // ---------- TÍNH TIỀN NƯỚC (ví dụ: tiền nước + tiền DVTN + VAT từng phần) ----------
    private fun tinhTienNuoc() {
        val start = edtNuocDauKy.text.toString()
        val end = edtNuocCuoiKy.text.toString()

        // Validate input (dùng hàm validatePair của bạn)
        val pair = validatePair(start, end, edtNuocDauKy, edtNuocCuoiKy)
        if (pair == null) {
            tvNuocTrai.text = ""
            tvNuocPhai.text = ""
            return
        }

        val n1 = pair.first
        val n2 = pair.second
        val totalM3 = n2 - n1

        // =============================
        // BIỂU GIÁ THEO ĐÚNG HÓA ĐƠN
        // =============================

        val donGiaNuoc = 6700L      // Giá nước / m3
        val donGiaDVTN = 2010L      // Giá thoát nước (DVTN) / m3

        // Tiền trước thuế
        val tienNuocNoTax = totalM3 * donGiaNuoc      // 107.200
        val tienDVTNNoTax = totalM3 * donGiaDVTN      // 32.160

        // =============================
        // VAT THEO HÓA ĐƠN
        // =============================
        val vatNuocPercent = 0.05   // VAT tiền nước 5%
        val vatDVTNPercent = 0.08   // VAT DVTN 8% (không giống tiền nước)

        val vatNuoc = Math.round(tienNuocNoTax * vatNuocPercent)     // 5.360
        val vatDVTN = Math.round(tienDVTNNoTax * vatDVTNPercent)     // 2.573

        // =============================
        // TỔNG TIỀN PHẢI TRẢ
        // =============================
        val totalPay = tienNuocNoTax + vatNuoc +
                tienDVTNNoTax + vatDVTN     // = 147.293

        // =============================
        // HIỂN THỊ RA MÀN HÌNH
        // =============================
        tvNuocTrai.text = "$totalM3 m³"
        tvNuocPhai.text = formatCurrency(totalPay)
        2
        // lưu tag để hiển thị chi tiết
        val sb = StringBuilder()
        sb.append("WATER_DETAIL")
        sb.append("|WATER,").append(totalM3).append(",").append(donGiaNuoc).append(",").append(tienNuocNoTax)
        sb.append("|DVTN,").append(totalM3).append(",").append(donGiaDVTN).append(",").append(tienDVTNNoTax)
        sb.append("|VAT_WATER,").append(vatNuoc)
        sb.append("|VAT_DVTN,").append(vatDVTN)
        sb.append("|TOTAL_PAY,").append(totalPay)
        tvNuocPhai.tag = sb.toString()
    }

    // Hiển thị hộp chọn thanh toán và HÓA ĐƠN CHI TIẾT (có tổng tiền)
    private fun showPaymentDialog() {
        val items = arrayOf("Điện", "Nước", "Cả 2")
        var selected = -1

        AlertDialog.Builder(this)
            .setTitle("Chọn loại thanh toán")
            .setSingleChoiceItems(items, -1) { _, which ->
                selected = which
            }
            .setPositiveButton("Xác nhận") { _, _ ->
                if (selected == -1) {
                    Toast.makeText(this, "Bạn chưa chọn hình thức!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                showHoaDon(selected)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // Hiện hóa đơn chi tiết: phân bậc + VAT + Tổng
    private fun showHoaDon(loai: Int) {
        // Lấy chi tiết điện từ tag
        val dienTag = tvDienPhai.tag as? String
        val nuocTag = tvNuocPhai.tag as? String

        // Xây message chi tiết
        val sb = StringBuilder()
        var totalAll: Long = 0

        if (loai == 0 || loai == 2) {
            // điện
            sb.append("=== HÓA ĐƠN ĐIỆN ===\n")
            val dienMsg = buildDienDetail(dienTag)
            sb.append(dienMsg.text)
            totalAll += dienMsg.total
            sb.append("\n")
        }

        if (loai == 1 || loai == 2) {
            // nước
            sb.append("=== HÓA ĐƠN NƯỚC ===\n")
            val nuocMsg = buildNuocDetail(nuocTag)
            sb.append(nuocMsg.text)
            totalAll += nuocMsg.total
            sb.append("\n")
        }

        sb.append("=> Tổng cộng phải thanh toán: ").append(formatCurrency(totalAll)).append("\n\n")
        sb.append("Mã chuyển khoản: ").append(" (để trống)")

        AlertDialog.Builder(this)
            .setTitle("Hóa đơn thanh toán")
            .setMessage(sb.toString())
            .setPositiveButton("Đóng", null)
            .show()
    }

    // Build chi tiết điện từ tag string
    private fun buildDienDetail(tag: String?): DetailResult {
        if (tag == null || !tag.startsWith("CHI_TIET")) {
            return DetailResult("Chưa có dữ liệu điện\n", 0L)
        }
        // format: CHI_TIET|sl,dg,tt|sl,dg,tt|...|TOTAL_NO_TAX,value|VAT,value|TOTAL_PAY,value
        val parts = tag.split("|")
        val lines = StringBuilder()
        var totalNoTax = 0L
        var vat = 0L
        var totalPay = 0L
        // parse
        for (p in parts) {
            if (p.startsWith("TOTAL_NO_TAX")) {
                val v = p.split(",").getOrNull(1)?.toLongOrNull() ?: 0L
                totalNoTax = v
            } else if (p.startsWith("VAT,")) {
                val v = p.split(",").getOrNull(1)?.toLongOrNull() ?: 0L
                vat = v
            } else if (p.startsWith("TOTAL_PAY,")) {
                val v = p.split(",").getOrNull(1)?.toLongOrNull() ?: 0L
                totalPay = v
            } else if (p.contains(",")) {
                // sl,dg,tt
                val arr = p.split(",")
                if (arr.size >= 3 && arr[0].toIntOrNull() != null) {
                    val sl = arr[0].toInt()
                    val dg = arr[1].toLong()
                    val tt = arr[2].toLong()
                    lines.append("Bậc: $sl kWh × ${formatCurrency(dg)} = ${formatCurrency(tt)}\n")
                }
            }
        }
        lines.append("Tổng trước thuế: ${formatCurrency(totalNoTax)}\n")
        lines.append("Thuế GTGT: ${formatCurrency(vat)}\n")
        lines.append("Tổng phải trả (điện): ${formatCurrency(totalPay)}\n")
        return DetailResult(lines.toString(), totalPay)
    }

    // Build chi tiết nước từ tag string
    private fun buildNuocDetail(tag: String?): DetailResult {
        if (tag == null || !tag.startsWith("WATER_DETAIL")) {
            return DetailResult("Chưa có dữ liệu nước\n", 0L)
        }
        // format: WATER_DETAIL|WATER,total,dg,tt|DVTN,total,dg,tt|VAT_WATER,x|VAT_DVTN,y|TOTAL_PAY,z
        val parts = tag.split("|")
        val lines = StringBuilder()
        var vatW = 0L
        var vatD = 0L
        var totalPay = 0L
        var waterLine: String? = null
        var dvtnLine: String? = null
        for (p in parts) {
            if (p.startsWith("WATER,")) {
                val a = p.split(",")
                val qty = a.getOrNull(1)?.toLongOrNull() ?: 0L
                val dg = a.getOrNull(2)?.toLongOrNull() ?: 0L
                val tt = a.getOrNull(3)?.toLongOrNull() ?: 0L
                waterLine = "Tiền nước: $qty m³ × ${formatCurrency(dg)} = ${formatCurrency(tt)}"
            } else if (p.startsWith("DVTN,")) {
                val a = p.split(",")
                val qty = a.getOrNull(1)?.toLongOrNull() ?: 0L
                val dg = a.getOrNull(2)?.toLongOrNull() ?: 0L
                val tt = a.getOrNull(3)?.toLongOrNull() ?: 0L
                dvtnLine = "Tiền DVTN: $qty m³ × ${formatCurrency(dg)} = ${formatCurrency(tt)}"
            } else if (p.startsWith("VAT_WATER,")) {
                vatW = p.split(",").getOrNull(1)?.toLongOrNull() ?: 0L
            } else if (p.startsWith("VAT_DVTN,")) {
                vatD = p.split(",").getOrNull(1)?.toLongOrNull() ?: 0L
            } else if (p.startsWith("TOTAL_PAY,")) {
                totalPay = p.split(",").getOrNull(1)?.toLongOrNull() ?: 0L
            }
        }
        if (waterLine != null) lines.append(waterLine).append("\n")
        if (dvtnLine != null) lines.append(dvtnLine).append("\n")
        lines.append("Thuế GTGT tiền nước: ${formatCurrency(vatW)}\n")
        lines.append("Thuế GTGT DVTN: ${formatCurrency(vatD)}\n")
        lines.append("Tổng phải trả (nước): ${formatCurrency(totalPay)}\n")
        return DetailResult(lines.toString(), totalPay)
    }

    data class DetailResult(val text: String, val total: Long)
}