package com.example.zerowaste.data.remote

import com.google.gson.annotations.SerializedName

data class PageWrapper<T>(
    @SerializedName("content")
    val content: List<T>,

    @SerializedName("pageNumber")
    val pageNumber: Int,

    @SerializedName("pageSize")
    val pageSize: Int,

    @SerializedName("totalElements")
    val totalElements: Long,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("last")
    val last: Boolean,

    @SerializedName("first")
    val first: Boolean,

    @SerializedName("hasNext")
    val hasNext: Boolean,

    @SerializedName("hasPrevious")
    val hasPrevious: Boolean,

    @SerializedName("hasContent")
    val hasContent: Boolean
)

// This matches the BrowseFoodItemResDTO from your backend.
data class BrowseFoodItemResponse(
    val id: Long,
    val usersId: Long,
    val convertToDonation: Boolean,
    val category: String?,
    val expiryDate: String?, // Dates are often strings in JSON
    val storageLocation: String?,
    val username: String,
    val itemName: String,
    val quantity: Long,
    val pickupLocation: String?,
    val contactMethod: String?,
    val actionType: String,
    val actionTypeLabel: String
)

// This matches the FoodItemResDTO from your backend.
data class FoodItemDetailResponse(
    val id: Long,
    val name: String,
    val quantity: Long,
    val expiryDate: String?,
    val category: String?,
    val storageLocation: String?,
    val remarks: String?,
    val contactMethod: String?,
    val pickupLocation: String?,
    val actionType: String?, // Enums are usually strings in JSON
    val user: UserInfo, // A nested object for user details
    val convertToDonation: Boolean,
    val reservedQuantity: Long,
    val userId: Long
)

// A simple data class for the nested user object in the response.
data class UserInfo(
    val id: Long,
    val username: String
)
