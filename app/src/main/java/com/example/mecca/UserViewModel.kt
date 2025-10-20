package com.example.mecca

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mecca.Network.isNetworkAvailable
import com.example.mecca.Repositories.UserRepository
import com.example.mecca.util.InAppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    val syncStatus = MutableStateFlow(false)      // Tracks sync status
    val loginStatus = MutableStateFlow(false)     // Tracks login status
    private val showNoNetworkNoLoginDialog = MutableStateFlow(false)

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> get() = _loginError

    // -------------------------------------------------------------------------
    // LOGIN
    // -------------------------------------------------------------------------
    fun login(context: Context, username: String, password: String?) {
        viewModelScope.launch {
            if (username.isEmpty() || password.isNullOrEmpty()) {
                InAppLogger.d("Username or password is empty. Skipping login.")
                loginStatus.value = false
                return@launch
            }

            try {
                InAppLogger.d("Login started for username: $username")
                val user = userRepository.getUserByUsername(username)

                if (user != null && user.isActive) {
                    val generatedPassword = MasterPasswordGenerator.generateWeeklyPassword()
                    InAppLogger.d("Generated weekly password")
                   Log.d("MESSA-DEBUG","Generated weekly password: $generatedPassword")
                    if (password == generatedPassword.toString()) {
                        PreferencesHelper.saveCredentials(context, username, password, user.meaId)
                        InAppLogger.d("Login successful. Credentials saved.")
                        loginStatus.value = true
                        _loginError.value = null
                    } else {
                        InAppLogger.e("Incorrect password for $username. Login failed.")
                        _loginError.value = "Incorrect username or password."
                        loginStatus.value = false
                    }
                } else {
                    InAppLogger.e("User not found or inactive: $username")
                    _loginError.value = "User not found or inactive. Login failed."
                    loginStatus.value = false
                }
            } catch (e: Exception) {
                InAppLogger.e("Error during login: ${e.message}")
                _loginError.value = "Error during login: ${e.message}"
                loginStatus.value = false
            }
        }
    }

    // -------------------------------------------------------------------------
    // USER SYNC
    // -------------------------------------------------------------------------
    fun syncUsers(context: Context) {
        viewModelScope.launch {
            try {
                val hasLocalUsers = userRepository.hasLocalUsers()
                val hasInternet = isNetworkAvailable(context)

                // Case 1: no users + no internet = hard stop
                if (!hasLocalUsers && !hasInternet) {
                    InAppLogger.e("No internet and no local users. Cannot proceed.")
                    _loginError.value =
                        "No users available. Please connect to the internet to sync for the first time."
                    syncStatus.value = false
                    return@launch
                }

                // Case 2: internet available → attempt full sync
                if (hasInternet) {
                    userRepository.syncUsers()
                    InAppLogger.d("User sync complete.")
                } else {
                    InAppLogger.d("Offline mode: using local users.")
                }

                syncStatus.value = true

            } catch (e: Exception) {
                InAppLogger.e("Error syncing users: ${e.message}")
                _loginError.value = "Sync failed: ${e.message ?: "Unknown error"}"
                syncStatus.value = false
            }
        }
    }

    // -------------------------------------------------------------------------
    // MISC
    // -------------------------------------------------------------------------
    fun showNoNetworkNoLoginError() {
        showNoNetworkNoLoginDialog.value = true
        InAppLogger.e("Displayed 'No Network / No Login' error dialog.")
    }

    fun setSyncStatus(status: Boolean) {
        syncStatus.value = status
        InAppLogger.d("Sync status manually set to: $status")
    }

    fun getUserId(username: String, onResult: (Int?) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = userRepository.fetchUserIdByUsername(username)
                InAppLogger.d("Fetched userId for $username: $userId")
                onResult(userId)
            } catch (e: Exception) {
                InAppLogger.e("Error fetching userId for $username: ${e.message}")
                onResult(null)
            }
        }
    }
}
