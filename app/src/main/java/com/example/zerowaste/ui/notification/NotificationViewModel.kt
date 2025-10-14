package com.example.zerowaste.ui.notification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.local.SessionManager
import com.example.zerowaste.data.remote.NotificationResponse
import com.example.zerowaste.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NotificationFilter { ALL, UNREAD }

data class NotificationUiState(
    val notifications: List<NotificationResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filter: NotificationFilter = NotificationFilter.ALL
)

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val apiService = RetrofitClient.getNotificationApi(application)

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState = _uiState.asStateFlow()

    // --- UPDATED: This function no longer needs a userId parameter ---
    fun fetchNotifications(unreadOnly: Boolean = false) {
        viewModelScope.launch {
            // 1. Get the userId safely inside the ViewModel
            val userId = sessionManager.getUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not logged in.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = if (unreadOnly) {
                    apiService.getUnreadNotificationList(userId)
                } else {
                    apiService.getNotificationList(userId)
                }

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = response.body()?.data ?: emptyList(),
                            filter = if (unreadOnly) NotificationFilter.UNREAD else NotificationFilter.ALL
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to load notifications.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Error: ${e.message}")
                }
            }
        }
    }

    fun markNotificationAsRead(notification: NotificationResponse) {
        if (notification.markAsRead) return
        viewModelScope.launch {
            try {
                val response = apiService.markAsRead(notification.id)
                if (response.isSuccessful) {
                    val updated = response.body()?.data
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map {
                                if (it.id == updated?.id) updated else it
                            }
                        )
                    }
                } else {
                    _uiState.update { it.copy(error = "Failed to mark as read.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error marking as read: ${e.message}") }
            }
        }
    }

    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteNotification(notificationId)
                if (response.isSuccessful) {
                    _uiState.update { state ->
                        state.copy(notifications = state.notifications.filterNot { it.id == notificationId })
                    }
                } else {
                    _uiState.update { it.copy(error = "Failed to delete notification.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error deleting: ${e.message}") }
            }
        }
    }

    // --- UPDATED: This function also gets the userId itself ---
    fun deleteAllNotifications() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId == null) {
                _uiState.update { it.copy(error = "User not logged in.") }
                return@launch
            }

            try {
                val response = apiService.deleteAllNotifications(userId)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(notifications = emptyList()) }
                } else {
                    _uiState.update { it.copy(error = "Failed to delete all notifications.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error deleting all: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
