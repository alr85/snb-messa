package com.snb.inspect.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.snb.inspect.dataClasses.CustomerLocal
import kotlinx.coroutines.flow.Flow


@Dao
interface CustomerDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customers: List<CustomerLocal>)

    @Query("SELECT * FROM customer ORDER BY name")
    fun observeCustomers(): Flow<List<CustomerLocal>>

    @Query("SELECT * FROM customer ORDER BY name")
    suspend fun getAllCustomers(): List<CustomerLocal>

    @Query("DELETE FROM customer")
    suspend fun deleteAllCustomers()

    @Query("SELECT name FROM customer WHERE fusionID = :fusionId")
    suspend fun getCustomerName(fusionId: Int): String?
}
