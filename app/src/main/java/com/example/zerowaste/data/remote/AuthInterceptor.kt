package com.example.zerowaste.data.remote

import com.example.zerowaste.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Get the original request
        val requestBuilder = chain.request().newBuilder()

        // Get the token from SessionManager
        sessionManager.getToken()?.let { token ->
            // If the token exists, add the Authorization header
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        // Proceed with the modified request
        return chain.proceed(requestBuilder.build())
    }
}
