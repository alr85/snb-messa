package com.snb.inspect.repositories

import android.content.Context
import com.snb.inspect.ApiService
import com.snb.inspect.FetchResult
import com.snb.inspect.daos.SensitivityOptimisationValidationDAO
import com.snb.inspect.daos.MetalDetectorSystemsDAO
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

class SensitivityOptimisationValidationRepository(
    private val sovDao: SensitivityOptimisationValidationDAO,
    private val mdSystemsDAO: MetalDetectorSystemsDAO
) {

    private val uploadMutex = Mutex()

    suspend fun insertOrUpdate(sov: SensitivityOptimisationValidationLocal) {
        sovDao.insertOrUpdate(sov)
    }

    suspend fun getById(id: String) = sovDao.getById(id)

    suspend fun updateSystemLocation(systemId: Int, lastLocation: String) {
        mdSystemsDAO.updateLastLocation(systemId, lastLocation)
    }

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
                    fileName = "SOV_${sov.sovId}",
                    isValidation = true
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
                sov.sovId,
                sov.mapVersion,
                sov.systemId,
                sov.tempSystemId,
                sov.cloudSystemId,
                sov.systemTypeId,
                sov.modelId,
                sov.engineerId,
                sov.customerId,
                sov.startDate,
                sov.endDate,
                sov.newLocation,

                sov.systemComments,
                sov.beltSpeed,

                sov.productDescription,
                sov.productLibraryReference,
                sov.productLength,
                sov.productWidth,
                sov.productHeight,
                sov.productWeight,
                sov.productComments,

                sov.sensitivityAsLeftFerrous,
                sov.sampleCertAsLeftFerrous,
                sov.val1LeadingSuccesses,
                sov.val1MiddleSuccesses,
                sov.val1TrailingSuccesses,
                sov.minSignalAsLeftFerrousLeading,
                sov.minSignalAsLeftFerrousMiddle,
                sov.minSignalAsLeftFerrousTrailing,

                sov.sensitivityAsLeftNonFerrous,
                sov.sampleCertAsLeftNonFerrous,
                sov.val2LeadingSuccesses,
                sov.val2MiddleSuccesses,
                sov.val2TrailingSuccesses,
                sov.minSignalAsLeftNonFerrousLeading,
                sov.minSignalAsLeftNonFerrousMiddle,
                sov.minSignalAsLeftNonFerrousTrailing,

                sov.sensitivityAsLeftStainless,
                sov.sampleCertAsLeftStainless,
                sov.val3LeadingSuccesses,
                sov.val3MiddleSuccesses,
                sov.val3TrailingSuccesses,
                sov.minSignalAsLeftStainlessLeading,
                sov.minSignalAsLeftStainlessMiddle,
                sov.minSignalAsLeftStainlessTrailing,

                sov.notesAsLeftFerrous,
                sov.notesAsLeftNonFerrous,
                sov.notesAsLeftStainless,

                sov.packValidationPassed,

                sov.detectionSetting1label,
                sov.detectionSettingAsLeft1,
                sov.detectionSetting2label,
                sov.detectionSettingAsLeft2,
                sov.detectionSetting3label,
                sov.detectionSettingAsLeft3,
                sov.detectionSetting4label,
                sov.detectionSettingAsLeft4,
                sov.detectionSetting5label,
                sov.detectionSettingAsLeft5,
                sov.detectionSetting6label,
                sov.detectionSettingAsLeft6,
                sov.detectionSetting7label,
                sov.detectionSettingAsLeft7,
                sov.detectionSetting8label,
                sov.detectionSettingAsLeft8,

                sov.notesAsLeftDetectionSettings


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
