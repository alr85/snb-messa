package com.snb.inspect.calibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.snb.inspect.repositories.CustomerRepository

class CustomerViewModelFactory(
    private val repository: CustomerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CustomerViewModel(repository) as T
    }
}
