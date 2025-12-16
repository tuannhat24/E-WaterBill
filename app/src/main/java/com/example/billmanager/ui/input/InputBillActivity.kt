package com.example.billmanager.ui.input

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.example.billmanager.data.local.entity.NotificationEntity
import com.example.billmanager.data.model.DienDetail
import com.example.billmanager.data.model.NuocDetail
import com.example.billmanager.data.model.NotificationType
import com.example.billmanager.data.repository.HoaDonRepository
import com.example.billmanager.utils.BillCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class InputBillActivity : AppCompatActivity() {

    // ------------------------
    // Các control trên giao diện
    // ------------------------
    private lateinit var edtThang: EditText
    private lateinit var edtNam: EditText
    private lateinit var rgLoai: RadioGroup
    private lateinit var edtChiSoCu: EditText
    private lateinit var edtChiSoMoi: EditText
    private lateinit var tvTamTinh: TextView
    private lateinit var tvTitle: TextView
    private lateinit var btnLuu: Button
    private lateinit var btnThanhToan: Button
    private lateinit var btnBack: ImageButton

    // ------------------------
    // Repository & dữ liệu
    // ------------------------
    private lateinit var repository: HoaDonRepository
    private var detailDien: DienDetail? = null
    private var detailNuoc: NuocDetail? = null
    private var editBill: HoaDonEntity? = null

    // ------------------------
    // onCreate
    // ------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_bill)

        // Khởi tạo repository thao tác DB
        val db = AppDatabase.getInstance(this)
        repository = HoaDonRepository(db.hoaDonDao())

        setControl() // Gán control
        setEvent()   // Gán sự kiện

        // Nếu intent truyền hóa đơn để sửa
        editBill = intent.getParcelableExtra("EDIT_BILL")
        editBill?.let { bill ->
            tvTitle.text = "Sửa Hóa Đơn"
            edtThang.setText(bill.thang.toString())
            edtNam.setText(bill.nam.toString())
            edtChiSoCu.setText(bill.chiSoDau.toString())
            edtChiSoMoi.setText(bill.chiSoCuoi.toString())
            if (bill.loai == "Điện") rgLoai.check(R.id.rbDien)
            else rgLoai.check(R.id.rbNuoc)
            autoCalculate()
        }
    }

    // ------------------------
    // Gán control
    // ------------------------
    private fun setControl() {
        edtThang = findViewById(R.id.edtThang)
        edtNam = findViewById(R.id.edtNam)
        rgLoai = findViewById(R.id.rgLoai)
        edtChiSoCu = findViewById(R.id.edtChiSoCu)
        edtChiSoMoi = findViewById(R.id.edtChiSoMoi)
        tvTamTinh = findViewById(R.id.tvTamTinh)
        btnLuu = findViewById(R.id.btnLuu)
        btnThanhToan = findViewById(R.id.btnThanhToan)
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        tvTitle.text = "Thêm Hóa Đơn"
    }

    // ------------------------
    // Gán sự kiện
    // ------------------------
    private fun setEvent() {

        // Nút quay lại
        btnBack.setOnClickListener { finish() }

        // TextWatcher tự tính tiền khi chỉ số thay đổi
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { autoCalculate() }
        }
        edtChiSoCu.addTextChangedListener(watcher)
        edtChiSoMoi.addTextChangedListener(watcher)

        // Nút lưu hóa đơn
        btnLuu.setOnClickListener { saveBill() }

        // Nút thanh toán
        btnThanhToan.setOnClickListener { showPaymentDialog() }
    }

    // ------------------------
    // Tính tạm tổng tiền
    // ------------------------
    private fun autoCalculate() {
        val cu = edtChiSoCu.text.toString().toIntOrNull() ?: return
        val moi = edtChiSoMoi.text.toString().toIntOrNull() ?: return
        if (moi < cu) return

        val isDien = rgLoai.checkedRadioButtonId == R.id.rbDien
        val chiTietLines: List<String>
        val tong: Long

        if (isDien) {
            val utilDetail = BillCalculator.tinhTienDien(cu, moi)
            detailDien = utilDetail
            chiTietLines = utilDetail.chiTiet
            tong = utilDetail.tongTien
        } else {
            val utilDetail = BillCalculator.tinhTienNuoc(cu, moi)
            detailNuoc = utilDetail
            chiTietLines = listOf(
                "Tiền nước: ${formatMoney(utilDetail.tienNuoc)}",
                "Tiền DVTN: ${formatMoney(utilDetail.tienDVTN)}",
                "VAT: ${formatMoney(utilDetail.vatNuoc + utilDetail.vatDVTN)}"
            )
            tong = utilDetail.tongTien
        }

        tvTamTinh.text = chiTietLines.joinToString("\n") + "\nTỔNG: ${formatMoney(tong)}"
        tvTamTinh.textSize = if (chiTietLines.size <= 3) 16f else 12f
    }

    // ------------------------
    // Lưu hóa đơn
    // ------------------------
    private fun saveBill() {
        val thang = edtThang.text.toString().toIntOrNull()
        val nam = edtNam.text.toString().toIntOrNull()
        val cu = edtChiSoCu.text.toString().toIntOrNull()
        val moi = edtChiSoMoi.text.toString().toIntOrNull()
        val isDien = rgLoai.checkedRadioButtonId == R.id.rbDien
        val loai = if (isDien) "Điện" else "Nước"

        // Kiểm tra dữ liệu hợp lệ
        if (thang == null || nam == null || cu == null || moi == null) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }
        if (thang !in 1..12 || moi < cu) {
            Toast.makeText(this, "Thông tin không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        // ------------------------
        // Kiểm tra duplicate hóa đơn cùng loại/tháng/năm
        // ------------------------
        val exists = repository.getAll().any {
            it.loai == loai && it.thang == thang && it.nam == nam && (editBill == null || it.id != editBill!!.id)
        }
        if (exists) {
            Toast.makeText(this, "Hóa đơn $loai tháng này đã tồn tại!", Toast.LENGTH_LONG).show()
            return
        }

        // ------------------------
        // --- Fix: Chặn sửa hóa đơn đã thanh toán ---
        // ------------------------
        val existingPaid = repository.getAll().firstOrNull {
            it.loai == loai && it.thang == thang && it.nam == nam && it.trangThai == "Đã thanh toán" &&
                    (editBill == null || it.id != editBill!!.id)
        }
        if (existingPaid != null) {
            Toast.makeText(this, "Hóa đơn $loai tháng này đã thanh toán, không thể sửa!", Toast.LENGTH_LONG).show()
            return
        }

        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Tạo đối tượng hóa đơn
        val hoaDon = if (isDien) {
            val dien = detailDien ?: return
            HoaDonEntity(
                id = if (editBill?.loai == loai) editBill!!.id else 0,
                loai = loai,
                thang = thang,
                nam = nam,
                chiSoDau = cu,
                chiSoCuoi = moi,
                soLuong = dien.soKwh,
                tongTien = dien.tongTien,
                gio = timeFormat.format(currentTime),
                ngay = dateFormat.format(currentTime),
                trangThai = "Chưa thanh toán"
            )
        } else {
            val nuoc = detailNuoc ?: return
            HoaDonEntity(
                id = if (editBill?.loai == loai) editBill!!.id else 0,
                loai = loai,
                thang = thang,
                nam = nam,
                chiSoDau = cu,
                chiSoCuoi = moi,
                soLuong = nuoc.soM3,
                tongTien = nuoc.tongTien,
                gio = timeFormat.format(currentTime),
                ngay = dateFormat.format(currentTime),
                trangThai = "Chưa thanh toán"
            )
        }

        // Lưu vào DB
        CoroutineScope(Dispatchers.IO).launch {
            if (editBill != null && editBill!!.loai == loai) repository.update(hoaDon)
            else repository.insert(hoaDon)

            val db = AppDatabase.getInstance(this@InputBillActivity)
            val type = if (isDien) NotificationType.ELECTRIC else NotificationType.WATER
            db.notificationDao().insertNotification(
                NotificationEntity(
                    title = if (editBill != null && editBill!!.loai == loai) "Hóa đơn $loai đã cập nhật" else "Hóa đơn mới: $loai",
                    message = "T$thang/$nam - ${formatMoney(hoaDon.tongTien)}",
                    type = type,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        Toast.makeText(this, if (editBill != null && editBill!!.loai == loai) "Cập nhật hóa đơn thành công" else "Đã lưu hóa đơn mới", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK, Intent().apply { putExtra("HOADON_NEW", hoaDon) })
        finish()
    }

    // ------------------------
    // Dialog chọn thanh toán
    // ------------------------
    private fun showPaymentDialog() {
        val isDienFilter = rgLoai.checkedRadioButtonId == R.id.rbDien
        val isNuocFilter = rgLoai.checkedRadioButtonId == R.id.rbNuoc

        if (isDienFilter || isNuocFilter) {
            showHoaDonThanhToan(if (isDienFilter) 0 else 1)
            return
        }

        var chon = -1
        AlertDialog.Builder(this)
            .setTitle("Chọn loại thanh toán")
            .setSingleChoiceItems(arrayOf("Điện", "Nước"), -1) { _, i -> chon = i }
            .setPositiveButton("Tiếp tục") { _, _ -> if (chon != -1) showHoaDonThanhToan(chon) }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // ------------------------
    // Dialog hiển thị hóa đơn trước khi thanh toán
    // ------------------------
    private fun showHoaDonThanhToan(loai: Int) {
        val isDien = loai == 0
        val ten = if (isDien) "Điện" else "Nước"
        val maCK = taoMaChuyenKhoan(ten)
        val qrView = taoQR("Thanh toán $ten T${edtThang.text}/${edtNam.text} - $maCK")

        val msg = buildString {
            append("=== HÓA ĐƠN $ten ===\n")
            append("Chỉ số: ${edtChiSoCu.text} → ${edtChiSoMoi.text}\n")
            if (isDien) detailDien?.chiTiet?.forEach { append(it).append("\n") }
            else detailNuoc?.let { nuoc ->
                append("Tiền nước: ${formatMoney(nuoc.tienNuoc)}\n")
                append("Tiền DVTN: ${formatMoney(nuoc.tienDVTN)}\n")
                append("VAT: ${formatMoney(nuoc.vatNuoc + nuoc.vatDVTN)}\n")
            }
            append("TỔNG: ${formatMoney(if (isDien) detailDien?.tongTien ?: 0 else detailNuoc?.tongTien ?: 0)}\n")
            append("Nội dung chuyển khoản: $maCK")
        }

        AlertDialog.Builder(this)
            .setTitle("Xác nhận thanh toán")
            .setMessage(msg)
            .setView(qrView)
            .setPositiveButton("Tôi đã chuyển khoản") { _, _ -> xuLyThanhToan(isDien, ten) }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // ------------------------
    // Xử lý khi đã thanh toán
    // ------------------------
    private fun xuLyThanhToan(isDien: Boolean, ten: String) {
        val thang = edtThang.text.toString().toIntOrNull() ?: return
        val nam = edtNam.text.toString().toIntOrNull() ?: return

        // ------------------------
        // --- Fix: Chặn thanh toán nếu đã thanh toán ---
        // ------------------------
        val alreadyPaid = repository.getAll().firstOrNull {
            it.loai == ten && it.thang == thang && it.nam == nam && it.trangThai == "Đã thanh toán"
        }
        if (alreadyPaid != null) {
            runOnUiThread {
                Toast.makeText(
                    this@InputBillActivity,
                    "Hóa đơn $ten tháng này đã thanh toán, không thể thanh toán lại!",
                    Toast.LENGTH_LONG
                ).show()
            }
            return
        }

        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val hoaDonMoi = if (isDien) {
            val dien = detailDien ?: return
            HoaDonEntity(
                id = 0,
                loai = ten,
                thang = thang,
                nam = nam,
                chiSoDau = edtChiSoCu.text.toString().toInt(),
                chiSoCuoi = edtChiSoMoi.text.toString().toInt(),
                soLuong = dien.soKwh,
                tongTien = dien.tongTien,
                gio = timeFormat.format(currentTime),
                ngay = dateFormat.format(currentTime),
                trangThai = "Đã thanh toán"
            )
        } else {
            val nuoc = detailNuoc ?: return
            HoaDonEntity(
                id = 0,
                loai = ten,
                thang = thang,
                nam = nam,
                chiSoDau = edtChiSoCu.text.toString().toInt(),
                chiSoCuoi = edtChiSoMoi.text.toString().toInt(),
                soLuong = nuoc.soM3,
                tongTien = nuoc.tongTien,
                gio = timeFormat.format(currentTime),
                ngay = dateFormat.format(currentTime),
                trangThai = "Đã thanh toán"
            )
        }

        CoroutineScope(Dispatchers.IO).launch {
            val existingBill = repository.getAll().firstOrNull {
                it.loai == ten && it.thang == thang && it.nam == nam
            }

            if (existingBill != null) {
                val updatedBill = existingBill.copy(
                    chiSoDau = hoaDonMoi.chiSoDau,
                    chiSoCuoi = hoaDonMoi.chiSoCuoi,
                    soLuong = hoaDonMoi.soLuong,
                    tongTien = hoaDonMoi.tongTien,
                    gio = hoaDonMoi.gio,
                    ngay = hoaDonMoi.ngay,
                    trangThai = "Đã thanh toán"
                )
                repository.update(updatedBill)
            } else {
                repository.insert(hoaDonMoi)
            }

            val db = AppDatabase.getInstance(this@InputBillActivity)
            val type = if (isDien) NotificationType.ELECTRIC else NotificationType.WATER
            db.notificationDao().insertNotification(
                NotificationEntity(
                    title = "Hóa đơn $ten đã thanh toán",
                    message = "T$thang/$nam - ${formatMoney(hoaDonMoi.tongTien)}",
                    type = type,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

    // ------------------------
    // Tạo mã chuyển khoản giả
    // ------------------------
    private fun taoMaChuyenKhoan(loai: String) =
        "HD_${loai}_${edtThang.text}_${edtNam.text}_${System.currentTimeMillis() % 100000}"

    // ------------------------
    // Tạo QR code hiển thị dialog
    // ------------------------
    private fun taoQR(noiDung: String): ImageView {
        val url = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=$noiDung"
        val img = ImageView(this)
        img.layoutParams = LinearLayout.LayoutParams(600, 600)
        Thread {
            try {
                val stream = URL(url).openStream()
                val bitmap = BitmapFactory.decodeStream(stream)
                runOnUiThread { img.setImageBitmap(bitmap) }
            } catch (_: Exception) {}
        }.start()
        return img
    }

    // ------------------------
    // Định dạng tiền Việt Nam
    // ------------------------
    private fun formatMoney(v: Long) = String.format("%,d", v) + " đ"
}
