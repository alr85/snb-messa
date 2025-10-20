package com.example.mecca

import android.app.Application

class MyApplication : Application() {


    // Using the getDatabase method from AppDatabase to provide the instance
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    // API service initialization
    val apiService: ApiService by lazy {
        RetrofitClient.instance
    }
}