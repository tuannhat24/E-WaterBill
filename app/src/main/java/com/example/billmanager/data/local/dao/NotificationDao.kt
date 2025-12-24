package com.example.billmanager.data.local.dao

import androidx.room.*
import com.example.billmanager.data.local.entity.NotificationEntity

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userEmail = :email ORDER BY id DESC")
    fun getNotificationsByUser(email: String): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(notification: NotificationEntity)

    @Update
    fun update(notification: NotificationEntity)

    @Delete
    fun delete(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userEmail = :email")
    fun markAllAsRead(email: String)

    // Xóa tất cả (Theo User)
    @Query("DELETE FROM notifications WHERE userEmail = :email")
    fun clearAll(email: String)
}