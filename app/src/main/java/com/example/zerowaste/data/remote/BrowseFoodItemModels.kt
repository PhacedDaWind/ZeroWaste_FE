package com.example.zerowaste.data.remote

import com.example.zerowaste.enums.FoodItemActionType


/**
 * Mirrors `BrowseFoodItemResDTO.java` from your backend.
 */
data class BrowseFoodItemResDTO(
    val id: Long,
    val usersId: Long,
    val convertToDonation: Boolean,
    val category: String,
    val expiryDate: String,
    val storageLocation: String,
    val username: String,
    val itemName: String,
    val quantity: Long,
    val pickupLocation: String?,
    val contactMethod: String?,
    val actionType: FoodItemActionType?,
    val actionTypeLabel: String?
)

/**
 * Mirrors `BrowseFoodItemReqDTO.java` for sending filter and pagination info.
 */
data class BrowseFoodItemReqDTO(
    val usersId: Long? = null,
    val convertToDonation: Boolean? = null,
    val category: String? = null,
    val expiryDate: String? = null,
    val storageLocation: String? = null,
    val actionType: FoodItemActionType? = null,
    val page: Int = 0,
    val size: Int = 20,
    val sort: List<String>? = null
)
