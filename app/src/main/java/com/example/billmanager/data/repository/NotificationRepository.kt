package com.example.billmanager.data.repository

import com.example.billmanager.data.local.dao.NotificationDao
import com.example.billmanager.data.local.entity.NotificationEntity

class NotificationRepository(private val notificationDao: NotificationDao) {

    fun getAllNotifications(email: String): List<NotificationEntity> {
        return notificationDao.getNotificationsByUser(email)
    }

    suspend fun insert(notification: NotificationEntity) {
        notificationDao.insert(notification)
    }

    suspend fun update(notification: NotificationEntity) {
        notificationDao.update(notification)
    }

    suspend fun delete(notification: NotificationEntity) {
        notificationDao.delete(notification)
    }

    suspend fun markAllRead(email: String) {
        notificationDao.markAllAsRead(email)
    }

    suspend fun clearAll(email: String) {
        notificationDao.clearAll(email)
    }
}