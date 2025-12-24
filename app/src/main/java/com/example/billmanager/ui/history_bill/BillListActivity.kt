package com.example.billmanager.ui.history_bill

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.example.billmanager.data.repository.HoaDonRepository
import com.example.billmanager.ui.input.InputBillActivity
import com.example.billmanager.utils.UserSession
import java.util.Calendar

class BillListActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnAdd: ImageButton
    private lateinit var btnFilter: ImageButton
    private lateinit var btnAnalytics: ImageButton
    private lateinit var rvHoaDon: RecyclerView
    private lateinit var tvEmpty: TextView

    private lateinit var adapter: HoaDonAdapter
    private lateinit var repository: HoaDonRepository
    private var originalList: List<HoaDonEntity> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_list)

        val db = AppDatabase.getInstance(this)
        repository = HoaDonRepository(db.hoaDonDao())

        setControl()
        checkRole()
        setEvent()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setControl() {
        tvTitle = findViewById(R.id.tvTitle)
        if (::tvTitle.isInitialized) tvTitle.text = "Lịch sử Hóa đơn"

        btnBack = findViewById(R.id.btnBack)
        btnAdd = findViewById(R.id.btnAdd)
        btnFilter = findViewById(R.id.btnFilter)
        btnAnalytics = findViewById(R.id.btnAnalytics)
        rvHoaDon = findViewById(R.id.rvHoaDon)
        tvEmpty = findViewById(R.id.tvEmpty)

        rvHoaDon.layoutManager = LinearLayoutManager(this)
        adapter = HoaDonAdapter(mutableListOf()) { bill ->
            val intent = Intent(this, BillDetailActivity::class.java)
            // Truyền object hoặc ID
            intent.putExtra("BILL_DATA", bill)
            startActivity(intent)
        }
        rvHoaDon.adapter = adapter
    }

    private fun checkRole() {
        val session = UserSession(this)
        val role = session.getUserRole()
        btnAdd.visibility = View.VISIBLE
        if (role == "Admin") {
            btnAdd.backgroundTintList = ColorStateList.valueOf(Color.RED)
        }
    }

    private fun setEvent() {
        btnBack.setOnClickListener { finish() }

        btnAdd.setOnClickListener {
            startActivity(Intent(this, InputBillActivity::class.java))
        }

        btnFilter.setOnClickListener {
            showFilterDialog()
        }

        btnAnalytics.setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }
    }

    private fun loadData() {
        val session = UserSession(this)
        val email = session.getUserEmail() ?: ""

        // Lấy hóa đơn của User hiện tại
        originalList = repository.getBillsByUser(email)

        // Sắp xếp: Mới nhất lên đầu
        originalList =
            originalList.sortedWith(compareByDescending<HoaDonEntity> { it.nam }.thenByDescending { it.thang })

        updateUI(originalList)
    }

    private fun updateUI(list: List<HoaDonEntity>) {
        if (list.isEmpty()) {
            if (::tvEmpty.isInitialized) tvEmpty.visibility = View.VISIBLE
            rvHoaDon.visibility = View.GONE
        } else {
            if (::tvEmpty.isInitialized) tvEmpty.visibility = View.GONE
            rvHoaDon.visibility = View.VISIBLE
            adapter.updateList(list)
        }
    }

    private fun showFilterDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_filter_bill, null)

        val spnMonth = view.findViewById<Spinner>(R.id.spnMonth)
        val spnYear = view.findViewById<Spinner>(R.id.spnYear)
        val spnType = view.findViewById<Spinner>(R.id.spnType)
        val spnStatus = view.findViewById<Spinner>(R.id.spnStatus)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilter)

        // Setup Spinner Tháng
        val months = listOf("Tất cả tháng") + (1..12).map { "Tháng $it" }
        spnMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)

        // Setup Spinner Năm
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = listOf("Tất cả năm") + (currentYear downTo 2020).map { it.toString() }
        spnYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)

        // Setup Spinner LOẠI (Điện/Nước)
        val types = listOf("Tất cả loại", "Điện", "Nước")
        spnType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        // Setup Spinner TRẠNG THÁI (Đã trả/Chưa trả)
        val statuses = listOf("Tất cả trạng thái", "Đã thanh toán", "Chưa thanh toán")
        spnStatus.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        btnApply.setOnClickListener {
            val monthIdx = spnMonth.selectedItemPosition // 0=All, 1=T1...
            val yearStr = spnYear.selectedItem.toString()
            val typeStr = spnType.selectedItem.toString()
            val statusStr = spnStatus.selectedItem.toString()

            var filtered = originalList

            // Lọc Tháng
            if (monthIdx > 0) {
                filtered = filtered.filter { it.thang == monthIdx }
            }

            // Lọc Năm
            if (yearStr != "Tất cả năm") {
                filtered = filtered.filter { it.nam == yearStr.toInt() }
            }

            // Lọc Loại (Điện/Nước)
            if (typeStr != "Tất cả loại") {
                filtered = filtered.filter { it.loai == typeStr }
            }

            // Lọc Trạng thái
            if (statusStr == "Đã thanh toán") {
                filtered = filtered.filter { it.trangThai == "Đã thanh toán" }
            } else if (statusStr == "Chưa thanh toán") {
                filtered = filtered.filter { it.trangThai == "Chưa thanh toán" }
            }

            updateUI(filtered)
            dialog.dismiss()
        }

        dialog.show()
    }
}