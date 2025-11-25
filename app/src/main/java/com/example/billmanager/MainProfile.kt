package com.example.billmanager

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainProfile : AppCompatActivity() {
    lateinit var txtProfileName: TextView
    lateinit var edtFullName: EditText
    lateinit var edtEmail: EditText
    lateinit var btnLogout: LinearLayout
    lateinit var btnDeleteAccount: LinearLayout
    lateinit var btnEdit: LinearLayout
    lateinit var txtEdit: TextView
    var isEditing = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_profile)
        setControl()
        setEvent()
    }

    private fun setControl(){
        txtProfileName = findViewById<TextView>(R.id.txtProfileName)
        edtFullName = findViewById<EditText>(R.id.edtFullName)
        edtEmail = findViewById<EditText>(R.id.edtEmail)
        btnLogout = findViewById<LinearLayout>(R.id.btnLogout)
        btnDeleteAccount = findViewById<LinearLayout>(R.id.btnDeleteAccount)
        btnEdit = findViewById<LinearLayout>(R.id.btnEdit)
        txtEdit = findViewById<TextView>(R.id.txtEdit)
    }

    private fun setEvent() {
        val currentUser = UserManager.getCurrentUser(this)
        if (currentUser != null) {
            txtProfileName.text = currentUser.fullName
            edtFullName.setText(currentUser.fullName)
            edtEmail.setText(currentUser.email)
        } else {
            txtProfileName.text = "Chưa có thông tin"
            edtFullName.setText("Chưa có thông tin")
            edtEmail.setText("Chưa có thông tin")
        }


        btnEdit.setOnClickListener {
            if (!isEditing) {
                enableEditing()
            } else {
                if (currentUser != null) {
                    val newName = edtFullName.text.toString().trim()

                    // tạo object user mới giữ nguyên email, password
                    val updatedUser = User(
                        fullName = newName,
                        email = currentUser.email,
                        password = currentUser.password
                    )

                    // Lưu lại user mới
                    UserManager.saveCurrentUser(this, updatedUser)

                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                }
                disableEditing()
            }
        }

        //logout
        btnLogout.setOnClickListener {
            startActivity(Intent(this, MainLogin::class.java))
            Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show()
            finish()
        }

        //remove account
        btnDeleteAccount.setOnClickListener {
            UserManager.removeUser(this)
            startActivity(Intent(this, MainLogin::class.java))
            Toast.makeText(this, "Tài khoản đã được xóa!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    private fun enableEditing() {
        isEditing = true
        txtEdit.text = "Lưu"

        edtFullName.isEnabled = true
    }

    private fun disableEditing() {
        isEditing = false
        txtEdit.text = "Chỉnh sửa"

        edtFullName.isEnabled = false
    }
}