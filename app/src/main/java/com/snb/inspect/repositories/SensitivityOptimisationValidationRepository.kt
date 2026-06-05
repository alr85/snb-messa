package com.snb.inspect.repositories

import android.content.Context
import com.snb.inspect.ApiService
import com.snb.inspect.FetchResult
import com.snb.inspect.daos.SensitivityOptimisationValidationDAO
import com.snb.inspect.dataClasses.SensitivityOptimisationValidationLocal
import com.snb.inspect.network.isNetworkAvailable
import com.snb.inspect.util.CsvUploader
import com.snb.inspect.util.InAppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.text.Normalizer

class SensitivityOptimisationValidationRepository(private val sovDao: SensitivityOptimisationValidationDAO) {

    private val uploadMutex = Mutex()

    suspend fun insertOrUpdate(sov: SensitivityOptimisationValidationLocal) {
        sovDao.insertOrUpdate(sov)
    }

    suspend fun getById(id: String) = sovDao.getById(id)

    suspend fun uploadUnsynced(
        context: Context,
        apiService: ApiService,
        specificId: String? = null
    ): FetchResult = uploadMutex.withLock {
        if (!isNetworkAvailable(context)) {
            return@withLock FetchResult.Failure("Offline. Upload skipped.")
        }

        val pending = if (specificId != null) {
            val sov = sovDao.getById(specificId)
            if (sov != null && !sov.isSynced && sov.endDate.isNotBlank()) {
                listOf(sov)
            } else {
                emptyList()
            }
        } else {
            sovDao.getAllPending().first()
        }

        if (pending.isEmpty()) {
            return@withLock FetchResult.Success("No pending SOVs.")
        }

        var uploaded = 0
        val failed = mutableListOf<String>()

        for (sov in pending) {
            try {
                val csvFile = createCsvFile(context, sov.sovId)
                if (csvFile == null || !csvFile.exists()) {
                    failed += "${sov.sovId} (CSV failed)"
                    continue
                }

                val success = CsvUploader.uploadCsvFile(
                    csvFile = csvFile,
                    apiService = apiService,
                    fileName = "SOV_${sov.sovId}"
                )

                if (success) {
                    sovDao.updateIsSynced(sov.sovId, true)
                    uploaded++
                } else {
                    failed += sov.sovId
                }
            } catch (e: Exception) {
                failed += "${sov.sovId} (${e.message})"
            }
        }

        if (failed.isEmpty()) FetchResult.Success("Uploaded $uploaded SOV(s).")
        else FetchResult.Failure("Uploaded $uploaded. Failed: ${failed.joinToString()}")
    }

    private fun normalize(input: Any?): String {
        if (input == null) return ""
        var text = input.toString().replace(";", ",").replace("\n", " ").replace("\r", "")
        text = Normalizer.normalize(text, Normalizer.Form.NFD)
        return text.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "").trim()
    }

    suspend fun createCsvFile(context: Context, sovId: String): File? = withContext(Dispatchers.IO) {
        val sov = sovDao.getById(sovId) ?: return@withContext null
        val fileName = "sov_data_$sovId.csv"
        val csvFile = File(context.filesDir, fileName)

        try {
            val data = listOf(
                sov.sovId, sov.mapVersion, sov.systemId, sov.cloudSystemId, sov.serialNumber, sov.lastLocation,
                sov.engineerId, sov.customerId, sov.startDate, sov.endDate,
                sov.productDescription, sov.productLibraryReference, sov.productLibraryNumber, sov.beltSpeed,
                sov.sensitivityAsFoundFerrous, sov.sensitivityAsFoundNonFerrous, sov.sensitivityAsFoundStainless,
                sov.sensitivityAsFoundOther1, sov.sensitivityAsFoundOther2,
                sov.detectionSettingAsFound1, sov.detectionSettingAsFound2, sov.detectionSettingAsFound3, sov.detectionSettingAsFound4,
                sov.detectionSettingAsFound5, sov.detectionSettingAsFound6, sov.detectionSettingAsFound7, sov.detectionSettingAsFound8,
                sov.validationTest1Description, sov.validationTest1Passes, sov.validationTest1Successes,
                sov.validationTest2Description, sov.validationTest2Passes, sov.validationTest2Successes,
                sov.validationTest3Description, sov.validationTest3Passes, sov.validationTest3Successes,
                sov.sensitivityAsLeftFerrous, sov.sensitivityAsLeftNonFerrous, sov.sensitivityAsLeftStainless,
                sov.sensitivityAsLeftOther1, sov.sensitivityAsLeftOther2,
                sov.detectionSettingAsLeft1, sov.detectionSettingAsLeft2, sov.detectionSettingAsLeft3, sov.detectionSettingAsLeft4,
                sov.detectionSettingAsLeft5, sov.detectionSettingAsLeft6, sov.detectionSettingAsLeft7, sov.detectionSettingAsLeft8,
                sov.systemComments, sov.productComments,
                sov.customerName,
                sov.detectionSetting1label, sov.detectionSetting2label, sov.detectionSetting3label, sov.detectionSetting4label,
                sov.detectionSetting5label, sov.detectionSetting6label, sov.detectionSetting7label, sov.detectionSetting8label
            ).map { normalize(it) }

            csvFile.outputStream().use { out ->
                out.bufferedWriter(Charsets.UTF_8).use { writer ->
                    writer.write(data.joinToString(";"))
                    writer.write("\r\n")
                }
            }
            csvFile
        } catch (e: Exception) {
            null
        }
    }
}
