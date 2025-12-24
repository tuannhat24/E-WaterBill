package com.example.billmanager.ui.location

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.LocationEntity
import com.example.billmanager.data.local.dao.LocationDao
import com.example.billmanager.utils.BackupHelper
import com.example.billmanager.utils.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private lateinit var userSession: UserSession

    // Trình chọn file import
    private val importLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    val msg = BackupHelper.importFromUri(this, uri)
                    showAlert(msg)
                    loadData()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        db = AppDatabase.getInstance(this)
        locationDao = db.locationDao()
        userSession = UserSession(this)

        // Kiểm tra và tạo dữ liệu mặc định CHO USER NÀY nếu chưa có
        val email = userSession.getUserEmail() ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            if (locationDao.countByUser(email) == 0) {
                locationDao.insert(
                    LocationEntity(
                        name = "Nhà riêng",
                        address = "Mặc định",
                        type = "Nhà riêng",
                        isSelected = true,
                        userEmail = email
                    )
                )
            }
            // Sau khi insert xong mới load data
            withContext(Dispatchers.Main) {
                setControl()
                loadData()
                setEvent()
            }
        }
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
        val email = userSession.getUserEmail() ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            // 1. Lấy danh sách địa điểm của User
            val list = locationDao.getLocationsByUser(email)

            withContext(Dispatchers.Main) {
                adapter.updateList(list)

                // 2. Cập nhật Dashboard
                val selected = list.find { it.isSelected } ?: list.firstOrNull()
                if (selected != null) {
                    tvCurrentLocationName.text = "Đang chọn: ${selected.name} (${selected.type})"
                    updateStats(selected.id, email)
                } else {
                    tvCurrentLocationName.text = "Chưa chọn địa điểm"
                    tvLocationStats.text = "---"
                }
            }
        }
    }

    private fun updateStats(locationId: Int, email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // Lấy hóa đơn của User tại địa điểm này
            val bills = db.hoaDonDao().getBillsByUser(email).filter { it.locationId == locationId }

            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val thisMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1

            val totalYear = bills.filter { it.nam == currentYear }.sumOf { it.tongTien }
            val totalMonth = bills.filter { it.nam == currentYear && it.thang == thisMonth }
                .sumOf { it.tongTien }

            withContext(Dispatchers.Main) {
                val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                tvLocationStats.text = "Chi phí T$thisMonth: ${formatVND.format(totalMonth)}\n" +
                        "Tổng chi phí năm $currentYear: ${formatVND.format(totalYear)}\n" +
                        "Tổng số hóa đơn: ${bills.size}"
            }
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
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            importLauncher.launch(intent)
        }

        btnCleanStorage.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Dọn dẹp")
                .setMessage("Xóa tất cả hóa đơn cũ hơn 2 năm?")
                .setPositiveButton("Xóa") { _, _ ->
                    val count = BackupHelper.deleteOldData(this)
                    Toast.makeText(this, "Đã xóa $count hóa đơn cũ.", Toast.LENGTH_SHORT).show()
                    loadData()
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
                    val email = userSession.getUserEmail() ?: ""
                    CoroutineScope(Dispatchers.IO).launch {
                        if (loc == null) {
                            locationDao.insert(
                                LocationEntity(
                                    name = name,
                                    address = address,
                                    type = type,
                                    userEmail = email
                                )
                            )
                        } else {
                            locationDao.update(
                                loc.copy(
                                    name = name,
                                    address = address,
                                    type = type
                                )
                            )
                        }
                        runOnUiThread { loadData() }
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun handleLocationAction(loc: LocationEntity, action: String) {
        val email = userSession.getUserEmail() ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            when (action) {
                "SELECT" -> {
                    locationDao.unselectAllByUser(email) // Chỉ bỏ chọn của user này
                    locationDao.update(loc.copy(isSelected = true))
                    runOnUiThread {
                        loadData()
                        Toast.makeText(
                            this@LocationActivity,
                            "Đã chuyển sang: ${loc.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                "EDIT" -> runOnUiThread { showAddEditDialog(loc) }
                "DELETE" -> {
                    // Check ràng buộc hóa đơn
                    val count =
                        db.hoaDonDao().getBillsByUser(email).count { it.locationId == loc.id }
                    runOnUiThread {
                        if (count > 0) {
                            showAlert("Không thể xóa! Địa điểm này đang chứa $count hóa đơn.")
                        } else {
                            CoroutineScope(Dispatchers.IO).launch {
                                locationDao.delete(loc)
                                runOnUiThread { loadData() }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showAlert(msg: String) {
        AlertDialog.Builder(this).setMessage(msg).setPositiveButton("OK", null).show()
    }
}