package com.example.billmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val type: String,
    val isSelected: Boolean = false // Đánh dấu đây là địa điểm đang chọn để xem
) : Serializable