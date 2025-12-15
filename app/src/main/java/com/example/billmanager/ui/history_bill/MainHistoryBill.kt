package com.example.billmanager.ui.history_bill



import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.example.billmanager.data.model.HoaDonThang
import com.example.billmanager.ui.HoaDonAdapter
import com.example.billmanager.ui.tinhtiendiennuoc.MainTinhTienDienNuoc

class MainHistoryBill : AppCompatActivity() {

    // ===== RECYCLER VIEW =====
    private lateinit var rvHoaDon: RecyclerView          // Hiển thị danh sách hóa đơn
    private lateinit var adapter: HoaDonAdapter          // Adapter cho RecyclerView

    // ===== BUTTON LỌC =====
    private lateinit var btnAll: Button                  // Hiển thị tất cả
    private lateinit var btnDien: Button                 // Lọc hóa đơn điện
    private lateinit var btnNuoc: Button                 // Lọc hóa đơn nước
    private lateinit var btnChuaThanhToan: Button        // Lọc chưa thanh toán
    private lateinit var btnDaThanhToan: Button          // Lọc đã thanh toán

    // ===== TÌM KIẾM THEO THÁNG / NĂM =====
    private lateinit var edtThang: EditText
    private lateinit var edtNam: EditText
    private lateinit var btnSearch: Button

    // ===== PHÂN TRANG =====
    private lateinit var llPages: LinearLayout            // Layout chứa số trang
    private lateinit var btnBack: Button                  // Nút quay lại
    private lateinit var btnThanhToan: Button             // Nút tạo / thanh toán hóa đơn

    // ===== DANH SÁCH DỮ LIỆU =====
    private var listHoaDon = mutableListOf<HoaDonEntity>()      // Danh sách hóa đơn gốc
    private var filteredList = mutableListOf<HoaDonEntity>()    // Danh sách sau khi lọc
    private var listThang = mutableListOf<HoaDonThang>()  // Gom hóa đơn theo tháng

    // ===== PHÂN TRANG =====
    private var page = 1                                  // Trang hiện tại
    private val limit = 4                                 // Số item / trang
    private var totalPage = 1                             // Tổng số trang

    private lateinit var btnFirst: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnLast: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_history_bill)

        mappingView()
        taoDuLieuAo()
        setupRecycler()

        btnFirst.setOnClickListener {
            page = 1
            updatePage()
        }

        btnPrev.setOnClickListener {
            if (page > 1) {
                page--
                updatePage()
            }
        }

        btnNext.setOnClickListener {
            if (page < totalPage) {
                page++
                updatePage()
            }
        }

        btnLast.setOnClickListener {
            page = totalPage
            updatePage()
        }

        setupEvents()

        btnBack.setOnClickListener { finish() }
        btnThanhToan.setOnClickListener {
            showMonthYearPicker()
        }
    }

    //============================
    // ÁNH XẠ VIEW
    //============================
    private fun mappingView() {

        // RecyclerView danh sách hóa đơn
        rvHoaDon = findViewById(R.id.rvHoaDon)

        // Button lọc
        btnAll = findViewById(R.id.btnAll)
        btnDien = findViewById(R.id.btnDien)
        btnNuoc = findViewById(R.id.btnNuoc)
        btnChuaThanhToan = findViewById(R.id.btnChuaThanhToan)
        btnDaThanhToan = findViewById(R.id.btnDaThanhToan)

        // Tìm kiếm tháng / năm
        edtThang = findViewById(R.id.edtThang)
        edtNam = findViewById(R.id.edtNam)
        btnSearch = findViewById(R.id.btnSearch)

        // Phân trang
        llPages = findViewById(R.id.llPages)
        btnFirst = findViewById(R.id.btnFirst)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        btnLast = findViewById(R.id.btnLast)

        // Điều hướng
        btnBack = findViewById(R.id.btnBack)
        btnThanhToan = findViewById(R.id.btnThanhToan)
    }


    //============================
    // DỮ LIỆU GIẢ
    //============================
    private fun taoDuLieuAo() {

        val ds = mutableListOf<HoaDonEntity>(
            // ===== THÁNG 10 / 2025 =====
            fakeHoaDon(1, "Điện", 10, 2025, 2453, 2628, "10:20", "02/10/2025", "Chưa thanh toán"),
            fakeHoaDon(2, "Nước", 10, 2025, 346, 362, "14:22", "05/10/2025", "Đã thanh toán"),

            // ===== THÁNG 9 / 2025 =====
            fakeHoaDon(3, "Điện", 9, 2025, 2300, 2450, "08:12", "01/09/2025", "Đã thanh toán"),
            fakeHoaDon(4, "Nước", 9, 2025, 320, 340, "09:45", "03/09/2025", "Chưa thanh toán"),

            // ===== THÁNG 8 / 2025 =====
            fakeHoaDon(5, "Điện", 8, 2025, 2100, 2240, "11:30", "10/08/2025", "Đã thanh toán"),
            fakeHoaDon(6, "Nước", 8, 2025, 300, 315, "15:10", "15/08/2025", "Chưa thanh toán"),

            // ===== THÁNG 7 / 2025 =====
            fakeHoaDon(7, "Điện", 7, 2025, 2000, 2160, "07:55", "05/07/2025", "Đã thanh toán"),
            fakeHoaDon(8, "Nước", 7, 2025, 280, 300, "16:40", "18/07/2025", "Đã thanh toán"),

            // ===== THÁNG 6 / 2025 =====
            fakeHoaDon(9, "Điện", 6, 2025, 1850, 2000, "10:05", "09/06/2025", "Chưa thanh toán"),
            fakeHoaDon(10, "Nước", 6, 2025, 250, 265, "13:20", "20/06/2025", "Đã thanh toán")
        )

        // ✅ SẮP XẾP
        listHoaDon.clear()
        listHoaDon.addAll(
            ds.sortedWith(
                compareByDescending<HoaDonEntity> { it.nam }
                    .thenByDescending { it.thang }
                    .thenBy { it.loai }
            )
        )

        // ===== DANH SÁCH THÁNG =====
        listThang.clear()
        listHoaDon
            .groupBy { it.thang to it.nam }
            .forEach { (key, bills) ->
                listThang.add(
                    HoaDonThang(
                        key.first,
                        key.second,
                        bills.find { it.loai == "Điện" },
                        bills.find { it.loai == "Nước" }
                    )
                )
            }

        filteredList.clear()
        filteredList.addAll(listHoaDon)
    }

    //============================
    // RECYCLER
    //============================
    private fun setupRecycler() {
        adapter = HoaDonAdapter(filteredList) {
            showDetail(it)
        }
        rvHoaDon.layoutManager = LinearLayoutManager(this)
        rvHoaDon.adapter = adapter
        updatePage()
    }



    //============================
    // SỰ KIỆN BUTTON
    //============================
    private fun setupEvents() {

        btnAll.setOnClickListener {
            filteredList = listHoaDon.toMutableList()
            page = 1
            updatePage()
        }

        btnDien.setOnClickListener {
            filteredList = listHoaDon.filter { it.loai == "Điện" }.toMutableList()
            page = 1
            updatePage()
        }

        btnNuoc.setOnClickListener {
            filteredList = listHoaDon.filter { it.loai == "Nước" }.toMutableList()
            page = 1
            updatePage()
        }

        btnChuaThanhToan.setOnClickListener {
            filteredList = listHoaDon
                .filter { it.trangThai == "Chưa thanh toán" }
                .toMutableList()
            page = 1
            updatePage()
        }

        btnDaThanhToan.setOnClickListener {
            filteredList = listHoaDon
                .filter { it.trangThai == "Đã thanh toán" }
                .toMutableList()
            page = 1
            updatePage()
        }

        btnSearch.setOnClickListener {
            val thang = edtThang.text.toString().toIntOrNull()
            val nam = edtNam.text.toString().toIntOrNull()

            filteredList = listHoaDon.filter {
                (thang == null || it.thang == thang) &&
                        (nam == null || it.nam == nam)
            }.toMutableList()

            page = 1
            updatePage()
        }
    }

    //============================
    // DIALOG CHI TIẾT
    //============================
    private fun showDetail(hd: HoaDonEntity) {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_bill_detail, null)

        view.findViewById<TextView>(R.id.tvLoai).text = "Hóa đơn tiền ${hd.loai}"
        view.findViewById<TextView>(R.id.tvNgayGio).text = "Thanh toán lúc ${hd.gio} • ${hd.ngay}"
        view.findViewById<TextView>(R.id.tvChiSo).text =
            "Chỉ số: ${hd.chiSoDau} → ${hd.chiSoCuoi}"
        val donVi = if (hd.loai == "Điện") "kWh" else "m³"
        view.findViewById<TextView>(R.id.tvDonVi).text =
            "Khối lượng: ${hd.soLuong} $donVi"
        view.findViewById<TextView>(R.id.tvTongTien).text =  "Tổng tiền:\n${formatTien(hd.tongTien)}"
        view.findViewById<TextView>(R.id.tvTrangThai).text = hd.trangThai

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        view.findViewById<Button>(R.id.btnDong).setOnClickListener {
            dialog.dismiss()
        }

        val btnSua = view.findViewById<Button>(R.id.btnSua)

        if (hd.trangThai == "Đã thanh toán") {
            btnSua.isEnabled = false
            btnSua.alpha = 0.4f
        } else {
            btnSua.setOnClickListener {
                dialog.dismiss()

                val thangData = listThang.find {
                    it.thang == hd.thang && it.nam == hd.nam
                }

                val intent = Intent(this, MainTinhTienDienNuoc::class.java)
                intent.putExtra("THANG", hd.thang)
                intent.putExtra("NAM", hd.nam)
                intent.putExtra("DIEN", thangData?.dien)
                intent.putExtra("NUOC", thangData?.nuoc)

                startActivityForResult(intent, 100)
            }
        }

        view.findViewById<Button>(R.id.btnXoa).setOnClickListener {
            dialog.dismiss()

            // 1️⃣ Xóa hóa đơn khỏi danh sách chính
            listHoaDon.remove(hd)
            filteredList.remove(hd)

            // 2️⃣ BUILD LẠI listThang (QUAN TRỌNG NHẤT)
            listThang.clear()
            listHoaDon
                .groupBy { it.thang to it.nam }
                .forEach { (key, bills) ->
                    listThang.add(
                        HoaDonThang(
                            key.first,
                            key.second,
                            bills.find { it.loai == "Điện" },
                            bills.find { it.loai == "Nước" }
                        )
                    )
                }

            // 3️⃣ Cập nhật lại giao diện
            page = 1
            updatePage()
        }

        dialog.show()
    }


    //============================
    // PHÂN TRANG
    //============================
    private fun updatePage() {
        val total = filteredList.size
        totalPage = if (total % limit == 0) total / limit else total / limit + 1
        if (totalPage == 0) totalPage = 1

        // 🔒 KHÓA PAGE
        if (page < 1) page = 1
        if (page > totalPage) page = totalPage

        llPages.removeAllViews()

        // ===== CHỈ HIỂN THỊ 1 SỐ TRANG =====
        val tv = TextView(this)
        tv.text = page.toString()
        tv.gravity = Gravity.CENTER
        tv.setPadding(24, 12, 24, 12)
        tv.setBackgroundResource(R.drawable.bg_page_active)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(8, 0, 8, 0)
        tv.layoutParams = params

        llPages.addView(tv)

        // ===== CẮT DỮ LIỆU THEO TRANG =====
        val start = (page - 1) * limit
        val end = minOf(start + limit, total)

        adapter.updateList(
            if (total == 0) mutableListOf()
            else filteredList.subList(start, end).toMutableList()
        )
    }

    private fun moManHinhTinhTien(thang: Int, nam: Int) {
        val thangData = listThang.find { it.thang == thang && it.nam == nam }

        // ❌ Nếu cả điện + nước đều đã thanh toán → không cho sửa
        if (thangData?.dien?.trangThai == "Đã thanh toán" &&
            thangData.nuoc?.trangThai == "Đã thanh toán") {

            Toast.makeText(this, "Tháng này đã thanh toán đầy đủ", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, MainTinhTienDienNuoc::class.java)
        intent.putExtra("THANG", thang)
        intent.putExtra("NAM", nam)
        intent.putExtra("DIEN", thangData?.dien)
        intent.putExtra("NUOC", thangData?.nuoc)

        startActivityForResult(intent, 100)
    }

    private fun xuLyThangDaCoHoaDon(thang: Int, nam: Int) {
        val thangData = listThang.find { it.thang == thang && it.nam == nam }

        // 👉 Chưa có hóa đơn → tạo mới
        if (thangData == null) {
            moManHinhTinhTien(thang, nam)
            return
        }

        val trangThaiDien = thangData.dien?.trangThai ?: "Chưa có"
        val trangThaiNuoc = thangData.nuoc?.trangThai ?: "Chưa có"

        // ❌ CẢ 2 ĐÃ THANH TOÁN → KHÔNG CHO SỬA
        if (trangThaiDien == "Đã thanh toán" && trangThaiNuoc == "Đã thanh toán") {
            Toast.makeText(
                this,
                "Tháng $thang/$nam đã thanh toán đầy đủ",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // ✅ THÔNG BÁO CHO NGƯỜI DÙNG BIẾT
        AlertDialog.Builder(this)
            .setTitle("Hóa đơn này đã có rồi")
            .setMessage(
                "Tháng $thang/$nam:\n" +
                        "- Điện: $trangThaiDien\n" +
                        "- Nước: $trangThaiNuoc"
            )
            .setPositiveButton("Tiếp tục") { _, _ ->
                moManHinhTinhTien(thang, nam)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showMonthYearPicker() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 10)
        }

        val edtThangInput = EditText(this).apply {
            hint = "Nhập tháng "
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val edtNamInput = EditText(this).apply {
            hint = "Nhập năm "
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(edtThangInput)
        layout.addView(edtNamInput)

        AlertDialog.Builder(this)
            .setTitle("Chọn tháng / năm")
            .setView(layout)
            .setPositiveButton("Xác nhận") { _, _ ->
                val thang = edtThangInput.text.toString().toIntOrNull()
                val nam = edtNamInput.text.toString().toIntOrNull()

                if (thang == null || nam == null || thang !in 1..12) {
                    Toast.makeText(this, "Tháng/năm không hợp lệ", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                xuLyThangDaCoHoaDon(thang, nam)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {

            val msg = data.getStringExtra("MSG")
            Toast.makeText(this, msg ?: "Đã lưu hóa đơn", Toast.LENGTH_SHORT).show()

            val dienMoi = data.getSerializableExtra("DIEN_NEW") as? HoaDonEntity
            val nuocMoi = data.getSerializableExtra("NUOC_NEW") as? HoaDonEntity

            // ===== XỬ LÝ ĐIỆN =====
            dienMoi?.let { newDien ->
                listHoaDon.removeAll {
                    it.loai == "Điện" &&
                            it.thang == newDien.thang &&
                            it.nam == newDien.nam
                }
                listHoaDon.add(newDien)
            }

            // ===== XỬ LÝ NƯỚC =====
            nuocMoi?.let { newNuoc ->
                listHoaDon.removeAll {
                    it.loai == "Nước" &&
                            it.thang == newNuoc.thang &&
                            it.nam == newNuoc.nam
                }
                listHoaDon.add(newNuoc)
            }

            // ✅ SẮP XẾP ĐÚNG THỨ TỰ (CHỈ LÀM 1 LẦN Ở ĐÂY)
            listHoaDon.sortWith(
                compareByDescending<HoaDonEntity> { it.nam }
                    .thenByDescending { it.thang }
                    .thenBy { it.loai }
            )

            // ===== CẬP NHẬT DANH SÁCH THÁNG =====
            listThang.clear()
            listHoaDon
                .groupBy { it.thang to it.nam }
                .forEach { (key, bills) ->
                    listThang.add(
                        HoaDonThang(
                            key.first,
                            key.second,
                            bills.find { it.loai == "Điện" },
                            bills.find { it.loai == "Nước" }
                        )
                    )
                }

            filteredList = listHoaDon.toMutableList()
            page = 1
            updatePage()
        }
    }

    // =============================
// GENERATE DỮ LIỆU ẢO CHUẨN
// =============================
    private fun tinhTienDienTuChiSo(start: Int, end: Int): Pair<Int, Long> {
        val soKwh = end - start
        val bac = intArrayOf(50, 50, 100, 100, 100, Int.MAX_VALUE)
        val gia = longArrayOf(1984, 2050, 2380, 2729, 3050, 3151)

        var remain = soKwh
        var totalNoTax = 0L

        for (i in bac.indices) {
            if (remain <= 0) break
            val use = minOf(remain, bac[i])
            totalNoTax += use * gia[i]
            remain -= use
        }

        val vat = totalNoTax * 8 / 100
        val total = totalNoTax + vat
        return Pair(soKwh, total)
    }

    private fun tinhTienNuocTuChiSo(start: Int, end: Int): Pair<Int, Long> {
        val soM3 = end - start
        val tienNuoc = soM3 * 6700L
        val tienDVTN = soM3 * 2010L
        val vatNuoc = tienNuoc * 5 / 100
        val vatDVTN = tienDVTN * 8 / 100
        val total = tienNuoc + tienDVTN + vatNuoc + vatDVTN
        return Pair(soM3, total)
    }

    private fun fakeHoaDon(
        id: Int,
        loai: String,
        thang: Int,
        nam: Int,
        chiSoDau: Int,
        chiSoCuoi: Int,
        gio: String,
        ngay: String,
        trangThai: String
    ): HoaDonEntity {
        return if (loai == "Điện") {
            val (sl, tong) = tinhTienDienTuChiSo(chiSoDau, chiSoCuoi)
            HoaDonEntity(id, loai, thang, nam, chiSoDau, chiSoCuoi, sl, tong, gio, ngay, trangThai)
        } else {
            val (sl, tong) = tinhTienNuocTuChiSo(chiSoDau, chiSoCuoi)
            HoaDonEntity(id, loai, thang, nam, chiSoDau, chiSoCuoi, sl, tong, gio, ngay, trangThai)
        }
    }

    private fun formatTien(v: Long): String {
        return java.text.NumberFormat
            .getInstance(java.util.Locale("vi", "VN"))
            .format(v) + "đ"
    }

}
