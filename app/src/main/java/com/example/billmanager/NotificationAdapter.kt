package com.example.billmanager

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(
    private val notifications: MutableList<Notification>,
    private val onItemClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutNotification: LinearLayout = itemView.findViewById(R.id.layoutNotification)
        val iconBackground: CardView = itemView.findViewById(R.id.iconBackground)
        val iconNotification: ImageView = itemView.findViewById(R.id.iconNotification)
        val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)
        val tvTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        val tvMessage: TextView = itemView.findViewById(R.id.tvNotificationMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvNotificationTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        holder.tvTitle.text = notification.title
        holder.tvMessage.text = notification.message
        val timeAgo = DateUtils.getRelativeTimeSpanString(
            notification.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        holder.tvTime.text = timeAgo

        // Hiển thị chấm xanh nếu chưa đọc
        holder.unreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE

        // Đặt màu nền và icon theo loại thông báo
        when (notification.type) {
            NotificationType.WARNING -> {
                holder.iconBackground.setCardBackgroundColor(
                    holder.itemView.context.getColor(android.R.color.holo_orange_light)
                )
                holder.iconNotification.setImageResource(android.R.drawable.ic_dialog_alert)
            }
            NotificationType.WATER -> {
                holder.iconBackground.setCardBackgroundColor(
                    holder.itemView.context.getColor(android.R.color.holo_blue_light)
                )
                holder.iconNotification.setImageResource(android.R.drawable.ic_dialog_info)
            }
            NotificationType.ELECTRIC -> {
                holder.iconBackground.setCardBackgroundColor(
                    holder.itemView.context.getColor(android.R.color.holo_green_light)
                )
                holder.iconNotification.setImageResource(android.R.drawable.ic_menu_compass)
            }
            NotificationType.SUCCESS -> {
                holder.iconBackground.setCardBackgroundColor(
                    holder.itemView.context.getColor(android.R.color.holo_orange_light)
                )
                holder.iconNotification.setImageResource(android.R.drawable.checkbox_on_background)
            }
        }

        // Xử lý click
        holder.layoutNotification.setOnClickListener {
            onItemClick(notification)
        }
    }

    override fun getItemCount() = notifications.size
}