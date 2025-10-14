package com.example.mecca.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mecca.dataClasses.CustomerLocal

@Dao
interface CustomerDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customers: List<CustomerLocal>)

    @Query("SELECT * FROM customer")
    suspend fun getAllCustomers(): List<CustomerLocal>

    @Query("DELETE FROM customer")
    suspend fun deleteAllCustomers()  // Function to delete all customers

    @Query("SELECT name FROM customer WHERE fusionID = :fusionId")
    suspend fun getCustomerName(fusionId: Int): String? // Return null if no match found

}
