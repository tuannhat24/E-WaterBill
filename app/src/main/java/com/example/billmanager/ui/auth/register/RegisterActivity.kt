package com.example.billmanager.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.billmanager.R
import com.example.billmanager.data.local.database.UsersDB
import com.example.billmanager.data.local.entity.User
import com.example.billmanager.ui.auth.login.LoginActivity
import com.example.billmanager.utils.setupShowHidePassword

class RegisterActivity : AppCompatActivity() {
    lateinit var edtFullName: EditText
    lateinit var edtEmail: EditText
    lateinit var edtPassword: EditText
    lateinit var edtConfirmPassword: EditText
    lateinit var btnRegister: Button
    lateinit var txtSinIn: TextView
    lateinit var db: UsersDB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setControl()
        setEvent()
        setupShowHidePassword(edtPassword)
        setupShowHidePassword(edtConfirmPassword)
    }

    private fun setControl() {
        edtFullName = findViewById<EditText>(R.id.edtFullName)
        edtEmail = findViewById<EditText>(R.id.edtEmail)
        edtPassword = findViewById<EditText>(R.id.edtPassword)
        edtConfirmPassword = findViewById<EditText>(R.id.edtConfirmPassword)
        btnRegister = findViewById<Button>(R.id.btnRegister)
        txtSinIn = findViewById<TextView>(R.id.txtSignIn)
    }

    private fun setEvent() {
        db = UsersDB.getInstance(this)

        btnRegister.setOnClickListener {
            val fullName = edtFullName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            val confirmPassword = edtConfirmPassword.text.toString().trim()

            //kiểm tra rỗng
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //kiểm tra email này đã tồn tại hay chưa
            val getUsers = db.userDao().getAll()
            val user = getUsers.find { it.email == email }
            if (user != null) {
                Toast.makeText(
                    this,
                    "Email này đã được đăng ký.Vui lòng dùng email khác",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // REGEX kiểm tra mật khẩu mạnh
            val passwordRegex =
                Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\$%^&+=!?.*()_-]).{6,}$")
            if (!passwordRegex.matches(password)) {
                Toast.makeText(
                    this,
                    " Mật khẩu phải có ít nhất 6 ký tự, 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            //kiểm tra mật khẩu nhập lại có đúng hay không
            if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //thêm newUser
            val newUser =
                User(fullName = fullName, email = email, phoneNumber = "", password = password)
            try {
                db.userDao().insert(newUser)
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } catch (e: Exception) {
                println(e.message)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        //chuyển sang màn hình đăn nhập
        txtSinIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
