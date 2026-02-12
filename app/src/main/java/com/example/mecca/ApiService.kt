package com.example.mecca

import com.example.mecca.dataClasses.CloudUser
import com.example.mecca.dataClasses.ConveyorRetailerSensitivitiesEntity
import com.example.mecca.dataClasses.Customer
import com.example.mecca.dataClasses.FreefallThroatRetailerSensitivitiesEntity
import com.example.mecca.dataClasses.MdModel
import com.example.mecca.dataClasses.MdSystem
import com.example.mecca.dataClasses.MdSystemCloud
import com.example.mecca.dataClasses.MdSystemLocal
import com.example.mecca.dataClasses.NoticeCloud
import com.example.mecca.dataClasses.NoticeLocal
import com.example.mecca.dataClasses.PipelineRetailerSensitivitiesEntity
import com.example.mecca.dataClasses.SystemType
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path


interface ApiService {
    @GET("Users") // Endpoint to fetch users
    suspend fun getUsers(): Response<List<CloudUser>>

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

    @PUT("MdSystems/{id}")
    suspend fun updateMdSystem(
        @Path("id") id: Int,
        @Body systemData: MdSystemCloud
    ): Response<Unit>

    @GET("detectionlevels/conveyor")
    suspend fun getConveyorLevels(): List<ConveyorRetailerSensitivitiesEntity>

    @GET("detectionlevels/freefall")
    suspend fun getFreefallLevels(): List<FreefallThroatRetailerSensitivitiesEntity>

    @GET("detectionlevels/pipeline")
    suspend fun getPipelineLevels(): List<PipelineRetailerSensitivitiesEntity>

    @GET("notices")
    suspend fun getNotices(): Response<List<NoticeCloud>>



}

