package com.example.billmanager.ui.auth.user_management

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.User
import com.example.billmanager.data.repository.HoaDonRepository
import java.text.NumberFormat
import java.util.Locale

class UserManagementMainActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var rvUsers: RecyclerView
    private lateinit var db: AppDatabase
    private lateinit var adapter: UserAdapter
    private lateinit var billRepository: HoaDonRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management_main)
        db = AppDatabase.getInstance(this)
        billRepository = HoaDonRepository(db.hoaDonDao())
        setControl()
        setEvent()
    }

    private fun setControl() {
        rvUsers = findViewById<RecyclerView>(R.id.rvUsers)
        tvTitle = findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "Quản lý người dùng"
        btnBack = findViewById<ImageButton>(R.id.btnBack)
    }

    private fun setEvent() {
        btnBack.setOnClickListener {
            finish()
        }
        adapter = UserAdapter(
            onLock = { toggleLock(it) },
            onClickUser = { showUserDetailStats(it) }
        )
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = adapter

        loadUsers()
    }

    private fun loadUsers() {
        val listUser = db.userDao().getAll()
        adapter.submitList(listUser)
    }

    private fun toggleLock(user: User) {
        if (user.role == "Admin") return

        val newStatus = !user.isActive
        db.userDao().updateStatus(user.id, newStatus)

        loadUsers() // QUAN TRỌNG
    }

    // hiển thị chi tiết & thống kê user
    private fun showUserDetailStats(user: User) {
        val userBills = billRepository.getBillsByUser(user.email)

        // Tính toán thống kê
        val totalBills = userBills.size
        val totalMoney = userBills.sumOf { it.tongTien }
        val paidCount = userBills.count { it.trangThai == "Đã thanh toán" }

        // Lọc danh sách chưa thanh toán
        val unpaidBills = userBills.filter { it.trangThai == "Chưa thanh toán" }
        val unpaidCount = unpaidBills.size

        // Tạo chuỗi hiển thị danh sách các tháng còn nợ
        val unpaidDetails = if (unpaidBills.isEmpty()) {
            "✅ Không có (Đã đóng đủ)"
        } else {
            // Map từng hóa đơn thành chuỗi: "• Điện T12/2025"
            unpaidBills.joinToString("\n") { bill ->
                val icon = if (bill.loai == "Điện") "⚡" else "💧"
                "$icon ${bill.loai} T${bill.thang}/${bill.nam}"
            }
        }

        // Format tiền
        val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        // Nội dung Dialog
        val message = """
            Email: ${user.email}
            SĐT: ${user.phoneNumber}
            --------------------------------
            TỔNG QUAN:
            - Tổng số hóa đơn: $totalBills
            - Tổng tiền đã chi: ${formatVND.format(totalMoney)}
            
            Đã thanh toán: $paidCount
            Chưa thanh toán: $unpaidCount
            
            CHI TIẾT CÁC KHOẢN NỢ:
            $unpaidDetails
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Hồ sơ: ${user.fullName}")
            .setMessage(message)
            .setPositiveButton("Đóng", null)
            .show()
    }
}