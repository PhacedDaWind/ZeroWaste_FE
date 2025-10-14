package com.example.zerowaste.data.remote

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * A generic class that matches your backend's ResponseDTO wrapper.
 * Your BaseController creates this structure for every response.
 */
data class ResponseDTO<T>(
    @SerializedName("data")
    val data: T
)

/**
 * This data class represents a single notification object.
 * It's designed to perfectly match the fields in your NotificationResDTO.java.
 */
data class NotificationResponse(
    @SerializedName("id")
    val id: Long,

    // IMPORTANT: This must be java.util.Date to correctly parse the JSON from your backend.
    @SerializedName("createdAt")
    val createdAt: Date,

    @SerializedName("username")
    val username: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("markAsRead")
    val markAsRead: Boolean,

    @SerializedName("notifType")
    val notifType: NotificationType
)

/**
 * This enum mirrors your backend's NotificationType enum for type safety.
 */
enum class NotificationType {
    FOOD_INVENTORY_ALERT,
    DONATION_POSTED,
    DONATION_CLAIMED,
    MEAL_REMINDER
}