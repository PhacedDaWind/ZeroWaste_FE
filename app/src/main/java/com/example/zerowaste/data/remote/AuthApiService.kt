package com.example.zerowaste.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    // UPDATED: This now returns the new unified LoginResponse
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // UPDATED: This uses the new Verify2faRequest and returns the final LoginResponse
    @POST("api/auth/verify-2fa")
    suspend fun verify2fa(@Body request: Verify2faRequest): LoginResponse

    // No changes to registration
    @POST("api/registration/register")
    suspend fun registerUser(@Body request: RegistrationRequest): ApiResponse<RegistrationResponse>
}

