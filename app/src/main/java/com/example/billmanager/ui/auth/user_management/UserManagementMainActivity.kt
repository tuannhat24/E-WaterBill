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

class UserManagementMainActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var rvUsers: RecyclerView

    private lateinit var db: AppDatabase
    private lateinit var adapter: UserAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management_main)
        db = AppDatabase.getInstance(this)
        setControl()
        setEvent()
    }

    private fun setControl() {
        rvUsers = findViewById<RecyclerView>(R.id.rvUsers)
        tvTitle = findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "Quản lý người dùng(Nhóm 10-Quang Dinh)"
        btnBack = findViewById<ImageButton>(R.id.btnBack)
    }

    private fun setEvent() {
        btnBack.setOnClickListener {
            finish()
        }
        adapter = UserAdapter(
            onLock = {toggleLock(it)}
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

        loadUsers() // ⬅️ QUAN TRỌNG
    }


}