package com.example.mecca

import android.app.Application
import androidx.room.Room
import com.example.mecca.AppDatabase

//class MyApplication : Application() {
//    val database: AppDatabase by lazy {
//        Room.databaseBuilder(
//            applicationContext,
//            AppDatabase::class.java, "my-database"
//        ).build()
//    }
//}

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