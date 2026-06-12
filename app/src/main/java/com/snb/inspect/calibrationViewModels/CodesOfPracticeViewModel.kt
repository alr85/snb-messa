package com.snb.inspect.calibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snb.inspect.FetchResult
import com.snb.inspect.dataClasses.CodeOfPracticeLocal
import com.snb.inspect.repositories.CodesOfPracticeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CodesOfPracticeViewModel(private val repository: CodesOfPracticeRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val filteredCodes: StateFlow<List<CodeOfPracticeLocal>> = combine(
        repository.allCodes,
        _searchQuery,
        _selectedCategory
    ) { codes, query, category ->
        codes.filter { code ->
            val matchesQuery = code.title.contains(query, ignoreCase = true) ||
                    (code.description?.contains(query, ignoreCase = true) ?: false)
            val matchesCategory = category == null || code.category == category
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun syncCodes() {
        viewModelScope.launch {
            repository.fetchAndStoreCodes()
        }
    }
}
