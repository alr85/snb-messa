package com.example.mecca

import com.example.mecca.DAOs.UserResponse
import com.example.mecca.DataClasses.CloudUser
import com.example.mecca.DataClasses.MdModel
import com.example.mecca.DataClasses.MdSystem
import com.example.mecca.DataClasses.MdSystemCloud
import com.example.mecca.DataClasses.MdSystemCloudResponse
import com.example.mecca.DataClasses.MdSystemLocal
import com.example.mecca.DataClasses.SystemType
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path


interface ApiService {
    @GET("Users") // Endpoint to fetch users
    suspend fun getUsers(): List<CloudUser>

    @GET("Customers")
    suspend fun getCustomers(): Response<List<Customer>> // Use suspend function for coroutines

    @GET("MdModels")
    suspend fun getMdModels(): Response<List<MdModel>>  // Use suspend function for coroutines

    @GET("MdSystems")
    suspend fun getMdSystems(): Response<List<MdSystem>>

    @GET("SystemTypes")
    suspend fun getSystemTypes(): Response<List<SystemType>>

    @POST("MdSystems")
    suspend fun postMdSystem(@Body newMdSystem: MdSystemLocal): Response<MdSystemLocal>

    @GET("MdSystems/checkSerialNumberExists/{serialNumber}")
    suspend fun checkSerialNumberExists(
        @Path("serialNumber") serialNumber: String
    ): Boolean

    @Multipart
    @POST("MdCalibrationCsvUpload/uploadMdCalibrationCSV") // Adjust path if needed
    fun uploadMdCalibrationCSV(
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>

}

