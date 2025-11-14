package com.example.zerowaste.ui.notification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.local.SessionManager
import com.example.zerowaste.data.remote.NotificationResponse
import com.example.zerowaste.data.remote.NotificationType
import com.example.zerowaste.data.remote.NotificationApiService
import com.example.zerowaste.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

// This data class holds the entire state for the notification screen
data class NotificationUiState(
    val notifications: List<NotificationResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val filterType: NotificationType? = null // null means "All"
)

// Inherit from AndroidViewModel to get the Application context
class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    // Get the API service by passing the context to your RetrofitClient
    private val apiService: NotificationApiService = RetrofitClient.getNotificationApi(application)

    // ⭐ GET USER ID DYNAMICALLY FROM SESSION MANAGER
    private val sessionManager = SessionManager(application)
    private val currentUserId: Long = sessionManager.getUserId() ?: 1L // Use 1L as a safe fallback

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        // Load notifications when the ViewModel is first created
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val currentFilter = _uiState.value.filterType

                // ⭐ USE DYNAMIC USER ID FOR API CALL
                val response = apiService.getNotificationList(currentUserId, currentFilter)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        // Success: Update the UI with the data
                        _uiState.update {
                            it.copy(isLoading = false, notifications = body.data)
                        }
                    } else {
                        throw IOException("Response body is null")
                    }
                } else {
                    // Handle API error (e.g., 404, 500)
                    throw IOException("Network call failed: ${response.code()}")
                }
            } catch (e: Exception) {
                // Handle network or parsing error
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to load notifications: ${e.message}")
                }
            }
        }
    }

    fun filterNotifications(type: NotificationType?) {
        _uiState.update { it.copy(filterType = type) }
        // Reload notifications with the new filter
        loadNotifications()
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.markAsRead(notificationId)
                if (response.isSuccessful && response.body() != null) {
                    // Optimistically update the local list
                    _uiState.update { currentState ->
                        val updatedList = currentState.notifications.map {
                            if (it.id == notificationId) it.copy(markAsRead = true) else it
                        }
                        currentState.copy(notifications = updatedList)
                    }
                } else {
                    // Handle failure to mark as read
                    throw IOException("Failed to mark as read: ${response.code()}")
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteNotification(notificationId)
                if (response.isSuccessful) {
                    // Optimistically remove from the local list
                    _uiState.update { currentState ->
                        val updatedList = currentState.notifications.filterNot { it.id == notificationId }
                        currentState.copy(notifications = updatedList)
                    }
                } else {
                    throw IOException("Failed to delete: ${response.code()}")
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            try {
                val response = apiService.deleteAllNotifications(currentUserId)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(notifications = emptyList()) }
                } else {
                    throw IOException("Failed to delete all: ${response.code()}")
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}