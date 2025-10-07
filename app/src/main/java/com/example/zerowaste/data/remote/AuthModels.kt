package com.example.zerowaste.data.remote

// This must match the JSON body your API expects
data class LoginRequest(
    val username: String,
    val password: String
)

// This must match the JSON response your API sends
data class LoginResponse(
    val token: String
)