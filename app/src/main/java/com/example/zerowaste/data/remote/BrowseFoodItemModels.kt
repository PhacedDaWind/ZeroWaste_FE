package com.example.zerowaste.data.remote

data class BrowseFoodItemResponse (
    val id: Long,
    val usersId: Long,
    val convertToDonation: Boolean,
    val category: String?,
    val expiryDate: String?,
    val storageLocation: String?,
    val userName: String?,
    val quantity: Long?,
    val pickupLocation: String?,
    val contactMethod: String?
)