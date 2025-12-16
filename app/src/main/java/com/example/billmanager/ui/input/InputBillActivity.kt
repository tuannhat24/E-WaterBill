package com.example.billmanager.ui.input

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.example.billmanager.data.local.entity.LocationEntity
import com.example.billmanager.data.local.entity.NotificationEntity
import com.example.billmanager.data.model.NotificationType
import com.example.billmanager.data.repository.HoaDonRepository
import com.example.billmanager.utils.BillCalculator
import com.example.billmanager.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class InputBillActivity : AppCompatActivity() {

    private lateinit var edtThang: EditText
    private lateinit var edtNam: EditText
    private lateinit var rgLoai: RadioGroup
    private lateinit var rbDien: RadioButton
    private lateinit var rbNuoc: RadioButton
    private lateinit var edtChiSoCu: EditText
    private lateinit var edtChiSoMoi: EditText
    private lateinit var tvTamTinh: TextView
    private lateinit var tvTitle: TextView
    private lateinit var btnLuu: Button
    private lateinit var btnBack: ImageButton
    private lateinit var spnLocation: Spinner
    private var listLocation: List<LocationEntity> = emptyList()

    private lateinit var repository: HoaDonRepository
    private var billToEdit: HoaDonEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_bill)

        val db = AppDatabase.getInstance(this)
        repository = HoaDonRepository(db.hoaDonDao())

        setControl()

        loadLocationSpinner()

        // Kiểm tra xem có phải đang sửa hóa đơn không
        if (intent.hasExtra("BILL_EDIT")) {
            billToEdit = intent.getSerializableExtra("BILL_EDIT") as HoaDonEntity
            fillDataToEdit(billToEdit!!)
            btnLuu.text = "Cập nhật Hóa Đơn"
        }

        setEvent()
    }

    private fun setControl() {
        edtThang = findViewById(R.id.edtThang)
        edtNam = findViewById(R.id.edtNam)
        rgLoai = findViewById(R.id.rgLoai)
        rbDien = findViewById(R.id.rbDien)
        rbNuoc = findViewById(R.id.rbNuoc)
        edtChiSoCu = findViewById(R.id.edtChiSoCu)
        edtChiSoMoi = findViewById(R.id.edtChiSoMoi)
        tvTamTinh = findViewById(R.id.tvTamTinh)
        btnLuu = findViewById(R.id.btnLuu)
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        tvTitle.text = "Thêm Hóa Đơn"
        spnLocation = findViewById(R.id.spnLocation)
    }

    private fun loadLocationSpinner() {
        // Lấy danh sách địa điểm từ DB
        listLocation = AppDatabase.getInstance(this).locationDao().getAll()

        // Nếu chưa có địa điểm nào, tạo list ảo để tránh crash (dù Module 5 thường đã tạo default)
        val locationNames = if (listLocation.isNotEmpty()) {
            listLocation.map { it.name }
        } else {
            listOf("Mặc định")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locationNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnLocation.adapter = adapter

        // Tự động chọn địa điểm đang active (isSelected = true)
        if (listLocation.isNotEmpty()) {
            val activeIndex = listLocation.indexOfFirst { it.isSelected }
            if (activeIndex >= 0) {
                spnLocation.setSelection(activeIndex)
            }
        }
    }

    private fun fillDataToEdit(bill: HoaDonEntity) {
        edtThang.setText(bill.thang.toString())
        edtNam.setText(bill.nam.toString())
        edtChiSoCu.setText(bill.chiSoDau.toString())
        edtChiSoMoi.setText(bill.chiSoCuoi.toString())

        if (bill.loai == "Điện") rbDien.isChecked = true else rbNuoc.isChecked = true

        // Chọn lại đúng địa điểm của hóa đơn đang sửa
        if (listLocation.isNotEmpty()) {
            val index = listLocation.indexOfFirst { it.id == bill.locationId }
            if (index >= 0) spnLocation.setSelection(index)
        }
    }

    private fun setEvent() {
        btnBack.setOnClickListener { finish() }

        btnLuu.setOnClickListener {
            saveOrUpdateBill()
        }
    }

    private fun saveOrUpdateBill() {
        val thang = edtThang.text.toString().toIntOrNull()
        val nam = edtNam.text.toString().toIntOrNull()
        val cu = edtChiSoCu.text.toString().toIntOrNull()
        val moi = edtChiSoMoi.text.toString().toIntOrNull()
        val isDien = rgLoai.checkedRadioButtonId == R.id.rbDien

        if (thang == null || nam == null || cu == null || moi == null) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show()
            return
        }
        if (moi < cu) {
            Toast.makeText(this, "Chỉ số mới phải >= chỉ số cũ", Toast.LENGTH_SHORT).show()
            return
        }

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

        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Lấy ID địa điểm đang chọn
        val locationId = if (listLocation.isNotEmpty()) {
            listLocation[spnLocation.selectedItemPosition].id
        } else {
            1 // Fallback nếu lỗi
        }

        // Tạo object
        val hoaDon = HoaDonEntity(
            id = billToEdit?.id ?: 0,
            loai = loaiStr,
            thang = thang,
            nam = nam,
            chiSoDau = cu,
            chiSoCuoi = moi,
            soLuong = soLuong,
            tongTien = tongTien,
            gio = billToEdit?.gio ?: timeFormat.format(currentTime),
            ngay = billToEdit?.ngay ?: dateFormat.format(currentTime),
            trangThai = billToEdit?.trangThai ?: "Chưa thanh toán",
            locationId = locationId // <--- LƯU LOCATION ID VÀO ĐÂY
        )

        if (billToEdit != null) {
            repository.update(hoaDon)
            Toast.makeText(this, "Đã cập nhật hóa đơn!", Toast.LENGTH_SHORT).show()
        } else {
            repository.insert(hoaDon)
            triggerNotification(hoaDon)
            Toast.makeText(this, "Đã thêm hóa đơn mới!", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun triggerNotification(hoaDon: HoaDonEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(this@InputBillActivity)
            val notiTitle = "Hóa đơn mới: ${hoaDon.loai}"
            val notiMsg = "T${hoaDon.thang}/${hoaDon.nam}: ${String.format("%,d", hoaDon.tongTien)}đ"
            val notiType = if (hoaDon.loai == "Điện") NotificationType.ELECTRIC else NotificationType.WATER

            val newNoti = NotificationEntity(
                title = notiTitle,
                message = notiMsg,
                type = notiType,
                timestamp = System.currentTimeMillis()
            )
            db.notificationDao().insertNotification(newNoti)

            runOnUiThread {
                NotificationHelper(this@InputBillActivity).showNotification(
                    notiTitle,
                    notiMsg,
                    (System.currentTimeMillis() % 10000).toInt(),
                    notiType
                )
            }
        }
    }
}