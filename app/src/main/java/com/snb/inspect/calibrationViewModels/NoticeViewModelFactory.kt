package com.snb.inspect.calibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.snb.inspect.repositories.NoticeRepository

class NoticeViewModelFactory(
    private val repository: NoticeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NoticeViewModel(repository) as T
    }
}
