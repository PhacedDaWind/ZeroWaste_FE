package com.example.zerowaste.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.remote.NotificationResponse
import com.example.zerowaste.data.remote.RetrofitClient // Assuming RetrofitClient is in this package
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// The UI state now uses the new NotificationResponse class
data class NotificationUiState(
    val notifications: List<NotificationResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class NotificationViewModel : ViewModel() {

    // Assuming RetrofitClient provides an instance of the new NotificationApiService
    private val apiService = RetrofitClient.notifApiService

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState = _uiState.asStateFlow()

    fun fetchNotifications(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Call the new service method
                val response = apiService.getNotificationList(userId)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            // The response body is now ResponseDTO, so we access its `data` property
                            notifications = response.body()?.data ?: emptyList()
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load notifications") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "An error occurred: ${e.message}") }
            }
        }
    }

    fun markNotificationAsRead(notification: NotificationResponse) {
        if (notification.markAsRead) return
        viewModelScope.launch {
            try {
                val response = apiService.markAsRead(notification.id)
                if (response.isSuccessful) {
                    _uiState.update { currentState ->
                        val updatedList = currentState.notifications.map {
                            // Use the returned object from the API to update the item
                            if (it.id == response.body()?.data?.id) response.body()!!.data else it
                        }
                        currentState.copy(notifications = updatedList)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to mark as read") }
            }
        }
    }

    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteNotification(notificationId)
                if (response.isSuccessful) {
                    _uiState.update { currentState ->
                        val updatedList = currentState.notifications.filterNot { it.id == notificationId }
                        currentState.copy(notifications = updatedList)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete notification") }
            }
        }
    }

    fun deleteAllNotifications(userId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteAllNotifications(userId)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(notifications = emptyList()) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete all notifications") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}