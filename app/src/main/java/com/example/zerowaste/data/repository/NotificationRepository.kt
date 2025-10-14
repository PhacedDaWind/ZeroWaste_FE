package com.example.zerowaste.data.repository

import com.example.zerowaste.data.remote.RetrofitClient

/**
 * The repository abstracts the data source. Its job is to provide data
 * to the ViewModel, hiding the fact that it's coming from a network API.
 */
class NotificationRepository {

    private val apiService = RetrofitClient.notificationApiService

    suspend fun getNotifications(userId: Long) = apiService.getNotificationList(userId)

    suspend fun markAsRead(notificationId: Long) = apiService.markAsRead(notificationId)
}