package com.example.zerowaste.data.remote

import com.google.gson.annotations.SerializedName

// --- This matches your backend's FoodItemReqDTO ---
// It's used for creating and updating items
data class FoodItemRequest(
    val name: String,
    val quantity: Long,
    val expiryDate: String?, // Format as "YYYY-MM-DD"
    val category: String?,
    val storageLocation: String?,
    val remarks: String?,
    val contactMethod: String?,
    val pickupLocation: String?,
    val actionType: String, // e.g., "PLAN_FOR_MEAL"
    val userId: Long,
    val convertToDonation: Boolean,
    val reservedQuantity: Long?,
    val donationQuantity: Long?
)

// --- This matches your backend's FoodItemResDTO ---
// This is the detailed response for a single item
data class FoodItemResponse(
    val id: Long,
    val name: String,
    val quantity: Long,
    val expiryDate: String?,
    val category: String?,
    val storageLocation: String?,
    val remarks: String?,
    val contactMethod: String?,
    val pickupLocation: String?,
    val actionType: String,
    val user: UserResponse, // Nested user object
    val convertToDonation: Boolean,
    val reservedQuantity: Long
)

// --- This matches your backend's UserResponseDTO ---
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val householdSize: Long?,
    val twoFactorAuthEnabled: Boolean,
    val status: String
)