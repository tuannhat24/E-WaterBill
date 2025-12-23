package com.example.billmanager.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.billmanager.R
import com.example.billmanager.MainActivity
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.ui.auth.profile.ProfileActivity
import com.example.billmanager.ui.auth.register.RegisterActivity
import com.example.billmanager.utils.UserSession
import com.example.billmanager.utils.setupShowHidePassword

class LoginActivity : AppCompatActivity() {
    lateinit var edtEmail: EditText
    lateinit var edtPassword: EditText
    lateinit var btnLogin: Button
    lateinit var txtSignup: TextView
    lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setControl()
        setEvent()
        setupShowHidePassword(edtPassword)
    }

    private fun setControl() {
        edtEmail = findViewById<EditText>(R.id.edtEmail)
        edtPassword = findViewById<EditText>(R.id.edtPassword)
        txtSignup = findViewById<TextView>(R.id.txtSignup)
        btnLogin = findViewById<Button>(R.id.btnLogin)
    }

    private fun setEvent() {
        db = AppDatabase.getInstance(this)

        //xử lý đăng nhập
        btnLogin.setOnClickListener {
            //lấy dữ liệu người dùng nhập vào
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            //kiểm tra rỗng
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //kiểm tra tài khoản
            try {
                val userCurrent = db.userDao().login(email, password)

                // KIỂM TRA ACTIVE (Admin yêu cầu: Nếu bị khóa thì không cho vào)
                if (userCurrent != null) {
                    if (!userCurrent.isActive) {
                        Toast.makeText(this, "Tài khoản đã bị khóa. Vui lòng liên hệ admin để biết thêm chi tiết!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    // Lưu session
                    val session = UserSession(this)

                    session.saveUser(userCurrent.email, userCurrent.role)

                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.d("Error", e.message.toString())
            }
        }

        //chuyển sang màn hình đăng ky
        txtSignup.setOnClickListener() {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}