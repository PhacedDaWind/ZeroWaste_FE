package com.example.zerowaste.ui.notification

import android.app.Application
import android.util.Log // Added for logging
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

data class NotificationUiState(
    val notifications: List<NotificationResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val filterType: NotificationType? = null // null means "All"
)

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService: NotificationApiService = RetrofitClient.getNotificationApi(application)
    private val sessionManager = SessionManager(application)

    // ⭐ REMOVED: private val currentUserId: Long = sessionManager.getUserId() ?: 1L
    // We will get the ID fresh inside the functions instead.

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    // Stores the full, unfiltered list received from the API
    private var allNotifications: List<NotificationResponse> = emptyList()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // ⭐ FIX: Get the most current User ID from SessionManager every time.
            val currentUserId = sessionManager.getUserId() ?: 1L

            Log.d("NotificationVM_DEBUG", "1. Starting load for User ID: $currentUserId") // This should now show 8
            try {
                // NOTE: The server MUST filter by currentUserId.
                val response = apiService.getNotificationList(currentUserId, null)

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("NotificationVM_DEBUG", "2. API Success. Response Code: ${response.code()}")

                    if (body != null) {
                        // CRITICAL CHECK: Did we get a list of data?
                        val fetchedListSize = body.data?.size ?: 0
                        Log.d("NotificationVM_DEBUG", "3. Data Fetched Size: $fetchedListSize")

                        if (fetchedListSize > 0 && body.data != null) {
                            allNotifications = body.data!!
                        } else {
                            allNotifications = emptyList()
                            Log.w("NotificationVM_DEBUG", "3a. Fetched list is empty, check server response structure.")
                        }

                        // Apply the current filter to the fetched data
                        filterNotifications(_uiState.value.filterType)

                    } else {
                        // This indicates a GSON/Parsing failure or an empty body
                        throw IOException("Response body is null. Check JSON parsing / data model.")
                    }
                } else {
                    // Log the error code if the API call failed (e.g., 404, 500)
                    throw IOException("Network call failed: HTTP ${response.code()} / ${response.message()}")
                }
            } catch (e: Exception) {
                // Log the exception details
                Log.e("NotificationVM_DEBUG", "4. Failed to load notifications: ${e.message}", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to load notifications: ${e.message}")
                }
            }
        }
    }

    // Filtering is done LOCALLY on the allNotifications list
    fun filterNotifications(type: NotificationType?) {
        // ... (This function is unchanged, it just filters the local list)
        _uiState.update { it.copy(isLoading = false, filterType = type) }

        val filteredList = when (type) {
            null -> allNotifications // "All" tab
            NotificationType.FOOD_INVENTORY_ALERT -> allNotifications.filter { it.notifType == type }
            NotificationType.MEAL_REMINDER -> allNotifications.filter { it.notifType == type }

            // FIX for Donations Tab: Filter by BOTH DONATION_CLAIMED and DONATION_POSTED.
            NotificationType.DONATION_CLAIMED -> allNotifications.filter {
                it.notifType == NotificationType.DONATION_CLAIMED || it.notifType == NotificationType.DONATION_POSTED
            }
            else -> emptyList()
        }

        Log.d("NotificationVM_DEBUG", "5. Filtered List Size for UI: ${filteredList.size} (Filter: $type)")
        _uiState.update { it.copy(notifications = filteredList) }
    }

    fun markAsRead(notificationId: Long) {
        // ... (This function is unchanged)
        viewModelScope.launch {
            try {
                val response = apiService.markAsRead(notificationId)
                if (response.isSuccessful && response.body() != null) {
                    // Update raw list
                    allNotifications = allNotifications.map {
                        if (it.id == notificationId) it.copy(markAsRead = true) else it
                    }
                    // Optimistically update the filtered list
                    _uiState.update { currentState ->
                        val updatedList = currentState.notifications.map {
                            if (it.id == notificationId) it.copy(markAsRead = true) else it
                        }
                        currentState.copy(notifications = updatedList)
                    }
                } else {
                    // Handle failure
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun deleteNotification(notificationId: Long) {
        // ... (This function is unchanged)
        viewModelScope.launch {
            try {
                val response = apiService.deleteNotification(notificationId)
                if (response.isSuccessful) {
                    // Update allNotifications raw list
                    allNotifications = allNotifications.filterNot { it.id == notificationId }
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
            // ⭐ FIX: Get the most current User ID from SessionManager.
            val currentUserId = sessionManager.getUserId() ?: 1L

            try {
                val response = apiService.deleteAllNotifications(currentUserId)
                if (response.isSuccessful) {
                    allNotifications = emptyList() // Clear raw list
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