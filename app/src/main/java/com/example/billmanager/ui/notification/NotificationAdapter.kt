package com.example.billmanager.ui.notification

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.billmanager.R
import com.example.billmanager.data.local.entity.NotificationEntity
import com.example.billmanager.data.model.NotificationType

class NotificationAdapter(
    private var notifications: List<NotificationEntity>,
    private val onItemClick: (NotificationEntity) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ... (Khai báo view giống cũ)
        val tvTime: TextView = itemView.findViewById(R.id.tvNotificationTime)
        // ...
        val layoutNotification: LinearLayout = itemView.findViewById(R.id.layoutNotification)
        val iconBackground: CardView = itemView.findViewById(R.id.iconBackground)
        val iconNotification: ImageView = itemView.findViewById(R.id.iconNotification)
        val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)
        val tvTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        val tvMessage: TextView = itemView.findViewById(R.id.tvNotificationMessage)
    }

    // Hàm update list mới từ LiveData
    fun setNotifications(newList: List<NotificationEntity>) {
        notifications = newList
        notifyDataSetChanged()
    }

    // Hàm lấy item tại vị trí (cho Swipe to delete)
    fun getNotificationAt(position: Int): NotificationEntity {
        return notifications[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.tvTitle.text = notification.title
        holder.tvMessage.text = notification.message

        // Time tự động tăng
        holder.tvTime.text = DateUtils.getRelativeTimeSpanString(
            notification.timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
        )

        holder.unreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE

        when (notification.type) {
            NotificationType.WARNING -> {
                holder.iconBackground.setCardBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_orange_light))
                holder.iconNotification.setImageResource(android.R.drawable.ic_dialog_alert)
            }
            NotificationType.WATER -> {
                holder.iconBackground.setCardBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_blue_light))
                holder.iconNotification.setImageResource(android.R.drawable.ic_menu_myplaces) // Ví dụ icon
            }
            NotificationType.ELECTRIC -> {
                holder.iconBackground.setCardBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_green_light))
                holder.iconNotification.setImageResource(android.R.drawable.ic_menu_compass) // Ví dụ icon
            }
            NotificationType.SUCCESS -> {
                holder.iconBackground.setCardBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_orange_light))
                holder.iconNotification.setImageResource(android.R.drawable.checkbox_on_background)
            }
        }

        holder.layoutNotification.setOnClickListener { onItemClick(notification) }
    }

    override fun getItemCount() = notifications.size
}