package com.example.mecca

import android.util.Log

class CustomerRepository(private val apiService: ApiService, private val db: AppDatabase) {

    // Fetch customer name from the local Room database
    suspend fun getCustomerName(fusionId: Int): String {
        return db.customerDao().getCustomerName(fusionId) ?: "Customer Not Found"
    }

    // Function to fetch data from the API and store it in the database
    suspend fun fetchAndStoreCustomers(): FetchResult {

        Log.d("DEBUG", "Fetching Customers from API...")

        return try {
            // Make the API call
            val response = apiService.getCustomers()

            // Check if the response was successful
            if (response.isSuccessful) {
                val apiCustomers = response.body()
                if (apiCustomers != null) {

                    // Clear the existing records before inserting new ones
                    try {
                        db.customerDao().deleteAllCustomers()
                        Log.d("DEBUG", "Cleared local customer database")
                    } catch (e: Exception) {
                        val errorMessage = "Error clearing customer database: ${e.message}"
                        Log.e("DEBUG", errorMessage)
                        return FetchResult.Failure(errorMessage)
                    }

                    // Map API data to local entities
                    val customerLocals = apiCustomers.mapIndexed { index, apiCustomers ->

                        Log.d(
                            "DEBUG",
                            "Mapping MD Systems $index: ID=${apiCustomers.fusionID}, Name=${apiCustomers.customerName ?: "Null"}"
                        )

                        // Return MD CustomerLocal for insertion
                        CustomerLocal(
                            id = 0,  // Auto-generate ID in the Room database
                            name = apiCustomers.customerName
                                ?: "Unknown Name",  // Default to "Unknown Name" if null
                            fusionID = apiCustomers.fusionID ?: 0,  // Default to 0 if null
                            postcode = apiCustomers.customerPostcode
                                ?: "Unknown Postcode",  // Handle null postcode
                            dateAdded = apiCustomers.dateAdded
                                ?: "Unknown Date",  // Handle null date
                            lat = apiCustomers.latitude ?: 0.0,  // Handle null latitude
                            lon = apiCustomers.longitude ?: 0.0,  // Handle null longitude
                            customerCityTown = apiCustomers.customerCityTown ?: "Unknown City/Town"
                        )
                    }

                    // Insert the mapped data into the local database
                    db.customerDao().insertCustomer(customerLocals)
                    Log.d("DEBUG", "System Types successfully inserted into the local database.")

                    return FetchResult.Success("System Types successfully fetched and stored")

                } else {
                    // Handle the case where body is null
                    val errorMessage = "No data found."
                    Log.e("API", errorMessage)
                    return FetchResult.Failure(errorMessage)
                }
            } else {
                // Handle HTTP errors (4xx, 5xx)
                val errorMessage = "Error: ${response.code()}, Message: ${response.message()}"
                Log.e("API", errorMessage)
                return FetchResult.Failure(errorMessage)
            }
        } catch (e: Exception) {
            // Handle any exceptions that occurred during the API call
            val errorMessage = "Exception occurred: ${e.message}"
            Log.e("API", errorMessage)
            return FetchResult.Failure(errorMessage)
        }
    }

    // Function to get all customers from the local database
    suspend fun getCustomersFromDb(): List<CustomerLocal> {
        return db.customerDao().getAllCustomers()  // Modify as per your DAO method
    }

}


//    // Function to fetch data from the API and store it in the database
//    suspend fun fetchAndStoreCustomers() {
//        Log.d("DEBUG", "Fetching customers from API...")
//        try {
//            // Fetch customers from the API
//            val apiCustomers = apiService.getCustomers()
//            Log.d("DEBUG", "API Response: $apiCustomers")
//
//            // Add log before clearing the database
//            Log.d("DEBUG", "Attempting to clear local customer database...")
//            // Clear the local database
//            db.customerDao().deleteAllCustomers()
//            Log.d("DEBUG", "Cleared local customer database")
//d
//
//            // Map the API response to Room entities
//            val customerLocals = apiCustomers.mapIndexed { index, apiCustomer ->
//                //Log.d("DEBUG", "Mapping customer $index: ID=${apiCustomer.customerID}, Name=${apiCustomer.customerName ?: "Null"}, FusionID=${apiCustomer.fusionID ?: "Null"}, Postcode=${apiCustomer.customerPostcode ?: "Null"}")
//                Log.d("DEBUG", "City = ${apiCustomer.customerCityTown}")
//                Log.d("DEBUG", "API Customer: $apiCustomer")
//
//                CustomerLocal(
//                    id = 0,  // Auto-generate ID in the Room database
//                    name = apiCustomer.customerName ?: "Unknown Name",  // Default to "Unknown Name" if null
//                    fusionID = apiCustomer.fusionID ?: 0,  // Default to 0 if null
//                    postcode = apiCustomer.customerPostcode ?: "Unknown Postcode",  // Handle null postcode
//                    dateAdded = apiCustomer.dateAdded ?: "Unknown Date",  // Handle null date
//                    lat = apiCustomer.latitude ?: 0.0,  // Handle null latitude
//                    lon = apiCustomer.longitude ?: 0.0,  // Handle null longitude
//                    customerCityTown = apiCustomer.customerCityTown ?: "Unknown City/Town"
//                )
//            }
//
//            // Insert the customers into the database
//            try {
//                db.customerDao().insertCustomer(customerLocals)
//                Log.d("DEBUG", "Successfully inserted ${customerLocals.size} customers into the database")
//            } catch (e: Exception) {
//                Log.e("DEBUG", "Error during database insertion: ${e.message}")
//            }
//
//        } catch (e: Exception) {
//            e.printStackTrace()  // Handle API errors or database errors
//        }
//    }



