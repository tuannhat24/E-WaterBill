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
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter(
    private var notifications: List<NotificationEntity>,
    private val onItemClick: (NotificationEntity) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvNotificationTime)
        val layoutNotification: LinearLayout = itemView.findViewById(R.id.layoutNotification)
        val iconBackground: CardView = itemView.findViewById(R.id.iconBackground)
        val iconNotification: ImageView = itemView.findViewById(R.id.iconNotification)
        val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)
        val tvTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        val tvMessage: TextView = itemView.findViewById(R.id.tvNotificationMessage)
    }

    fun setNotifications(newList: List<NotificationEntity>) {
        notifications = newList
        notifyDataSetChanged()
    }

    fun getNotificationAt(position: Int): NotificationEntity {
        return notifications[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.tvTitle.text = notification.title
        holder.tvMessage.text = notification.message

        // Chuyển String date ("dd/MM/yyyy HH:mm") về mili giây
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val timeMillis = try {
            sdf.parse(notification.date)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }

        holder.tvTime.text = DateUtils.getRelativeTimeSpanString(
            timeMillis, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
        )

        holder.unreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE

        when (notification.type) {
            "ALERT", "WARNING" -> {
                holder.iconBackground.setCardBackgroundColor(
                    holder.itemView.context.getColor(
                        android.R.color.holo_orange_light
                    )
                )
                holder.iconNotification.setImageResource(android.R.drawable.ic_dialog_alert)
            }

            "Nước", "WATER" -> {
                holder.iconBackground.setCardBackgroundColor(
                    holder.itemView.context.getColor(
                        android.R.color.holo_blue_light
                    )
                )
                holder.iconNotification.setImageResource(android.R.drawable.ic_menu_myplaces)
            }

            "Điện", "ELECTRIC" -> {
                holder.iconBackground.setCardBackgroundColor(
                    holder.itemView.context.getColor(
                        android.R.color.holo_green_light
                    )
                )
                holder.iconNotification.setImageResource(android.R.drawable.ic_menu_compass)
            }

            else -> { // "INFO", "SUCCESS"
                holder.iconBackground.setCardBackgroundColor(
                    holder.itemView.context.getColor(
                        android.R.color.holo_orange_light
                    )
                )
                holder.iconNotification.setImageResource(android.R.drawable.checkbox_on_background)
            }
        }

        holder.layoutNotification.setOnClickListener { onItemClick(notification) }
    }

    override fun getItemCount() = notifications.size
}