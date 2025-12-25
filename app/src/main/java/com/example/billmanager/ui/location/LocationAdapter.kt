package com.example.billmanager.ui.location

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.example.billmanager.data.local.entity.LocationEntity

class LocationAdapter(
    private var list: List<LocationEntity>,
    private val onAction: (LocationEntity, String) -> Unit
) : RecyclerView.Adapter<LocationAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(android.R.id.text1)
        val tvAddress: TextView = v.findViewById(android.R.id.text2)
        val btnSwitch: Button = v.findViewById(R.id.btnSwitch)
        val btnEdit: ImageButton = v.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = v.findViewById(R.id.btnDelete)
        val root: View = v.findViewById(R.id.rootLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.tvName.text = item.name
        holder.tvAddress.text = "${item.address} - ${item.type}"

        if (item.isSelected) {
            holder.root.setBackgroundColor(Color.parseColor("#E0F2F1"))
            holder.btnSwitch.text = "Đang chọn"
            holder.btnSwitch.isEnabled = false
        } else {
            holder.root.setBackgroundColor(Color.WHITE)
            holder.btnSwitch.text = "Chọn"
            holder.btnSwitch.isEnabled = true
        }

        holder.btnSwitch.setOnClickListener { onAction(item, "SELECT") }
        holder.btnEdit.setOnClickListener { onAction(item, "EDIT") }
        holder.btnDelete.setOnClickListener { onAction(item, "DELETE") }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<LocationEntity>) {
        list = newList
        notifyDataSetChanged()
    }
}