package com.example.billmanager.ui.history_bill

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.example.billmanager.data.repository.HoaDonRepository
import com.example.billmanager.ui.history_bill.HoaDonAdapter
import com.example.billmanager.ui.input.InputBillActivity
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.util.Locale

class BillListActivity : AppCompatActivity() {

    private lateinit var rvHoaDon: RecyclerView
    private lateinit var adapter: HoaDonAdapter
    private lateinit var btnAdd: ImageButton
    private lateinit var btnBack: ImageButton

    // Filter Chips
    private lateinit var chipAll: Chip
    private lateinit var chipDien: Chip
    private lateinit var chipNuoc: Chip
    private lateinit var chipUnpaid: Chip

    private lateinit var repository: HoaDonRepository
    private var originalList = listOf<HoaDonEntity>() // Danh sách gốc

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_list)

        // Init Repo
        val db = AppDatabase.getInstance(this)
        repository = HoaDonRepository(db.hoaDonDao())

        setControl()
        setEvent()
    }

    override fun onResume() {
        super.onResume()
        loadData() // Load lại dữ liệu mỗi khi quay lại màn hình
    }

    private fun setControl() {
        rvHoaDon = findViewById(R.id.rvHoaDon)
        btnAdd = findViewById(R.id.btnAdd)
        btnBack = findViewById(R.id.btnBack)

        chipAll = findViewById(R.id.chipAll)
        chipDien = findViewById(R.id.chipDien)
        chipNuoc = findViewById(R.id.chipNuoc)
        chipUnpaid = findViewById(R.id.chipUnpaid)

        // Setup RecyclerView
        adapter = HoaDonAdapter(mutableListOf()) { hoaDon ->
            showDetailDialog(hoaDon) // <--- GỌI DIALOG CHI TIẾT
        }
        rvHoaDon.layoutManager = LinearLayoutManager(this)
        rvHoaDon.adapter = adapter
    }

    private fun setEvent() {
        // Nút Thêm mới
        btnAdd.setOnClickListener {
            startActivity(Intent(this, InputBillActivity::class.java))
        }

        // Nút Quay lại
        btnBack.setOnClickListener { finish() }

        // Sự kiện bộ lọc (Filter)
        chipAll.setOnClickListener { filterList("ALL") }
        chipDien.setOnClickListener { filterList("Điện") }
        chipNuoc.setOnClickListener { filterList("Nước") }
        chipUnpaid.setOnClickListener { filterList("Unpaid") }
    }

    private fun loadData() {
        originalList = repository.getAll()
        // Sắp xếp: Mới nhất lên đầu
        originalList = originalList.sortedWith(compareByDescending<HoaDonEntity> { it.nam }.thenByDescending { it.thang })

        // Mặc định hiển thị tất cả
        chipAll.isChecked = true
        adapter.updateList(originalList)
    }

    private fun filterList(type: String) {
        val filtered = when(type) {
            "Điện" -> originalList.filter { it.loai == "Điện" }
            "Nước" -> originalList.filter { it.loai == "Nước" }
            "Unpaid" -> originalList.filter { it.trangThai == "Chưa thanh toán" }
            else -> originalList // ALL
        }
        adapter.updateList(filtered)
    }

    // --- LOGIC HIỂN THỊ DIALOG CHI TIẾT ---
    private fun showDetailDialog(hd: HoaDonEntity) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_bill_detail, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        // 1. Ánh xạ View trong Dialog
        val tvLoai = view.findViewById<TextView>(R.id.tvLoai)
        val tvNgayGio = view.findViewById<TextView>(R.id.tvNgayGio)
        val tvChiSo = view.findViewById<TextView>(R.id.tvChiSo)
        val tvDonVi = view.findViewById<TextView>(R.id.tvDonVi)
        val tvTongTien = view.findViewById<TextView>(R.id.tvTongTien)
        val tvTrangThai = view.findViewById<TextView>(R.id.tvTrangThai)

        val btnDong = view.findViewById<Button>(R.id.btnDong)
        val btnXoa = view.findViewById<Button>(R.id.btnXoa)
        val btnSua = view.findViewById<Button>(R.id.btnSua)

        // 2. Gán dữ liệu
        tvLoai.text = "Hóa đơn tiền ${hd.loai} - T${hd.thang}/${hd.nam}"
        tvNgayGio.text = "Ngày tạo: ${hd.gio} ${hd.ngay}"
        tvChiSo.text = "Chỉ số: ${hd.chiSoDau} ➝ ${hd.chiSoCuoi}"

        val donVi = if (hd.loai == "Điện") "kWh" else "m³"
        tvDonVi.text = "Tiêu thụ: ${hd.soLuong} $donVi"

        val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        tvTongTien.text = "Tổng tiền: ${formatVND.format(hd.tongTien)}"
        tvTrangThai.text = hd.trangThai

        // 3. Xử lý sự kiện nút trong Dialog
        btnDong.setOnClickListener { dialog.dismiss() }

        btnXoa.setOnClickListener {
            // Xóa database
            repository.delete(hd)
            Toast.makeText(this, "Đã xóa hóa đơn", Toast.LENGTH_SHORT).show()
            loadData() // Load lại list
            dialog.dismiss()
        }

        btnSua.setOnClickListener {
            Toast.makeText(this, "Chức năng Sửa đang cập nhật", Toast.LENGTH_SHORT).show()
            // TODO: Mở InputBillActivity và truyền dữ liệu cũ sang để sửa
            // val intent = Intent(this, InputBillActivity::class.java)
            // intent.putExtra("BILL_ID", hd.id) ...
            // startActivity(intent)
        }

        dialog.show()
    }
}