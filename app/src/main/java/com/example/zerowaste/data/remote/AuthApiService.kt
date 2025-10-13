package com.example.zerowaste.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    // This now returns our new, flexible response object.
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): UnifiedLoginResponse

    // The 2FA verification endpoint.
    @POST("api/auth/verify-2fa")
    suspend fun verify2fa(@Body request: Verify2faRequest): FinalLoginResponse
}

