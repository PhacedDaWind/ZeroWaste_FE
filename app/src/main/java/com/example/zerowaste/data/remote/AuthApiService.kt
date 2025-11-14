package com.example.zerowaste.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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

    @GET("api/user/{id}")
    suspend fun getUserDetails(@Path("id") userId: Long): ApiResponse<UserDetailsResponse>

    @POST("api/security/enable-2fa/{id}")
    suspend fun enable2fa(@Path("id") userId: Long): ApiResponse<Any>

    @POST("api/security/disable-2fa/{id}")
    suspend fun disable2fa(@Path("id") userId: Long): ApiResponse<Any>

    @POST("api/security/verify-2fa-setup/{id}")
    suspend fun verify2faSetup(@Path("id") userId: Long, @Body request: Verify2faSetupRequest): ApiResponse<Any>

    @POST("api/password-reset/request")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequest): ApiResponse<String>

    @POST("api/password-reset/execute")
    suspend fun executePasswordReset(@Body request: PasswordResetExecute): ApiResponse<String>
}

