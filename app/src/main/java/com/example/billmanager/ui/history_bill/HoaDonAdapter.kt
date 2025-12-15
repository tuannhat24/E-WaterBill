package com.example.billmanager.ui.history_bill

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.example.billmanager.data.local.entity.HoaDonEntity


class HoaDonAdapter(
    private val ds: MutableList<HoaDonEntity>,
    private val onClick: (HoaDonEntity) -> Unit
) : RecyclerView.Adapter<HoaDonAdapter.HoaDonViewHolder>() {

    inner class HoaDonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgLoai: ImageView = view.findViewById(R.id.imgLoai)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvTimeAmount: TextView = view.findViewById(R.id.tvTimeAndAmount)
        val tvTrangThai: TextView = view.findViewById(R.id.tvTrangThai)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoaDonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_bill, parent, false)
        return HoaDonViewHolder(view)
    }

    override fun onBindViewHolder(holder: HoaDonViewHolder, position: Int) {
        val hd = ds[position]

        holder.imgLoai.setImageResource(
            if (hd.loai == "Điện") R.drawable.ic_dien else R.drawable.ic_nuoc
        )

        holder.tvTitle.text =
            "Hóa đơn tiền ${hd.loai} T${hd.thang}/${hd.nam}"

        holder.tvTimeAmount.text =
            "${hd.gio} ${hd.ngay}\n${formatTien(hd.tongTien)}"

        holder.tvTrangThai.text = hd.trangThai

        holder.itemView.setOnClickListener {
            onClick(hd)
        }
    }

    override fun getItemCount(): Int = ds.size

    // ✅ CHUẨN
    fun updateList(newList: List<HoaDonEntity>) {
        ds.clear()
        ds.addAll(newList)
        notifyDataSetChanged()
    }

    private fun formatTien(v: Long): String {
        return java.text.NumberFormat
            .getInstance(java.util.Locale("vi", "VN"))
            .format(v) + "đ"
    }
}

