package com.example.zerowaste.data.remote

import android.content.Context
import com.example.zerowaste.data.local.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    // We keep a single instance to avoid re-creating it unnecessarily
    private var retrofit: Retrofit? = null

    private fun getRetrofit(context: Context): Retrofit {
        if (retrofit == null) {
            val sessionManager = SessionManager(context.applicationContext)
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager))
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun getAuthApi(context: Context): AuthApiService =
        getRetrofit(context).create(AuthApiService::class.java)

    fun getBrowseFoodApi(context: Context): BrowseFoodItemApiService =
        getRetrofit(context).create(BrowseFoodItemApiService::class.java)

    fun getNotificationApi(context: Context): NotificationApiService =
        getRetrofit(context).create(NotificationApiService::class.java)
}


