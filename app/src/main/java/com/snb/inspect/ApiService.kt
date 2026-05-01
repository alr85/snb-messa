package com.snb.inspect

import com.snb.inspect.dataClasses.ApiMeasuringEquipment
import com.snb.inspect.dataClasses.CloudUser
import com.snb.inspect.dataClasses.ConveyorRetailerSensitivitiesEntity
import com.snb.inspect.dataClasses.Customer
import com.snb.inspect.dataClasses.FreefallThroatRetailerSensitivitiesEntity
import com.snb.inspect.dataClasses.MdModel
import com.snb.inspect.dataClasses.MdSystem
import com.snb.inspect.dataClasses.MdSystemCloud
import com.snb.inspect.dataClasses.NoticeCloud
import com.snb.inspect.dataClasses.PipelineRetailerSensitivitiesEntity
import com.snb.inspect.dataClasses.SystemType
import com.snb.inspect.dataClasses.UserManual
import com.snb.inspect.dataClasses.WeekendRotaResponse
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
    suspend fun postMdSystem(@Body newMdSystem: MdSystemCloud): Response<MdSystem>

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

    @GET("https://snb-mea-web-api20240909215557.azurewebsites.net/api/WeekendRota")
    suspend fun getWeekendRota(): Response<List<WeekendRotaResponse>>

    @GET("UserManuals")
    suspend fun getUserManuals(): Response<List<UserManual>>

    @GET("MeasuringEquipment") // Adjust the endpoint path to match your API
    suspend fun getMeasuringEquipment(): Response<List<ApiMeasuringEquipment>>
}
