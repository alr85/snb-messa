package com.example.mecca

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val BASE_URL = "https://snb-mea-web-apiapi.azure-api.net/api/"

    // Full body logging (for debugging)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // --- Stable, polite client ---
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)  // time allowed to connect
        .readTimeout(60, TimeUnit.SECONDS)     // time allowed to wait for response
        .writeTimeout(60, TimeUnit.SECONDS)    // time allowed to send data
        .retryOnConnectionFailure(true)        // retry automatically if connection drops
        .build()

    // --- Retrofit instance ---
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
