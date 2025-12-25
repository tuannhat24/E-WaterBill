package com.example.billmanager.ui.budget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import java.text.NumberFormat
import java.util.Locale

data class HistoryItem(val time: String, val dien: Long, val nuoc: Long)

class BudgetHistoryAdapter(private val list: List<HistoryItem>) :
    RecyclerView.Adapter<BudgetHistoryAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvTime: TextView = v.findViewById(R.id.tvTime)
        val tvElectric: TextView = v.findViewById(R.id.tvElectric)
        val tvWater: TextView = v.findViewById(R.id.tvWater)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget_history, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        val formatVND = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        holder.tvTime.text = item.time
        holder.tvElectric.text = formatVND.format(item.dien)
        holder.tvWater.text = formatVND.format(item.nuoc)
    }

    override fun getItemCount() = list.size
}