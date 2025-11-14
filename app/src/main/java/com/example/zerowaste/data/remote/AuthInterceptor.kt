package com.example.zerowaste.data.remote

import com.example.zerowaste.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // --- THIS IS THE KEY FIX ---
        // Call encodedPath() as a function
        if (originalRequest.url().encodedPath().contains("/api/auth/") ||
            originalRequest.url().encodedPath().contains("/api/registration/") ||
            originalRequest.url().encodedPath().contains("/api/password-reset/")) {
            // This is a public request, let it proceed as-is
            return chain.proceed(originalRequest)
        }

        // For all other (protected) requests, add the token
        sessionManager.getToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}

