package com.example.billmanager.ui.history_bill

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.example.billmanager.data.repository.HoaDonRepository
import com.example.billmanager.ui.input.InputBillActivity
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.util.*

// --- Activity hiển thị danh sách hóa đơn ---
class BillListActivity : AppCompatActivity() {

    // --- View Controls ---
    private lateinit var rvHoaDon: RecyclerView       // RecyclerView danh sách hóa đơn
    private lateinit var adapter: HoaDonAdapter       // Adapter hiển thị từng hóa đơn
    private lateinit var btnAdd: ImageButton          // Nút thêm hóa đơn mới
    private lateinit var btnBack: ImageButton         // Nút quay lại
    private lateinit var chipAll: Chip                // Chip lọc tất cả
    private lateinit var chipDien: Chip               // Chip lọc điện
    private lateinit var chipNuoc: Chip               // Chip lọc nước
    private lateinit var chipUnpaid: Chip             // Chip lọc chưa thanh toán
    private lateinit var chipPaid: Chip               // Chip lọc đã thanh toán
    private lateinit var edtThang: EditText           // Input tháng
    private lateinit var edtNam: EditText             // Input năm
    private lateinit var btnSearch: Button            // Nút tìm kiếm theo tháng/năm
    private lateinit var layoutEmpty: LinearLayout    // Layout hiển thị khi danh sách rỗng

    // --- Repository & Data ---
    private lateinit var repository: HoaDonRepository
    private var originalList = listOf<HoaDonEntity>() // Danh sách hóa đơn gốc
    private var currentFilter = "ALL"                 // Bộ lọc hiện tại

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_list)

        val db = AppDatabase.getInstance(this)
        repository = HoaDonRepository(db.hoaDonDao())  // Khởi tạo repository

        setControl()  // Gán view
        setEvent()    // Gán sự kiện
    }

    override fun onResume() {
        super.onResume()
        loadData()   // Tải dữ liệu mỗi lần activity hiện ra
    }

    // --- Gán các control ---
    private fun setControl() {
        rvHoaDon = findViewById(R.id.rvHoaDon)
        btnAdd = findViewById(R.id.btnAdd)
        btnBack = findViewById(R.id.btnBack)
        chipAll = findViewById(R.id.chipAll)
        chipDien = findViewById(R.id.chipDien)
        chipNuoc = findViewById(R.id.chipNuoc)
        chipUnpaid = findViewById(R.id.chipUnpaid)
        chipPaid = findViewById(R.id.chipPaid)
        edtThang = findViewById(R.id.edtThang)
        edtNam = findViewById(R.id.edtNam)
        btnSearch = findViewById(R.id.btnSearch)
        layoutEmpty = findViewById(R.id.layoutEmpty)

        // Khởi tạo adapter với callback click vào hóa đơn
        adapter = HoaDonAdapter(mutableListOf()) { hd -> showDetailDialog(hd) }
        rvHoaDon.layoutManager = LinearLayoutManager(this)
        rvHoaDon.adapter = adapter
    }

    // --- Gán sự kiện ---
    private fun setEvent() {
        // Thêm hóa đơn mới
        btnAdd.setOnClickListener {
            startActivity(Intent(this, InputBillActivity::class.java))
        }
        // Quay lại
        btnBack.setOnClickListener { finish() }

        // Lọc theo loại hoặc trạng thái
        chipAll.setOnClickListener { filterList("ALL") }
        chipDien.setOnClickListener { filterList("Điện") }
        chipNuoc.setOnClickListener { filterList("Nước") }
        chipUnpaid.setOnClickListener { filterList("Unpaid") }
        chipPaid.setOnClickListener { filterList("Paid") }

        // Tìm kiếm theo tháng/năm
        btnSearch.setOnClickListener {
            val thang = edtThang.text.toString().toIntOrNull()
            val nam = edtNam.text.toString().toIntOrNull()
            val filtered = originalList.filter {
                (thang == null || it.thang == thang) &&
                        (nam == null || it.nam == nam)
            }
            adapter.updateList(filtered)
            updateEmptyView(filtered)
        }
    }

    // --- Load danh sách hóa đơn từ database ---
    private fun loadData() {
        originalList = repository.getAll()
            .sortedWith(compareByDescending<HoaDonEntity> { it.nam }
                .thenByDescending { it.thang }
                .thenBy { it.loai })  // Sắp xếp: năm giảm dần → tháng giảm dần → loại
        filterList(currentFilter) // Áp dụng bộ lọc hiện tại
    }

    // --- Lọc danh sách theo loại hoặc trạng thái ---
    private fun filterList(type: String) {
        currentFilter = type
        val filtered = when (type) {
            "Điện" -> originalList.filter { it.loai == "Điện" }
            "Nước" -> originalList.filter { it.loai == "Nước" }
            "Unpaid" -> originalList.filter { it.trangThai == "Chưa thanh toán" }
            "Paid" -> originalList.filter { it.trangThai == "Đã thanh toán" }
            else -> originalList
        }
        adapter.updateList(filtered)
        updateEmptyView(filtered)
    }

    // --- Hiển thị layout "trống" nếu danh sách rỗng ---
    private fun updateEmptyView(list: List<HoaDonEntity>) {
        layoutEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    // --- Hiển thị chi tiết hóa đơn ---
    private fun showDetailDialog(hd: HoaDonEntity) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_bill_detail, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        // Gán các view hiển thị thông tin hóa đơn
        val tvLoai = view.findViewById<TextView>(R.id.tvLoai)
        val tvNgayGio = view.findViewById<TextView>(R.id.tvNgayGio)
        val tvChiSo = view.findViewById<TextView>(R.id.tvChiSo)
        val tvDonVi = view.findViewById<TextView>(R.id.tvDonVi)
        val tvTongTien = view.findViewById<TextView>(R.id.tvTongTien)
        val tvTrangThai = view.findViewById<TextView>(R.id.tvTrangThai)
        val btnDong = view.findViewById<Button>(R.id.btnDong)
        val btnSua = view.findViewById<Button>(R.id.btnSua)
        val btnXoa = view.findViewById<Button>(R.id.btnXoa)

        // Hiển thị thông tin hóa đơn
        tvLoai.text = "Hóa đơn tiền ${hd.loai}"
        tvNgayGio.text = "Ngày: ${hd.ngay} • ${hd.gio}"
        tvChiSo.text = "Chỉ số: ${hd.chiSoDau} → ${hd.chiSoCuoi}"
        val donVi = if (hd.loai == "Điện") "kWh" else "m³"
        tvDonVi.text = "Khối lượng: ${hd.soLuong} $donVi"
        tvTongTien.text = "Tổng tiền: ${formatTien(hd.tongTien)}"
        tvTrangThai.text = hd.trangThai

        // Nếu đã thanh toán thì không cho sửa
        if (hd.trangThai == "Đã thanh toán") {
            btnSua.isEnabled = false
            btnSua.alpha = 0.4f
        } else {
            btnSua.setOnClickListener {
                dialog.dismiss()
                val intent = Intent(this, InputBillActivity::class.java)
                intent.putExtra("EDIT_BILL", hd) // Truyền hóa đơn để sửa
                startActivity(intent)
            }
        }

        // Xóa hóa đơn
        btnXoa.setOnClickListener {
            dialog.dismiss()
            AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn xóa hóa đơn này?")
                .setPositiveButton("Xóa") { _, _ ->
                    repository.delete(hd)  // Xóa trong database
                    loadData()              // Tải lại danh sách
                    Toast.makeText(this, "Đã xóa hóa đơn", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
        // Đóng dialog
        btnDong.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    // --- Format tiền Việt Nam ---
    private fun formatTien(value: Long) =
        NumberFormat.getInstance(Locale("vi", "VN")).format(value) + "đ"
}
