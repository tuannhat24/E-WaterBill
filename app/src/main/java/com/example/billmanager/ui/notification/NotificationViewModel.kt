package com.example.billmanager.ui.notification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.NotificationEntity
import com.example.billmanager.data.repository.NotificationRepository
import com.example.billmanager.utils.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).notificationDao()
    private val repository = NotificationRepository(dao)

    private val userSession = UserSession(application)

    private val _notifications = MutableLiveData<List<NotificationEntity>>()
    val notifications: LiveData<List<NotificationEntity>> = _notifications

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            val email = userSession.getUserEmail() ?: ""
            // Gọi qua Repository
            val list = repository.getAllNotifications(email)
            _notifications.postValue(list)
        }
    }

    fun insert(notification: NotificationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(notification)
            loadNotifications()
        }
    }

    fun update(notification: NotificationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(notification)
            loadNotifications()
        }
    }

    // Hàm đánh dấu đã đọc
    fun markAsRead(notification: NotificationEntity) {
        update(notification.copy(isRead = true))
    }

    fun delete(notification: NotificationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(notification)
            loadNotifications()
        }
    }

    fun markAllRead() {
        viewModelScope.launch(Dispatchers.IO) {
            val email = userSession.getUserEmail() ?: ""
            repository.markAllRead(email)
            loadNotifications()
        }
    }
}