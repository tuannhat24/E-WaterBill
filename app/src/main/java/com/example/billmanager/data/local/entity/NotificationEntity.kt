package com.example.billmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.billmanager.data.model.NotificationType

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val date: String,
    val type: String,
    val isRead: Boolean = false,
    val userEmail: String = ""
)
