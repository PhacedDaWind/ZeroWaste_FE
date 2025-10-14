package com.example.zerowaste.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.remote.NotificationResponse
import com.example.zerowaste.data.repository.NotificationRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * The ViewModel holds the UI state and business logic. It survives configuration
 * changes (like screen rotation) and provides data to the UI via LiveData.
 */
class NotificationViewModel : ViewModel() {

    private val repository = NotificationRepository()

    // LiveData holding the list of notifications for the UI to observe.
    private val _notifications = MutableLiveData<List<NotificationResponse>>()
    val notifications: LiveData<List<NotificationResponse>> = _notifications

    // LiveData for showing/hiding a loading spinner.
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for displaying error messages.
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Fetches the list of notifications from the repository.
     */
    fun fetchNotifications(userId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.getNotifications(userId)
                if (response.isSuccessful) {
                    // Extract the list from the 'data' field of the ResponseDTO
                    _notifications.postValue(response.body()?.data)
                } else {
                    _error.postValue("API Error: ${response.code()} ${response.message()}")
                }
            } catch (e: HttpException) {
                _error.postValue("Server Error: ${e.message()}")
            } catch (e: IOException) {
                _error.postValue("Network Error: Please check your connection.")
            } catch (e: Exception) {
                _error.postValue("An unexpected error occurred: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Marks a specific notification as read and then refreshes the list.
     */
    fun markNotificationAsRead(notificationId: Long, currentUserId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.markAsRead(notificationId)
                if (response.isSuccessful) {
                    // Refresh the list to show the visual change (e.g., un-bolding text)
                    fetchNotifications(currentUserId)
                } else {
                    _error.postValue("Failed to mark as read.")
                }
            } catch (e: Exception) {
                _error.postValue("Error marking notification as read: ${e.message}")
            }
        }
    }
}