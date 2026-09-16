package com.snb.inspect.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snb.inspect.network.AzureCloudHealthReport
import com.snb.inspect.network.AzureStatusManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AzureCloudStatusUiState(
    val isLoading: Boolean = false,
    val report: AzureCloudHealthReport? = null,
    val errorMessage: String? = null
)

class AzureCloudStatusViewModel(private val azureStatusManager: AzureStatusManager) : ViewModel() {

    private val _uiState = MutableStateFlow(AzureCloudStatusUiState())
    val uiState: StateFlow<AzureCloudStatusUiState> = _uiState.asStateFlow()

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val report = azureStatusManager.checkHealth()
                _uiState.update { it.copy(isLoading = false, report = report) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = e.localizedMessage ?: "Failed to check Azure status"
                    ) 
                }
            }
        }
    }
}
