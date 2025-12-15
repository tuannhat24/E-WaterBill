package com.example.billmanager.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.billmanager.data.local.entity.NotificationEntity

@Dao
interface NotificationDao {
    // Lấy tất cả thông báo, mới nhất lên đầu
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): LiveData<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}