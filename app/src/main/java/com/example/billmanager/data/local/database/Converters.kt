package com.example.billmanager.data.local.database

import androidx.room.TypeConverter
import com.example.billmanager.data.model.NotificationType

class Converters {
    @TypeConverter
    fun fromType(value: NotificationType): String = value.name

    @TypeConverter
    fun toType(value: String): NotificationType = NotificationType.valueOf(value)
}