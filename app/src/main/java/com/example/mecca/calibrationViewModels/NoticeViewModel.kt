package com.example.mecca.calibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mecca.repositories.NoticeRepository
import kotlinx.coroutines.launch

class NoticeViewModel(
    private val repository: NoticeRepository
) : ViewModel() {

    val notices = repository.observeActiveNotices()

    fun syncNotices() {
        viewModelScope.launch {
            repository.fetchAndStoreNotices()
        }
    }
}