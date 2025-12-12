package com.example.billmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: Int, // 1: Điện, 2: Nước
    val amountLimit: Double,
    val month: Int,
    val year: Int,
    val alertThreshold: Int = 80 // Mặc định cảnh báo ở mức 80%
)