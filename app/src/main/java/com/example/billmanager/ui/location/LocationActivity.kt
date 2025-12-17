package com.example.billmanager.ui.location

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.LocationEntity
import com.example.billmanager.data.local.dao.LocationDao
import com.example.billmanager.utils.BackupHelper
import java.text.NumberFormat
import java.util.Locale

class LocationActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var tvCurrentLocationName: TextView
    private lateinit var tvLocationStats: TextView
    private lateinit var btnAddLocation: Button
    private lateinit var rvLocations: RecyclerView
    private lateinit var btnBackup: Button
    private lateinit var btnRestore: Button
    private lateinit var btnCleanStorage: Button

    private lateinit var db: AppDatabase
    private lateinit var locationDao: LocationDao
    private lateinit var adapter: LocationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        db = AppDatabase.getInstance(this)
        locationDao = db.locationDao()

        // Tạo dữ liệu mặc định nếu chưa có
        if (locationDao.count() == 0) {
            locationDao.insert(
                LocationEntity(
                    name = "Nhà riêng",
                    address = "Mặc định",
                    type = "Nhà riêng",
                    isSelected = true
                )
            )
        }

        setControl()
        loadData()
        setEvent()
    }

    private fun setControl() {
        tvTitle = findViewById(R.id.tvTitle)
        tvTitle.text = "Quản lý Địa điểm & Backup"
        btnBack = findViewById(R.id.btnBack)

        tvCurrentLocationName = findViewById(R.id.tvCurrentLocationName)
        tvLocationStats = findViewById(R.id.tvLocationStats)
        btnAddLocation = findViewById(R.id.btnAddLocation)
        rvLocations = findViewById(R.id.rvLocations)
        btnBackup = findViewById(R.id.btnBackup)
        btnRestore = findViewById(R.id.btnRestore)
        btnCleanStorage = findViewById(R.id.btnCleanStorage)

        adapter = LocationAdapter(mutableListOf()) { loc, action ->
            handleLocationAction(loc, action)
        }
        rvLocations.layoutManager = LinearLayoutManager(this)
        rvLocations.adapter = adapter
    }

    private fun loadData() {
        val list = locationDao.getAll()
        adapter.updateList(list)

        // Cập nhật Dashboard cho địa điểm đang chọn
        val selected = list.find { it.isSelected } ?: list.firstOrNull()
        if (selected != null) {
            tvCurrentLocationName.text = "Đang chọn: ${selected.name} (${selected.type})"

            // Tính toán sơ bộ (Overview)
            val bills = db.hoaDonDao().getAll().filter { it.locationId == selected.id }
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val thisMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1

            val totalYear = bills.filter { it.nam == currentYear }.sumOf { it.tongTien }
            val totalMonth = bills.filter { it.nam == currentYear && it.thang == thisMonth }
                .sumOf { it.tongTien }

            val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            tvLocationStats.text = "Chi phí T$thisMonth: ${formatVND.format(totalMonth)}\n" +
                    "Tổng chi phí năm $currentYear: ${formatVND.format(totalYear)}\n" +
                    "Tổng số hóa đơn: ${bills.size}"
        }
    }

    private fun setEvent() {
        btnBack.setOnClickListener { finish() }

        btnAddLocation.setOnClickListener {
            showAddEditDialog(null)
        }

        btnBackup.setOnClickListener {
            val msg = BackupHelper.exportData(this)
            showAlert(msg)
        }

        btnRestore.setOnClickListener {
            val msg = BackupHelper.importData(this)
            showAlert(msg)
            loadData() // Load lại UI sau khi import
        }

        btnCleanStorage.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Dọn dẹp")
                .setMessage("Xóa tất cả hóa đơn cũ hơn 2 năm?")
                .setPositiveButton("Xóa") { _, _ ->
                    val count = BackupHelper.deleteOldData(this)
                    Toast.makeText(this, "Đã xóa $count hóa đơn cũ.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun showAddEditDialog(loc: LocationEntity?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_location, null)
        val edtName = view.findViewById<EditText>(R.id.edtName)
        val edtAddress = view.findViewById<EditText>(R.id.edtAddress)
        val spnType = view.findViewById<Spinner>(R.id.spnType)

        // Setup Spinner
        val types = arrayOf("Nhà riêng", "Chung cư", "Phòng trọ", "Khác")
        spnType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        if (loc != null) {
            edtName.setText(loc.name)
            edtAddress.setText(loc.address)
            val idx = types.indexOf(loc.type)
            if (idx >= 0) spnType.setSelection(idx)
        }

        AlertDialog.Builder(this)
            .setTitle(if (loc == null) "Thêm Địa điểm" else "Sửa Địa điểm")
            .setView(view)
            .setPositiveButton("Lưu") { _, _ ->
                val name = edtName.text.toString()
                val address = edtAddress.text.toString()
                val type = spnType.selectedItem.toString()

                if (name.isNotEmpty()) {
                    if (loc == null) {
                        locationDao.insert(
                            LocationEntity(
                                name = name,
                                address = address,
                                type = type
                            )
                        )
                    } else {
                        locationDao.update(loc.copy(name = name, address = address, type = type))
                    }
                    loadData()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun handleLocationAction(loc: LocationEntity, action: String) {
        when (action) {
            "SELECT" -> {
                locationDao.unselectAll()
                locationDao.update(loc.copy(isSelected = true))
                loadData()
                Toast.makeText(this, "Đã chuyển sang: ${loc.name}", Toast.LENGTH_SHORT).show()
            }

            "EDIT" -> showAddEditDialog(loc)
            "DELETE" -> {
                // Check xem có hóa đơn không trước khi xóa (Constraint)
                val count = db.hoaDonDao().getAll().count { it.locationId == loc.id }
                if (count > 0) {
                    showAlert("Không thể xóa! Địa điểm này đang chứa $count hóa đơn.")
                } else {
                    locationDao.delete(loc)
                    loadData()
                }
            }
        }
    }

    private fun showAlert(msg: String) {
        AlertDialog.Builder(this).setMessage(msg).setPositiveButton("OK", null).show()
    }

    // INNER ADAPTER CLASS
    inner class LocationAdapter(
        private var list: List<LocationEntity>,
        private val onAction: (LocationEntity, String) -> Unit
    ) : RecyclerView.Adapter<LocationAdapter.VH>() {

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(android.R.id.text1)
            val tvAddress: TextView = v.findViewById(android.R.id.text2)
            val btnSwitch: Button = v.findViewById(R.id.btnSwitch)
            val btnEdit: ImageButton = v.findViewById(R.id.btnEdit)
            val btnDelete: ImageButton = v.findViewById(R.id.btnDelete)
            val root: View = v.findViewById(R.id.rootLayout)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = list[position]
            holder.tvName.text = item.name
            holder.tvAddress.text = "${item.address} - ${item.type}"

            if (item.isSelected) {
                holder.root.setBackgroundColor(android.graphics.Color.parseColor("#E0F2F1"))
                holder.btnSwitch.text = "Đang chọn"
                holder.btnSwitch.isEnabled = false
            } else {
                holder.root.setBackgroundColor(android.graphics.Color.WHITE)
                holder.btnSwitch.text = "Chọn"
                holder.btnSwitch.isEnabled = true
            }

            holder.btnSwitch.setOnClickListener { onAction(item, "SELECT") }
            holder.btnEdit.setOnClickListener { onAction(item, "EDIT") }
            holder.btnDelete.setOnClickListener { onAction(item, "DELETE") }
        }

        override fun getItemCount() = list.size
        fun updateList(newList: List<LocationEntity>) {
            list = newList; notifyDataSetChanged()
        }
    }
}