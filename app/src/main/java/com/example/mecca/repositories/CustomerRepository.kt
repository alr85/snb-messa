package com.example.mecca.repositories

import androidx.room.withTransaction
import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.FetchResult
import com.example.mecca.dataClasses.CustomerLocal
import com.example.mecca.util.InAppLogger
import com.example.mecca.util.SyncPreferences

class CustomerRepository(
    private val apiService: ApiService,
    private val db: AppDatabase,
    private val syncPrefs: SyncPreferences
) {

    fun observeCustomers() = db.customerDao().observeCustomers()

    private fun shouldRefresh(): Boolean {

        val lastSync = syncPrefs.getCustomerLastSync()
        val twelveHours = 12 * 60 * 60 * 1000L

        val should = System.currentTimeMillis() - lastSync > twelveHours

        InAppLogger.d(
            "SYNC CHECK -> lastSync=$lastSync | shouldRefresh=$should"
        )

        return should
    }





    suspend fun getCustomerName(fusionId: Int): String {
        val name = db.customerDao().getCustomerName(fusionId) ?: "Customer Not Found"
        InAppLogger.d("Fetched customer name for fusionId=$fusionId -> $name")
        return name
    }

    suspend fun fetchAndStoreCustomers(force: Boolean = false): FetchResult
    {

        if (!force && !shouldRefresh()) {
            return FetchResult.Success("Customer list already up to date")
        }

        if (force) {
            InAppLogger.d("FORCED CUSTOMER SYNC")
        }

        InAppLogger.d("Fetching customers from API...")

        return try {
            val response = apiService.getCustomers()
            InAppLogger.d("API call complete. HTTP ${response.code()}")

            if (!response.isSuccessful) {
                val msg = "HTTP ${response.code()} error: ${response.message()}"
                InAppLogger.e(msg)
                return FetchResult.Failure(msg)
            }

            val apiCustomers = response.body()
            if (apiCustomers.isNullOrEmpty()) {
                return FetchResult.Failure("No customer data returned by API.")
            }

            val customerLocals = apiCustomers.map { apiCustomer ->
                CustomerLocal(
                    id = 0,
                    name = apiCustomer.customerName,
                    fusionID = apiCustomer.fusionID,
                    postcode = apiCustomer.customerPostcode,
                    dateAdded = apiCustomer.dateAdded,
                    lat = apiCustomer.latitude,
                    lon = apiCustomer.longitude,
                    customerCityTown = apiCustomer.customerCityTown
                )
            }


            db.withTransaction {

                db.customerDao().deleteAllCustomers()

                db.customerDao().insertCustomer(customerLocals)

                syncPrefs.setCustomerLastSync(System.currentTimeMillis())
            }

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


