package com.example.billmanager.ui.input

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.example.billmanager.data.repository.HoaDonRepository
import com.example.billmanager.utils.BillCalculator
import java.text.SimpleDateFormat
import java.util.*

class InputBillActivity : AppCompatActivity() {

    private lateinit var edtThang: EditText
    private lateinit var edtNam: EditText
    private lateinit var rgLoai: RadioGroup
    private lateinit var edtChiSoCu: EditText
    private lateinit var edtChiSoMoi: EditText
    private lateinit var tvTamTinh: TextView
    private lateinit var btnLuu: Button
    private lateinit var btnBack: ImageButton // Nút Back

    private lateinit var repository: HoaDonRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_bill)

        // Init DB & Repo
        val db = AppDatabase.getInstance(this)
        repository = HoaDonRepository(db.hoaDonDao())

        setControl()
        setEvent()
    }

    private fun setControl() {
        edtThang = findViewById(R.id.edtThang)
        edtNam = findViewById(R.id.edtNam)
        rgLoai = findViewById(R.id.rgLoai)
        edtChiSoCu = findViewById(R.id.edtChiSoCu)
        edtChiSoMoi = findViewById(R.id.edtChiSoMoi)
        tvTamTinh = findViewById(R.id.tvTamTinh)
        btnLuu = findViewById(R.id.btnLuu)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setEvent() {
        btnBack.setOnClickListener { finish() }

        // Logic tính tiền tạm thời khi nhập xong (Optional: dùng TextWatcher để auto tính)
        btnLuu.setOnClickListener {
            saveBill()
        }
    }

    private fun saveBill() {
        // 1. Lấy dữ liệu
        val thang = edtThang.text.toString().toIntOrNull()
        val nam = edtNam.text.toString().toIntOrNull()
        val cu = edtChiSoCu.text.toString().toIntOrNull()
        val moi = edtChiSoMoi.text.toString().toIntOrNull()
        val isDien = rgLoai.checkedRadioButtonId == R.id.rbDien // Giả sử có RadioButton rbDien

        // 2. Validate
        if (thang == null || nam == null || cu == null || moi == null) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }
        if (thang !in 1..12) {
            Toast.makeText(this, "Tháng không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }
        if (moi < cu) {
            Toast.makeText(this, "Chỉ số mới phải lớn hơn hoặc bằng chỉ số cũ", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Tính toán
        val loaiStr = if (isDien) "Điện" else "Nước"
        val soLuong: Int
        val tongTien: Long

        if (isDien) {
            val detail = BillCalculator.tinhTienDien(cu, moi)
            soLuong = detail.soKwh
            tongTien = detail.tongTien
        } else {
            val detail = BillCalculator.tinhTienNuoc(cu, moi)
            soLuong = detail.soM3
            tongTien = detail.tongTien
        }

        // 4. Lưu vào Database
        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val hoaDon = HoaDonEntity(
            loai = loaiStr,
            thang = thang,
            nam = nam,
            chiSoDau = cu,
            chiSoCuoi = moi,
            soLuong = soLuong,
            tongTien = tongTien,
            gio = timeFormat.format(currentTime),
            ngay = dateFormat.format(currentTime),
            trangThai = "Chưa thanh toán"
        )

        // TODO: Kiểm tra trùng lặp (Tháng/Năm/Loại) trước khi Insert
        // val existing = repository.findBill(thang, nam, loaiStr) -> Cần thêm hàm này trong DAO

        repository.insert(hoaDon)
        Toast.makeText(this, "Đã lưu hóa đơn thành công!", Toast.LENGTH_SHORT).show()
        finish() // Đóng màn hình nhập
    }
}