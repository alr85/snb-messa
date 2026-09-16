package com.snb.inspect.network

import com.snb.inspect.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import kotlin.system.measureTimeMillis

data class EndpointStatus(
    val name: String,
    val url: String,
    val isHealthy: Boolean,
    val latencyMs: Long,
    val statusCode: Int,
    val message: String
)

data class AzureCloudHealthReport(
    val overallStatus: CloudHealthState,
    val endpoints: List<EndpointStatus>,
    val timestamp: Long = System.currentTimeMillis()
)

enum class CloudHealthState {
    HEALTHY, DEGRADED, OFFLINE
}

class AzureStatusManager(private val apiService: ApiService) {

    suspend fun checkHealth(): AzureCloudHealthReport = withContext(Dispatchers.IO) {
        val endpoints = mutableListOf<EndpointStatus>()

        // 1. Check Base API Gateway (Users endpoint)
        endpoints.add(checkEndpoint("Base API (Users)", "https://snb-mea-web-apiapi.azure-api.net/api/Users") {
            apiService.getUsers()
        })

        // 2. Check Customers API
        endpoints.add(checkEndpoint("Customers API", "https://snb-mea-web-apiapi.azure-api.net/api/Customers") {
            apiService.getCustomers()
        })

        // 3. Check MD Systems API
        endpoints.add(checkEndpoint("MD Systems API", "https://snb-mea-web-apiapi.azure-api.net/api/MdSystems") {
            apiService.getMdSystems()
        })

        // 4. Check Weekend Rota API
        endpoints.add(checkEndpoint("Weekend Rota API", "https://snb-mea-web-api20240909215557.azurewebsites.net/api/WeekendRota") {
            apiService.getWeekendRota()
        })

        val healthyCount = endpoints.count { it.isHealthy }
        val overall = when {
            healthyCount == endpoints.size -> CloudHealthState.HEALTHY
            healthyCount > 0 -> CloudHealthState.DEGRADED
            else -> CloudHealthState.OFFLINE
        }

        AzureCloudHealthReport(
            overallStatus = overall,
            endpoints = endpoints
        )
    }

    private suspend fun <T> checkEndpoint(name: String, url: String, call: suspend () -> Response<List<T>>): EndpointStatus {
        var statusCode = -1
        var isHealthy = false
        var message = "Success"
        var latency = 0L

        try {
            val response: Response<List<T>>
            latency = measureTimeMillis {
                response = call()
            }
            statusCode = response.code()
            isHealthy = response.isSuccessful
            message = if (response.isSuccessful) "HTTP $statusCode OK" else "HTTP $statusCode: ${response.message()}"
        } catch (e: Exception) {
            isHealthy = false
            message = e.localizedMessage ?: "Connection error"
        }

        return EndpointStatus(
            name = name,
            url = url,
            isHealthy = isHealthy,
            latencyMs = latency,
            statusCode = statusCode,
            message = message
        )
    }
}
