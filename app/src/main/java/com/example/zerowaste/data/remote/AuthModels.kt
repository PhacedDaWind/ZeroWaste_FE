package com.example.zerowaste.data.remote

// --- Login Request ---
// This is sent when the user first enters their username and password.
data class LoginRequest(
    val username: String,
    val password: String
)

// --- Unified Login Response ---
// This is the flexible response from the first login call.
data class UnifiedLoginResponse(
    val message: String,
    val twoFactorEnabled: Boolean, // Flag to tell the app what to do next
    val token: String?,            // The final JWT (if 2FA is OFF)
    val twoFactorToken: String?     // The temporary token (if 2FA is ON)
)

// --- 2FA Verification Request ---
// This is sent after the user enters their 2FA code.
data class Verify2faRequest(
    val twoFactorToken: String,
    val code: String
)

// --- Final Login Response ---
// The final response from a successful 2FA verification.
data class FinalLoginResponse(
    val token: String
)