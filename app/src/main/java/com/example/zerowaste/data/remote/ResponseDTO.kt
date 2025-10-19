package com.example.zerowaste.data.remote

import com.google.gson.annotations.SerializedName
data class ResponseDTO<T>(
    val status: Int,
    val message: String,
    val data: T
)
