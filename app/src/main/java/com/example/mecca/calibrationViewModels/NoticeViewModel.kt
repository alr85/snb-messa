package com.example.mecca.calibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mecca.FetchResult
import com.example.mecca.repositories.NoticeRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class NoticeViewModel(
    private val repository: NoticeRepository
) : ViewModel() {

    val notices = repository.observeActiveNotices()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _events = MutableSharedFlow<String>(
        extraBufferCapacity = 1
    )
    val events = _events.asSharedFlow()

    fun syncNotices(force: Boolean = false) {

        if (_isRefreshing.value) return

        viewModelScope.launch {

            _isRefreshing.value = true

            val result = repository.fetchAndStoreNotices(force)

            val message = when (result) {
                is FetchResult.Success -> "✅ Notices updated"
                is FetchResult.Failure -> "⚠️ ${result.errorMessage}"
            }

            _events.tryEmit(message)

            _isRefreshing.value = false
        }
    }

}
