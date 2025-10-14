package com.example.zerowaste.data.remote

import com.google.gson.annotations.SerializedName

// --- Login Request (No changes needed) ---
data class LoginRequest(
    val username: String,
    val password: String
)

// --- Unified Login/2FA Response (UPDATED) ---
// This now exactly matches your backend's LoginResponse DTO.
data class LoginResponse(
    val status: String,
    val message: String,
    val token: String?, // This will be null when 2FA is required
    val userId: Long? // <-- ADD THIS FIELD to capture the user's ID
)

// --- 2FA Verification Request (UPDATED) ---
// This now sends the username and code, as required by your new API.
data class Verify2faRequest(
    val username: String,
    val code: String
)

// --- Generic and Registration DTOs (No changes needed) ---

// Generic wrapper for responses like registration
data class ApiResponse<T>(
    val data: T,
    val status: Int,
    val errorCode: String?
)

// For parsing custom API errors
data class ApiErrorResponse(
    val status: Int,
    @SerializedName("errorCode")
    val errorCode: String,
    val data: Any? = null
)

// Registration DTOs
data class RegistrationRequest(
    val username: String,
    val password: String,
    val email: String,
    val householdSize: Long?,
    val twoFactorAuthEnabled: Boolean?,
    val status: String
)

data class RegistrationResponse(
    val username: String,
    val email: String,
    val householdSize: Long?,
    val twoFactorAuthEnabled: Boolean?,
    val status: String
)

data class UserDetailsResponse(
    val id: Long,
    val username: String,
    val email: String,
    val householdSize: Long?,
    val twoFactorAuthEnabled: Boolean,
    val status: String, // Enums from backend are typically represented as Strings
    val totalItems: Long,
    val donationsMade: Long
)

data class ExpiringItem(val name: String, val quantity: String, val expiryDate: String)