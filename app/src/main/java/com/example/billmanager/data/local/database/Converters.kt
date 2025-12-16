package com.example.billmanager.data.local.database

import androidx.room.TypeConverter
import com.example.billmanager.data.model.NotificationType
import java.util.Date

class Converters {
    // Xử lý Enum NotificationType
    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = NotificationType.valueOf(value)

    // Xử lý Date
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}