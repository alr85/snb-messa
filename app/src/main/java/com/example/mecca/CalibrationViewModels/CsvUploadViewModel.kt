package com.example.mecca.CalibrationViewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mecca.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CsvUploadViewModel(private val apiService: ApiService) : ViewModel() {

    private val _isUploading = MutableLiveData(false)
    val isUploading: LiveData<Boolean> get() = _isUploading


    suspend fun uploadCsvFile(context: Context, csvFile: File, fileName: String): Boolean {
        _isUploading.postValue(true)

        return withContext(Dispatchers.IO) {
            try {
                // Create the request body and multipart part from the CSV file
                val requestFile = csvFile.asRequestBody("text/csv".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData("file", fileName, requestFile)


                // Log upload attempt
                Log.d("CSV", "Attempting to upload file: $fileName")

                // Execute the upload call
                val response = apiService.uploadMdCalibrationCSV(multipartBody).execute()


                if (response.isSuccessful) {
                    Log.d("CSV", "File uploaded successfully!")
                    true
                } else {
                    Log.e("CSV", "Upload failed: ${response.errorBody()?.string()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("CSV", "Error uploading file: ${e.message}")
                false
            } finally {
                _isUploading.postValue(false)
            }
        }
    }
}


