package com.example.zerowaste.data.remote

import retrofit2.Response
import retrofit2.http.*

/**
 * This interface defines all the callable endpoints from your NotificationController.
 * Retrofit uses this to generate the actual networking code.
 */
interface NotificationApiService {

    @GET("api/notification/list/{id}")
    suspend fun getNotificationList(
        @Path("id") userId: Long,
        @Query("notificationType") notificationType: NotificationType? = null
    ): Response<ResponseDTO<List<NotificationResponse>>>

    @GET("api/notification/unread/{id}")
    suspend fun getUnreadNotificationList(
        @Path("id") userId: Long
    ): Response<ResponseDTO<List<NotificationResponse>>>

    @PUT("api/notification/update/{id}")
    suspend fun markAsRead(
        @Path("id") notificationId: Long
    ): Response<ResponseDTO<NotificationResponse>>

    @DELETE("api/notification/delete/{id}")
    suspend fun deleteNotification(
        @Path("id") notificationId: Long
    ): Response<ResponseDTO<String>>

    @DELETE("api/notification/alldelete/{userId}")
    suspend fun deleteAllNotifications(
        @Path("userId") userId: Long
    ): Response<ResponseDTO<String>>
}