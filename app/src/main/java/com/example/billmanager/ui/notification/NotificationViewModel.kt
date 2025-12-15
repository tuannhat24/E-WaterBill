package com.example.billmanager.ui.notification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.NotificationEntity
import com.example.billmanager.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotificationRepository
    val allNotifications: LiveData<List<NotificationEntity>>

    init {
        val dao = AppDatabase.getDatabase(application).notificationDao()
        repository = NotificationRepository(dao)
        allNotifications = repository.allNotifications
    }

    fun insert(notification: NotificationEntity) = viewModelScope.launch {
        repository.insert(notification)
    }

    fun update(notification: NotificationEntity) = viewModelScope.launch {
        repository.update(notification)
    }

    fun delete(notification: NotificationEntity) = viewModelScope.launch {
        repository.delete(notification)
    }

    fun markAllRead() = viewModelScope.launch {
        repository.markAllRead()
    }
}