package com.example.mecca.calibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mecca.FetchResult
import com.example.mecca.repositories.CustomerRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    val customers = repository.observeCustomers()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    init {
        syncCustomers(force = false)
    }


    fun syncCustomers(force: Boolean = false) {

        if (_isRefreshing.value) return

        viewModelScope.launch {

            _isRefreshing.value = true

            val result = repository.fetchAndStoreCustomers(force)

            val message = when (result) {
                is FetchResult.Success ->
                    if (force) "Customer list refreshed"
                    else result.message

                is FetchResult.Failure ->
                    result.errorMessage
            }

            _events.tryEmit(message)

            _isRefreshing.value = false
        }
    }
}

