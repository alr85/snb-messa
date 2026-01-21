package com.example.mecca.repositories

import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.FetchResult
import com.example.mecca.dataClasses.CustomerLocal
import com.example.mecca.util.InAppLogger

class CustomerRepository(
    private val apiService: ApiService,
    private val db: AppDatabase
) {

    suspend fun getCustomerName(fusionId: Int): String {
        val name = db.customerDao().getCustomerName(fusionId) ?: "Customer Not Found"
        InAppLogger.d("Fetched customer name for fusionId=$fusionId -> $name")
        return name
    }

    suspend fun fetchAndStoreCustomers(): FetchResult {
        InAppLogger.d("Fetching customers from API...")

        return try {
            val response = apiService.getCustomers()
            InAppLogger.d("API call to fetch customers complete. HTTP ${response.code()}")

            if (!response.isSuccessful) {
                val msg = "HTTP ${response.code()} error: ${response.message()}"
                InAppLogger.e(msg)
                return FetchResult.Failure(msg)
            }

            val apiCustomers = response.body()
            if (apiCustomers.isNullOrEmpty()) {
                val msg = "No customer data returned by API."
                InAppLogger.e(msg)
                return FetchResult.Failure(msg)
            }

            // Clear DB
            try {
                db.customerDao().deleteAllCustomers()
                InAppLogger.d("Cleared local customer database.")
            } catch (e: Exception) {
                val msg = "Error clearing customer database: ${e.message}"
                InAppLogger.e(msg)
                return FetchResult.Failure(msg)
            }

            // Map + insert
            val customerLocals = apiCustomers.map { apiCustomer ->
                CustomerLocal(
                    id = 0,
                    name = apiCustomer.customerName,              // remove ?: if non-nullable
                    fusionID = apiCustomer.fusionID,              // remove ?: if non-nullable
                    postcode = apiCustomer.customerPostcode,
                    dateAdded = apiCustomer.dateAdded,
                    lat = apiCustomer.latitude,
                    lon = apiCustomer.longitude,
                    customerCityTown = apiCustomer.customerCityTown
                )
            }

            db.customerDao().insertCustomer(customerLocals)
            InAppLogger.d("Inserted ${customerLocals.size} customers into local DB.")

            FetchResult.Success("Customers successfully fetched and stored.")

        } catch (e: Exception) {
            val msg = "Exception during fetch: ${e.message}"
            InAppLogger.e(msg)
            FetchResult.Failure(msg)
        }
    }

    suspend fun getCustomersFromDb(): List<CustomerLocal> {
        val customers = db.customerDao().getAllCustomers()
        InAppLogger.d("Fetched ${customers.size} customers from local DB.")
        return customers
    }
}


