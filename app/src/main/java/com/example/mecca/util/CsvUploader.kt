package com.example.mecca.util

import com.example.mecca.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object CsvUploader {

    /**
     * Upload a CSV file to the API.
     *
     * @param csvFile   The file on disk to upload.
     * @param apiService Retrofit service instance.
     * @param fileName  Logical name to send to server (will be forced to end with .csv).
     * @return true if upload succeeded (2xx), else false.
     */
    suspend fun uploadCsvFile(
        csvFile: File,
        apiService: ApiService,
        fileName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!csvFile.exists() || !csvFile.isFile) {
                InAppLogger.e("CSV upload aborted — file not found: ${csvFile.absolutePath}")
                return@withContext false
            }

            // Ensure the server sees a .csv filename
            val safeName = if (fileName.endsWith(".csv", true)) fileName else "$fileName.csv"

            // Form-data part name MUST match the server model property: CsvUploadRequest.File  =>  "File"
            val body = csvFile.asRequestBody("text/csv".toMediaType())
            val part = MultipartBody.Part.createFormData("File", safeName, body)

            InAppLogger.d(
                "Preparing upload: $safeName (${csvFile.length()} bytes) from ${csvFile.absolutePath}"
            )

            val response = apiService.uploadMdCalibrationCSV(part).execute()

            if (response.isSuccessful) {
                InAppLogger.d("CSV upload successful (HTTP ${response.code()})")
                true
            } else {
                val err = response.errorBody()?.string()
                InAppLogger.e(
                    "CSV upload failed: HTTP ${response.code()} message=${response.message()} body=$err"
                )
                false
            }
        } catch (e: Exception) {
            InAppLogger.e("Exception during CSV upload: ${e.message}")
            false
        }
    }
}
