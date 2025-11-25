package com.example.billmanager

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainLogin : AppCompatActivity() {
    lateinit var edtEmail: EditText
    lateinit var edtPassword: EditText
    lateinit var btnLogin: Button
    lateinit var txtSignup: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_login)
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
        //chuyển sang màn hình đăng ky
        txtSignup.setOnClickListener() {
            val intent = Intent(this, MainRegister::class.java)
            startActivity(intent)
        }

        //xử lý đăng nhập
        btnLogin.setOnClickListener {
            println("aaaaa")
            //lấy dữ liệu người dùng nhập vào
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            val users = UserManager.getUsers(this)

            //kiểm tra rỗng
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val user = users.find { it.email == email && it.password == password }
            //kiểm tra tài khoản
            println(user)
            if (user != null) {
                //lưu lại thông tin user
                UserManager.saveCurrentUser(this, user)
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()

                //chuyển sang trang chính
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show()
            }
        }
    }
}