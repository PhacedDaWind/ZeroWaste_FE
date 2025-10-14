package com.example.zerowaste.data.remote

import android.content.Context
import com.example.zerowaste.data.local.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    // We keep a single instance to avoid re-creating it unnecessarily
    private var apiService: AuthApiService? = null

    fun getInstance(context: Context): AuthApiService {
        // If the instance doesn't exist, create it
        if (apiService == null) {
            // 1. Create the SessionManager
            val sessionManager = SessionManager(context.applicationContext)

            // 2. Create the AuthInterceptor with the SessionManager
            val authInterceptor = AuthInterceptor(sessionManager)

            // 3. Create an OkHttpClient and add the interceptor
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .build()

            // 4. Build Retrofit with the custom OkHttpClient
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            // 5. Create the ApiService instance
            apiService = retrofit.create(AuthApiService::class.java)
        }
        return apiService!!
    }
}

