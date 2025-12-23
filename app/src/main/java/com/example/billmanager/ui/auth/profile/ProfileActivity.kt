package com.example.billmanager.ui.auth.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.billmanager.R
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.User
import com.example.billmanager.ui.auth.login.LoginActivity
import com.example.billmanager.utils.UserSession

class ProfileActivity : AppCompatActivity() {
    lateinit var txtProfileName: TextView
    lateinit var txtEdit: TextView
    lateinit var edtFullName: EditText
    lateinit var edtEmail: EditText
    lateinit var edtPhoneNumber: EditText
    lateinit var btnEdit: LinearLayout
    lateinit var btnDeleteAccount: LinearLayout
    lateinit var tvTitle: TextView
    lateinit var btnBack: ImageButton
    lateinit var btnLogout: LinearLayout
    lateinit var db: AppDatabase
    var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setControl()
        setEvent()
    }

    private fun setControl() {
        txtProfileName = findViewById<TextView>(R.id.txtProfileName)
        txtEdit = findViewById<TextView>(R.id.txtEdit)
        edtFullName = findViewById<EditText>(R.id.edtFullName)
        edtEmail = findViewById<EditText>(R.id.edtEmail)
        edtPhoneNumber = findViewById<EditText>(R.id.edtPhoneNumber)
        btnEdit = findViewById<LinearLayout>(R.id.btnEdit)
        btnDeleteAccount = findViewById<LinearLayout>(R.id.btnDeleteAccount)
        btnLogout = findViewById<LinearLayout>(R.id.btnLogout)
        tvTitle = findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "Thông tin cá nhân(Nhóm 10-Quang Dinh)"
        btnBack = findViewById<ImageButton>(R.id.btnBack)
    }

    private fun setEvent() {
        db = AppDatabase.getInstance(this)
        val session = UserSession(this)
        val email = session.getUserEmail()

        // Lấy thông tin user theo email
        // Lưu ý: findByEmail có thể trả về null, nên check null an toàn
        var currentUser = db.userDao().findByEmail(email)

        if (currentUser != null) {
            refreshUserUI(currentUser)
        }

        btnBack.setOnClickListener {
            finish()
        }

        // Chỉnh sửa thông tin
        btnEdit.setOnClickListener {
            // Check lại currentUser đề phòng null
            if (currentUser == null) return@setOnClickListener

            if (!isEditing) {
                enableEditing()
            } else {
                val updateFullName = edtFullName.text.toString().trim()
                val updateEmail = edtEmail.text.toString().trim()
                val updatePhoneNumber = edtPhoneNumber.text.toString().trim()

                if (updateFullName.isEmpty() || updateEmail.isEmpty()) {
                    Toast.makeText(this, "Không được để trống", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Kiểm tra email trùng với user khác (trừ chính mình ra)
                val existing = db.userDao().findByEmail(updateEmail)
                if (existing != null && existing.id != currentUser!!.id) {
                    Toast.makeText(this, "Email đã tồn tại!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Cập nhật User
                // QUAN TRỌNG: Phải giữ nguyên role và isActive của user cũ
                val dataUserUpdate = User(
                    id = currentUser!!.id,
                    fullName = updateFullName,
                    email = updateEmail,
                    phoneNumber = updatePhoneNumber,
                    password = currentUser!!.password,
                    role = currentUser!!.role,
                    isActive = currentUser!!.isActive
                )

                db.userDao().update(dataUserUpdate)

                // Cập nhật Session với Email mới và Role cũ
                // Truyền thêm currentUser.role
                session.saveUser(updateEmail, currentUser!!.role)

                // Cập nhật biến currentUser hiện tại để dùng tiếp nếu user chưa thoát
                currentUser = dataUserUpdate

                refreshUserUI(dataUserUpdate)
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()

                disableEditing()
            }
        }

        // Xóa tài khoản
        btnDeleteAccount.setOnClickListener {
            if (currentUser == null) return@setOnClickListener

            AlertDialog.Builder(this)
                .setTitle("Xóa tài khoản")
                .setMessage("Bạn có chắc muốn xóa tài khoản?")
                .setPositiveButton("Yes") { _, _ ->
                    try {
                        session.clearSession()
                        db.userDao().delete(currentUser!!)
                        Toast.makeText(this, "Xóa tài khoản thành công!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Đăng xuất tài khoản
        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Yes") { _, _ ->
                    session.clearSession()
                    Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun enableEditing() {
        isEditing = true
        txtEdit.text = "Lưu"

        edtFullName.isEnabled = true
        edtEmail.isEnabled = true
        edtPhoneNumber.isEnabled = true
    }

    private fun disableEditing() {
        isEditing = false
        txtEdit.text = "Chỉnh sửa"

        edtFullName.isEnabled = false
        edtEmail.isEnabled = false
        edtPhoneNumber.isEnabled = false
    }

    private fun refreshUserUI(user: User) {
        txtProfileName.text = user.fullName
        edtFullName.setText(user.fullName)
        edtEmail.setText(user.email)
        edtPhoneNumber.setText(user.phoneNumber)
    }
}