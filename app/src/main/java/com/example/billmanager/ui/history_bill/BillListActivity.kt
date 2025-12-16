package com.example.billmanager.ui.history_bill

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.example.billmanager.data.repository.HoaDonRepository
import com.example.billmanager.ui.input.InputBillActivity
import com.google.android.material.chip.Chip


class BillListActivity : AppCompatActivity() {

    private lateinit var rvHoaDon: RecyclerView
    private lateinit var adapter: HoaDonAdapter
    private lateinit var btnAdd: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnAnalytics: ImageButton

    // Filter Chips
    private lateinit var chipAll: Chip
    private lateinit var chipDien: Chip
    private lateinit var chipNuoc: Chip
    private lateinit var chipUnpaid: Chip

    private lateinit var repository: HoaDonRepository
    private var originalList = listOf<HoaDonEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_list)

        val db = AppDatabase.getInstance(this)
        repository = HoaDonRepository(db.hoaDonDao())

        setControl()
        setEvent()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setControl() {
        rvHoaDon = findViewById(R.id.rvHoaDon)
        btnAdd = findViewById(R.id.btnAdd)
        btnBack = findViewById(R.id.btnBack)
        btnAnalytics = findViewById(R.id.btnAnalytics)

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
        btnAdd.setOnClickListener {
            startActivity(Intent(this, InputBillActivity::class.java))
        }

        btnAnalytics.setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }

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

    private fun showDetailDialog(hd: HoaDonEntity) {
        val intent = Intent(this, BillDetailActivity::class.java)
        intent.putExtra("BILL_DATA", hd)
        startActivity(intent)
    }
}