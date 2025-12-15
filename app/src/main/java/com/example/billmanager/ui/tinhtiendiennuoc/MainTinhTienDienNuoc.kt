package com.example.billmanager.ui.tinhtiendiennuoc

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Locale
import android.content.Intent
import android.view.Gravity
import com.example.billmanager.R
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.example.billmanager.data.model.DienDetail
import com.example.billmanager.data.model.NuocDetail


class MainTinhTienDienNuoc : AppCompatActivity() {

    private lateinit var edtDienDau: EditText
    private lateinit var edtDienCuoi: EditText
    private lateinit var edtNuocDau: EditText
    private lateinit var edtNuocCuoi: EditText

    private lateinit var tvDienTrai: TextView
    private lateinit var tvDienPhai: TextView
    private lateinit var tvNuocTrai: TextView
    private lateinit var tvNuocPhai: TextView

    private lateinit var btnBack: Button
    private lateinit var btnLuu: Button
    private lateinit var btnThanhToan: Button

    private var dienDetail: DienDetail? = null
    private var nuocDetail: NuocDetail? = null
    private var hdDien: HoaDonEntity? = null
    private var hdNuoc: HoaDonEntity? = null
    private var thang: Int = 0
    private var nam: Int = 0
    private var isEdit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tinh_tien_dien_nuoc)

        mappingView()
        addWatcher()

        thang = intent.getIntExtra("THANG", 0)
        nam = intent.getIntExtra("NAM", 0)

        hdDien = intent.getSerializableExtra("DIEN") as? HoaDonEntity
        hdNuoc = intent.getSerializableExtra("NUOC") as? HoaDonEntity

        isEdit = hdDien != null || hdNuoc != null

        hdDien?.let { doDuLieuDien(it) }
        hdNuoc?.let { doDuLieuNuoc(it) }

        if (hdDien?.trangThai == "Đã thanh toán") khoaDien()
        if (hdNuoc?.trangThai == "Đã thanh toán") khoaNuoc()

        btnBack.setOnClickListener { finish() }
        btnLuu.setOnClickListener { xuLyLuu() }
        btnThanhToan.setOnClickListener { xuLyThanhToan() }
    }

    // =============================
// ÁNH XẠ VIEW TỪ XML
// =============================
// Liên kết các View trong layout XML
// với biến Kotlin để sử dụng trong code
    private fun mappingView() {

        // ===== NHẬP CHỈ SỐ ĐIỆN =====
        edtDienDau = findViewById(R.id.edtDienDauKy)   // Chỉ số điện đầu kỳ
        edtDienCuoi = findViewById(R.id.edtDienCuoiKy) // Chỉ số điện cuối kỳ

        // ===== NHẬP CHỈ SỐ NƯỚC =====
        edtNuocDau = findViewById(R.id.edtNuocDauKy)   // Chỉ số nước đầu kỳ
        edtNuocCuoi = findViewById(R.id.edtNuocCuoiKy) // Chỉ số nước cuối kỳ

        // ===== HIỂN THỊ KẾT QUẢ ĐIỆN =====
        tvDienTrai = findViewById(R.id.tvDienTrai) // Sản lượng điện (kWh)
        tvDienPhai = findViewById(R.id.tvDienPhai) // Tiền điện

        // ===== HIỂN THỊ KẾT QUẢ NƯỚC =====
        tvNuocTrai = findViewById(R.id.tvNuocTrai) // Sản lượng nước (m³)
        tvNuocPhai = findViewById(R.id.tvNuocPhai) // Tiền nước

        // ===== CÁC NÚT CHỨC NĂNG =====
        btnBack = findViewById(R.id.btnQuayLai)       // Nút quay lại
        btnLuu = findViewById(R.id.btnLuu)            // Nút lưu hóa đơn
        btnThanhToan = findViewById(R.id.btnThanhToan) // Nút thanh toán
    }


    // =============================
// THEO DÕI THAY ĐỔI INPUT
// =============================
// Khi người dùng nhập/chỉnh sửa chỉ số
// → tự động tính lại tiền điện & nước
    private fun addWatcher() {

        listOf(
            edtDienDau,
            edtDienCuoi,
            edtNuocDau,
            edtNuocCuoi
        ).forEach {

            it.addTextChangedListener(object : TextWatcher {

                // Sau khi người dùng nhập xong
                override fun afterTextChanged(s: Editable?) {
                    tinhTienDien() // Tính lại tiền điện
                    tinhTienNuoc() // Tính lại tiền nước
                }

                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?, start: Int, before: Int, count: Int
                ) {}
            })
        }
    }


    // =============================
    // ĐỔ DỮ LIỆU SỬA
    // =============================
    private fun doDuLieuSua(hd: HoaDonEntity) {
        if (hd.loai == "Điện") {
            edtDienDau.setText(hd.chiSoDau.toString())
            edtDienCuoi.setText(hd.chiSoCuoi.toString())
        } else {
            edtNuocDau.setText(hd.chiSoDau.toString())
            edtNuocCuoi.setText(hd.chiSoCuoi.toString())
        }

        if (hd.trangThai == "Đã thanh toán") {
            khoaNhap()
            btnLuu.isEnabled = false
            btnThanhToan.isEnabled = false
            Toast.makeText(this, "Hóa đơn đã thanh toán – chỉ xem", Toast.LENGTH_LONG).show()
        }
    }

    private fun khoaNhap() {
        edtDienDau.isEnabled = false
        edtDienCuoi.isEnabled = false
        edtNuocDau.isEnabled = false
        edtNuocCuoi.isEnabled = false
    }

    // =============================
    // LƯU
    // =============================
    private fun xuLyLuu() {

        val result = Intent()

        // ===== HÓA ĐƠN ĐIỆN =====
        val dienMoi = if (dienDetail != null) {
            HoaDonEntity(
                id = hdDien?.id ?: -1,
                loai = "Điện",
                thang = thang,
                nam = nam,
                chiSoDau = edtDienDau.text.toString().toInt(),
                chiSoCuoi = edtDienCuoi.text.toString().toInt(),
                soLuong = dienDetail!!.soKwh,
                tongTien = dienDetail!!.tongTien,
                gio = hdDien?.gio ?: "10:00",
                ngay = hdDien?.ngay ?: "Hôm nay",
                trangThai = hdDien?.trangThai ?: "Chưa thanh toán"
            )
        } else null

        // ===== HÓA ĐƠN NƯỚC =====
        val nuocMoi = if (nuocDetail != null) {
            HoaDonEntity(
                id = hdNuoc?.id ?: -1,
                loai = "Nước",
                thang = thang,
                nam = nam,
                chiSoDau = edtNuocDau.text.toString().toInt(),
                chiSoCuoi = edtNuocCuoi.text.toString().toInt(),
                soLuong = nuocDetail!!.soM3,
                tongTien = nuocDetail!!.tongTien,
                gio = hdNuoc?.gio ?: getPaymentTime(),
                ngay = hdNuoc?.ngay ?: getPaymentDate(),
                trangThai = hdNuoc?.trangThai ?: "Chưa thanh toán"
            )
        } else null

        result.putExtra("DIEN_NEW", dienMoi)
        result.putExtra("NUOC_NEW", nuocMoi)
        result.putExtra("MSG", "Lưu hóa đơn thành công")

        setResult(RESULT_OK, result)
        finish()

    }

    private fun chuaLuuChiSo(): Boolean {
        if (dienDetail != null && hdDien == null) return true
        if (nuocDetail != null && hdNuoc == null) return true
        return false
    }


    // =============================
    // THANH TOÁN
    // =============================
    private fun xuLyThanhToan() {
        if (dienDetail == null && nuocDetail == null) {
            Toast.makeText(this, "Chưa có dữ liệu", Toast.LENGTH_SHORT).show()
            return
        }
        showPaymentDialog()
    }

    // =============================
    // FORMAT
    // =============================
    private fun money(v: Long): String =
        NumberFormat.getInstance(Locale("vi", "VN")).format(v) + " đ"


    // =============================
    // NGÀY / GIỜ THANH TOÁN THỰC TẾ
    // =============================
    private fun getPaymentTime(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun getPaymentDate(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(java.util.Date())
    }


    // =============================
    // MÃ CHUYỂN KHOẢN ẢO
    // =============================
    private fun taoMaChuyenKhoan(loai: String): String {
        return "HD_${loai}_${thang}_${nam}_${System.currentTimeMillis() % 100000}"
    }

    private fun taoQR(noiDung: String): ImageView {
        val url =
            "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=$noiDung"

        val img = ImageView(this)
        img.layoutParams = LinearLayout.LayoutParams(600, 600)

        Thread {
            try {
                val stream = java.net.URL(url).openStream()
                val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                runOnUiThread { img.setImageBitmap(bitmap) }
            } catch (_: Exception) {}
        }.start()

        return img
    }

    private fun showLoading(msg: String): AlertDialog {
        val pb = ProgressBar(this)
        val tv = TextView(this).apply {
            text = msg
            setPadding(0, 30, 0, 0)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
            addView(pb)
            addView(tv)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(layout)
            .setCancelable(false)
            .create()

        dialog.show()
        return dialog
    }

    // =============================
// TÍNH TIỀN ĐIỆN THEO BẬC
// =============================
    private fun tinhTienDien() {

        // Lấy dữ liệu từ EditText
        val start = edtDienDau.text.toString()
        val end = edtDienCuoi.text.toString()

        // Kiểm tra dữ liệu hợp lệ
        val pair = validatePair(start, end, edtDienDau, edtDienCuoi) ?: return

        // Sản lượng điện tiêu thụ
        val soKwh = pair.second - pair.first

        // Định mức bậc & giá
        val bac = intArrayOf(50, 50, 100, 100, 100, Int.MAX_VALUE)
        val gia = longArrayOf(1984, 2050, 2380, 2729, 3050, 3151)

        var remain = soKwh
        var totalNoTax = 0L
        val lines = mutableListOf<String>()

        // Tính tiền theo từng bậc
        for (i in bac.indices) {
            if (remain <= 0) break

            val use = minOf(remain, bac[i])
            val tien = use * gia[i]

            lines.add("Bậc ${i + 1}: $use kWh × ${money(gia[i])} = ${money(tien)}")

            totalNoTax += tien
            remain -= use
        }

        // VAT điện 8%
        val vat = (totalNoTax * 8 / 100)
        val total = totalNoTax + vat

        // Lưu chi tiết điện
        dienDetail = DienDetail(soKwh, totalNoTax, vat, total, lines)

        // Hiển thị ra UI
        tvDienTrai.text = "$soKwh kWh"
        tvDienPhai.text = money(total)
    }


    // =============================
// TÍNH TIỀN NƯỚC
// =============================
    private fun tinhTienNuoc() {

        // Lấy chỉ số nước
        val start = edtNuocDau.text.toString()
        val end = edtNuocCuoi.text.toString()

        // Kiểm tra hợp lệ
        val pair = validatePair(start, end, edtNuocDau, edtNuocCuoi) ?: return

        // Sản lượng nước tiêu thụ
        val soM3 = pair.second - pair.first

        // Các khoản phí
        val tienNuoc = soM3 * 6700L
        val tienDVTN = soM3 * 2010L
        val vatNuoc = (tienNuoc * 5 / 100)
        val vatDVTN = (tienDVTN * 8 / 100)

        // Tổng tiền nước
        val total = tienNuoc + tienDVTN + vatNuoc + vatDVTN

        // Lưu chi tiết nước
        nuocDetail = NuocDetail(
            soM3,
            tienNuoc,
            tienDVTN,
            vatNuoc,
            vatDVTN,
            total
        )

        // Hiển thị ra UI
        tvNuocTrai.text = "$soM3 m³"
        tvNuocPhai.text = money(total)
    }

    private fun doDuLieuDien(hd: HoaDonEntity) {
        edtDienDau.setText(hd.chiSoDau.toString())
        edtDienCuoi.setText(hd.chiSoCuoi.toString())
    }

    private fun doDuLieuNuoc(hd: HoaDonEntity) {
        edtNuocDau.setText(hd.chiSoDau.toString())
        edtNuocCuoi.setText(hd.chiSoCuoi.toString())
    }

    private fun khoaDien() {
        edtDienDau.isEnabled = false
        edtDienCuoi.isEnabled = false
    }

    private fun khoaNuoc() {
        edtNuocDau.isEnabled = false
        edtNuocCuoi.isEnabled = false
    }

    // =============================
    // DIALOG THANH TOÁN
    // =============================
    private fun showPaymentDialog() {
        val items = arrayOf("Điện", "Nước")
        var chon = -1

        AlertDialog.Builder(this)
            .setTitle("Chọn loại thanh toán")
            .setSingleChoiceItems(items, -1) { _, i -> chon = i }
            .setPositiveButton("Tiếp tục") { _, _ ->
                if (chon != -1) showHoaDonThanhToan(chon)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showHoaDonThanhToan(loai: Int) {

        val isDien = loai == 0
        val ten = if (isDien) "Điện" else "Nước"

        val detailText = StringBuilder()
        var tongTien = 0L

        if (isDien) {
            val d = dienDetail!!
            detailText.append(
                """
            === HÓA ĐƠN ĐIỆN ===
            Chỉ số: ${edtDienDau.text} → ${edtDienCuoi.text}
            Sản lượng: ${d.soKwh} kWh
            
            """
            )
            d.chiTiet.forEach { detailText.append(it).append("\n") }
            detailText.append("VAT (8%): ${money(d.vat)}\n")
            detailText.append("TỔNG ĐIỆN: ${money(d.tongTien)}\n")
            tongTien = d.tongTien
        } else {
            val n = nuocDetail!!
            detailText.append(
                """
            === HÓA ĐƠN NƯỚC ===
            Chỉ số: ${edtNuocDau.text} → ${edtNuocCuoi.text}
            Sản lượng: ${n.soM3} m³
            
            Tiền nước: ${money(n.tienNuoc)}
            Tiền DVTN: ${money(n.tienDVTN)}
            VAT: ${money(n.vatNuoc + n.vatDVTN)}
            TỔNG NƯỚC: ${money(n.tongTien)}
            """
            )
            tongTien = n.tongTien
        }

        // ===== QR ẢO =====
        val maCK = taoMaChuyenKhoan(ten)
        val noiDung = "Thanh toan $ten T$thang/$nam - $maCK"
        val qrView = taoQR(noiDung)

        AlertDialog.Builder(this)
            .setTitle("Xác nhận thanh toán")
            .setMessage(
                detailText.toString() +
                        "\n---------------------\n" +
                        "Số tiền cần thanh toán:\n${money(tongTien)}\n\n" +
                        "Nội dung chuyển khoản:\n$maCK"
            )
            .setView(qrView)
            .setPositiveButton("Tôi đã chuyển khoản") { _, _ ->
                val loading = showLoading("Đang xác nhận giao dịch...")
                qrView.postDelayed({
                    loading.dismiss()
                    xuLySauThanhToan(loai)
                }, 2000)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }


    private fun xuLySauThanhToan(loai: Int) {

        if (loai == 0) {
            hdDien?.trangThai = "Đã thanh toán"
        } else {
            hdNuoc?.trangThai = "Đã thanh toán"
        }

        val result = Intent()

        // 👉 Tạo hóa đơn mới để trả về History
        val dienMoi = dienDetail?.let {
            HoaDonEntity(
                id = hdDien?.id ?: -1,
                loai = "Điện",
                thang = thang,
                nam = nam,
                chiSoDau = edtDienDau.text.toString().toInt(),
                chiSoCuoi = edtDienCuoi.text.toString().toInt(),
                soLuong = it.soKwh,
                tongTien = it.tongTien,
                gio = getPaymentTime(),
                ngay = getPaymentDate(),
                trangThai = if (loai == 0) "Đã thanh toán" else hdDien?.trangThai ?: "Chưa thanh toán"
            )
        }

        val nuocMoi = nuocDetail?.let {
            HoaDonEntity(
                id = hdNuoc?.id ?: -1,
                loai = "Nước",
                thang = thang,
                nam = nam,
                chiSoDau = edtNuocDau.text.toString().toInt(),
                chiSoCuoi = edtNuocCuoi.text.toString().toInt(),
                soLuong = it.soM3,
                tongTien = it.tongTien,
                gio = getPaymentTime(),
                ngay = getPaymentDate(),
                trangThai = if (loai == 1) "Đã thanh toán" else hdNuoc?.trangThai ?: "Chưa thanh toán"
            )
        }

        result.putExtra("DIEN_NEW", dienMoi)
        result.putExtra("NUOC_NEW", nuocMoi)
        result.putExtra("MSG", "Thanh toán thành công")

        setResult(RESULT_OK, result)
        finish()
    }


    // =============================
    // VALIDATE
    // =============================
    private fun validatePair(s1: String, s2: String, e1: EditText, e2: EditText): Pair<Int, Int>? {
        if (s1.isEmpty() || s2.isEmpty()) return null
        val a = s1.toIntOrNull() ?: return null
        val b = s2.toIntOrNull() ?: return null
        if (b < a) {
            e2.error = "Chỉ số cuối phải ≥ chỉ số đầu"
            return null
        }
        return Pair(a, b)
    }
}