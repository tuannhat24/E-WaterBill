package com.example.billmanager.data.repository

import androidx.lifecycle.LiveData
import com.example.billmanager.data.local.dao.NotificationDao
import com.example.billmanager.data.local.entity.NotificationEntity

class NotificationRepository(private val notificationDao: NotificationDao) {

    val allNotifications: LiveData<List<NotificationEntity>> = notificationDao.getAllNotifications()

    suspend fun insert(notification: NotificationEntity) {
        notificationDao.insertNotification(notification)
    }

    suspend fun update(notification: NotificationEntity) {
        notificationDao.updateNotification(notification)
    }

    suspend fun delete(notification: NotificationEntity) {
        notificationDao.deleteNotification(notification)
    }

    suspend fun markAllRead() {
        notificationDao.markAllAsRead()
    }
}