package com.example.mecca


import MasterPasswordGenerator
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mecca.Repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    val syncStatus = MutableStateFlow(false) // Tracks sync status
    val loginStatus = MutableStateFlow(false) // Tracks login status
    private val showNoNetworkNoLoginDialog = MutableStateFlow(false)

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> get() = _loginError

    fun login(context: Context, username: String, password: String?) {
        viewModelScope.launch {
            if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
                Log.d("login", "Username or password is empty. Skipping login.")
                loginStatus.value = false
                return@launch
            }

            try {
                Log.d("login", "Login started for Username: $username")
                val user = userRepository.getUserByUsername(username)
                if (user != null && user.isActive) {
                    val generatedPassword = MasterPasswordGenerator.generateWeeklyPassword()
                    Log.d("login", "Password generated: $generatedPassword")
                    if (password == generatedPassword.toString()) {
                        PreferencesHelper.saveCredentials(context, username, password, user.meaId) // Save credentials
                        Log.d("login", "Incorrect password. Login failed.")
                        loginStatus.value = true
                        _loginError.value = null // Clear error on successful login
                    } else {
                        Log.d("login", "Incorrect password. Login failed.")
                        _loginError.value = "Incorrect username or password."
                        loginStatus.value = false
                    }
                } else {
                    Log.d("login", "User not found or inactive. Login failed.")
                    loginStatus.value = false
                    _loginError.value = "User not found or inactive. Login failed"
                }
            } catch (e: Exception) {
                Log.e("login", "Error during login: ${e.message}")
                loginStatus.value = false
                _loginError.value = "Error during login: ${e.message}"
            }
        }
    }

    fun syncUsers() {
        viewModelScope.launch {
            try {
                userRepository.syncUsers()
                syncStatus.value = true
            } catch (e: Exception) {
                Log.e("login", "Error syncing users: ${e.message}")
                syncStatus.value = false
            }
        }
    }

    fun showNoNetworkNoLoginError() {
        showNoNetworkNoLoginDialog.value = true
    }

    fun setSyncStatus(status: Boolean) {
        syncStatus.value = status
    }

    fun getUserId(username: String, onResult: (Int?) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = userRepository.fetchUserIdByUsername(username)
                onResult(userId)
            } catch (e: Exception) {
                Log.e("login", "Error fetching userId: ${e.message}")
                onResult(null)
            }
        }
    }
}


