package com.example.mecca.repositories

import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.FetchResult
import com.example.mecca.dataClasses.CustomerLocal
import com.example.mecca.util.InAppLogger

class CustomerRepository(private val apiService: ApiService, private val db: AppDatabase) {

    // Fetch customer name from the local Room database
    suspend fun getCustomerName(fusionId: Int): String {
        val name = db.customerDao().getCustomerName(fusionId) ?: "Customer Not Found"
        InAppLogger.d("Fetched customer name for fusionId=$fusionId -> $name")
        return name
    }

    // Function to fetch data from the API and store it in the database
    suspend fun fetchAndStoreCustomers(): FetchResult {

        InAppLogger.d("Fetching customers from API...")

        return try {
            val response = apiService.getCustomers()
            InAppLogger.d("API call to fetch customers complete. HTTP ${response.code()}")

            if (response.isSuccessful) {
                val apiCustomers = response.body()
                InAppLogger.d("API body received: ${apiCustomers?.size ?: 0} customers")

                if (apiCustomers != null) {
                    try {
                        db.customerDao().deleteAllCustomers()
                        InAppLogger.d("Cleared local customer database.")
                    } catch (e: Exception) {
                        val errorMessage = "Error clearing customer database: ${e.message}"
                        InAppLogger.e(errorMessage)
                        return FetchResult.Failure(errorMessage)
                    }

                    val customerLocals = apiCustomers.mapIndexed { index, apiCustomer ->
//                        InAppLogger.d(
//                            "Mapping customer $index: FusionID=${apiCustomer.fusionID}, " +
//                                    "Name=${apiCustomer.customerName ?: "Null"}"
//                        )

                        CustomerLocal(
                            id = 0, // Auto-generate ID in the Room database
                            name = apiCustomer.customerName ?: "Unknown Name",
                            fusionID = apiCustomer.fusionID ?: 0,
                            postcode = apiCustomer.customerPostcode ?: "Unknown Postcode",
                            dateAdded = apiCustomer.dateAdded ?: "Unknown Date",
                            lat = apiCustomer.latitude ?: 0.0,
                            lon = apiCustomer.longitude ?: 0.0,
                            customerCityTown = apiCustomer.customerCityTown ?: "Unknown City/Town"
                        )
                    }

                    db.customerDao().insertCustomer(customerLocals)
                    InAppLogger.d("Inserted ${customerLocals.size} customers into local DB.")

                    return FetchResult.Success("Customers successfully fetched and stored.")
                } else {
                    val errorMessage = "No customer data returned by API."
                    InAppLogger.e(errorMessage)
                    return FetchResult.Failure(errorMessage)
                }
            } else {
                val errorMessage = "HTTP ${response.code()} error: ${response.message()}"
                InAppLogger.e(errorMessage)
                return FetchResult.Failure(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = "Exception during fetch: ${e.message}"
            InAppLogger.e(errorMessage)
            return FetchResult.Failure(errorMessage)
        }
    }

    // Function to get all customers from the local database
    suspend fun getCustomersFromDb(): List<CustomerLocal> {
        val customers = db.customerDao().getAllCustomers()
        InAppLogger.d("Fetched ${customers.size} customers from local DB.")
        return customers
    }
}
