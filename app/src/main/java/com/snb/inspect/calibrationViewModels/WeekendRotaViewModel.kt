package com.snb.inspect.calibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snb.inspect.ApiService
import com.snb.inspect.dataClasses.WeekendRotaResponse
import com.snb.inspect.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeekendRotaViewModel(
    private val apiService: ApiService,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _rotaData = MutableStateFlow<List<WeekendRotaResponse>>(emptyList())
    val rotaData: StateFlow<List<WeekendRotaResponse>> = _rotaData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchRota() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.getWeekendRota()
                if (response.isSuccessful) {
                    val rawRota = response.body() ?: emptyList()
                    
                    val now = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val twelveMonthsLater = Calendar.getInstance().apply {
                        time = now.time
                        add(Calendar.MONTH, 12)
                    }

                    val formats = listOf("yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")

                    // Filter for next 12 months and enrich with names
                    val filteredAndEnriched = rawRota
                        .filter { item ->
                            val itemDate = parseDate(item.rotaDate, formats)
                            itemDate != null && (itemDate.after(now.time) || itemDate == now.time) && itemDate.before(twelveMonthsLater.time)
                        }
                        .sortedBy { parseDate(it.rotaDate, formats) } // Ensure chronological order
                        .map { item ->
                            val name = if (item.engineerName.isNullOrBlank() && item.engineerId != null) {
                                userRepository.getUsernameByMeaId(item.engineerId)
                            } else {
                                item.engineerName
                            }
                            item.copy(engineerName = name)
                        }
                    
                    _rotaData.value = filteredAndEnriched
                } else {
                    _error.value = "Failed to fetch rota: ${response.message()}"
                }
            } catch (e: UnknownHostException) {
                _error.value = "No internet connection. Please check your network and try again."
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun parseDate(dateString: String?, formats: List<String>): Date? {
        if (dateString.isNullOrBlank()) return null
        for (format in formats) {
            try {
                return SimpleDateFormat(format, Locale.getDefault()).parse(dateString)
            } catch (e: Exception) {
                // Try next
            }
        }
        return null
    }
}
