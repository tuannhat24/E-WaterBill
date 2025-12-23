package com.example.billmanager.ui.auth.user_management

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.example.billmanager.data.local.entity.User

class UserAdapter(
    val onLock: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var users = listOf<User>()

    fun submitList(data: List<User>) {
        users = data
        notifyDataSetChanged()
    }

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txtFullName)
        val txtEmail: TextView = view.findViewById(R.id.txtEmail)
        val txtRole: TextView = view.findViewById(R.id.txtRole)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val btnLock: Button = view.findViewById(R.id.btnLock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        holder.txtName.text = user.fullName
        holder.txtEmail.text = user.email
        holder.txtRole.text = "Role: ${user.role}"
        holder.txtStatus.text =
            if (user.isActive) "ACTIVE" else "LOCKED"

        holder.btnLock.text =
            if (user.isActive) "Khóa tài khoản" else "Mở khóa tài khoản"

        if (user.role == "Admin") {
            holder.btnLock.visibility = View.GONE
        } else {
            holder.btnLock.visibility = View.VISIBLE
        }

        holder.btnLock.setOnClickListener { onLock(user) }
    }
}
