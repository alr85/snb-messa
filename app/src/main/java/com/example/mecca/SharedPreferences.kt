package com.example.mecca

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object PreferencesHelper {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"
    private const val KEY_ENGINEER_ID = "engineer_id"

    // Save credentials, including userID
    fun saveCredentials(context: Context, username: String, password: String, engineerId: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .putInt(KEY_ENGINEER_ID, engineerId)
            .apply()
    }

    fun getCredentials(context: Context): Triple<String?, String?, Int?> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(KEY_USERNAME, null)
        val password = prefs.getString(KEY_PASSWORD, null)
        val engineerId = prefs.getInt(KEY_ENGINEER_ID, -1).takeIf { it != -1 }
        return Triple(username, password, engineerId)
    }

    // Clear credentials
    fun clearCredentials(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        Log.d("LoginDebug", "Before Clearing: ${sharedPreferences.all}")

        editor.clear().commit() // Clears all stored preferences synchronously

        Log.d("LoginDebug", "After Clearing: ${sharedPreferences.all}")
    }
}

