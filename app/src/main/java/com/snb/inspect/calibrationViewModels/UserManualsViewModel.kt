package com.snb.inspect.calibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snb.inspect.repositories.UserManualsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserManualsViewModel(private val repository: UserManualsRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType

    val filteredManuals = combine(
        repository.allManuals,
        _searchQuery,
        _selectedType
    ) { manuals, query, type ->
        manuals.filter { manual ->
            (type == null || manual.systemType == type) &&
            (query.isEmpty() || manual.description.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedType(type: String?) {
        _selectedType.value = type
    }

    fun syncManuals() {
        viewModelScope.launch {
            repository.fetchAndStoreManuals()
        }
    }
}
