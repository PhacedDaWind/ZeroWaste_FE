package com.example.zerowaste.data.remote

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.util.Date

/**
 * Generic response wrapper (matches backend ResponseDTO)
 */

/**
 * Notification model matching backend NotificationResDTO
 */
data class NotificationResponse(
    @SerializedName("id")
    val id: Long,

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
) {
    val title: String
        get() = when (notifType) {
            NotificationType.FOOD_INVENTORY_ALERT -> "Inventory Alert!"
            NotificationType.DONATION_POSTED -> "Donation Posted"
            NotificationType.DONATION_CLAIMED -> "Claimed Donation"
            NotificationType.MEAL_REMINDER -> "Meal Planner Reminder"
        }
}

data class NotificationReqDTO(
    val notifType: NotificationType,
    val usersId: Long,
    val itemName: List<String>?,
    val quantity: List<Long>?,
    val expiryDate: LocalDate?,
    val meal: String?
)

/**
 * Enum mirrors backend NotificationType
 */
enum class NotificationType {
    FOOD_INVENTORY_ALERT,
    DONATION_POSTED,
    DONATION_CLAIMED,
    MEAL_REMINDER
}
