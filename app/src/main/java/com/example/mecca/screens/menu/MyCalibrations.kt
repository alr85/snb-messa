package com.example.mecca.screens.menu

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mecca.ApiService
import com.example.mecca.PreferencesHelper
import com.example.mecca.daos.MetalDetectorConveyorCalibrationDAO
import com.example.mecca.network.isNetworkAvailable
import com.example.mecca.repositories.CustomerRepository
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.activities.MetalDetectorConveyorCalibrationActivity
import com.example.mecca.dataClasses.MetalDetectorConveyorCalibrationLocal
import com.example.mecca.formatDate
import com.example.mecca.ui.theme.ExpandableSection
import com.example.mecca.util.CsvUploader
import com.example.mecca.util.InAppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCalibrationsScreen(
    dao: MetalDetectorConveyorCalibrationDAO,
    customerRepository: CustomerRepository,
    systemsRepository: MetalDetectorSystemsRepository,
    apiService: ApiService,
    snackbarHostState: SnackbarHostState
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val unfinishedCalibrations by dao.getAllUnfinishedCalibrations()
        .collectAsState(initial = emptyList())

    val pendingCalibrations by dao.getAllPendingCalibrations()
        .collectAsState(initial = emptyList())

    val completedCalibrations by dao.getAllCompletedCalibrations()
        .collectAsState(initial = emptyList())

    // Safeguard for duplicate uploads
    var uploadingCalibrationIds by remember { mutableStateOf(setOf<String>()) }

    // ---------------- Customer Cache ----------------

    var customerNameCache by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    LaunchedEffect(
        unfinishedCalibrations.size,
        pendingCalibrations.size,
        completedCalibrations.size
    ) {

        val allCustomerIds = (unfinishedCalibrations + pendingCalibrations + completedCalibrations)
            .map { it.customerId }
            .distinct()

        val missingIds = allCustomerIds.filterNot { customerNameCache.containsKey(it) }
        if (missingIds.isEmpty()) return@LaunchedEffect

        val newEntries = missingIds.associateWith { id ->
            customerRepository.getCustomerName(fusionId = id) ?: "Unknown"
        }

        customerNameCache = customerNameCache + newEntries
    }



    // ---------------- UI ----------------

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {

        item {
            ExpandableSection("Incomplete Calibrations",true) {
                CalibrationList(
                    calibrations = unfinishedCalibrations,
                    status = "Incomplete",
                    customerNameCache = customerNameCache,
                    onItemClick = { calibration ->

                        coroutineScope.launch {
                            val system = systemsRepository
                                .getMetalDetectorsWithFullDetailsUsingLocalId(calibration.systemId)
                                .firstOrNull()

                            if (system == null) {
                                InAppLogger.e("Could not find system details for calibration: ${calibration.calibrationId}")
                                snackbarHostState.showSnackbar("Error: System details missing.")
                                return@launch
                            }

                            val (_, _, engineerId) = PreferencesHelper.getCredentials(context)

                            val intent = Intent(
                                context,
                                MetalDetectorConveyorCalibrationActivity::class.java
                            ).apply {
                                putExtra("CALIBRATION_ID", calibration.calibrationId)
                                putExtra("SYSTEM_FULL_DETAILS", system)
                                putExtra("ENGINEER_ID", engineerId ?: 0)
                            }

                            context.startActivity(intent)
                        }
                    }
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            ExpandableSection("Pending Calibrations for Upload") {

                CalibrationList(
                    calibrations = pendingCalibrations,
                    status = "Pending",
                    customerNameCache = customerNameCache,
                    uploadingCalibrationIds = uploadingCalibrationIds,
                    onItemClick = { calibration ->

                        if (uploadingCalibrationIds.contains(calibration.calibrationId)) return@CalibrationList

                        coroutineScope.launch {
                            uploadingCalibrationIds = uploadingCalibrationIds + calibration.calibrationId
                            
                            try {
                                val success = withContext(Dispatchers.IO) {
                                    if (!isNetworkAvailable(context)) {
                                        return@withContext null // Signal no network
                                    }

                                    if (calibration.cloudSystemId == 0) {
                                        return@withContext false // Signal missing system
                                    }

                                    val csvFile = File(
                                        context.filesDir,
                                        "calibration_data_${calibration.calibrationId}.csv"
                                    )

                                    CsvUploader.uploadCsvFile(
                                        csvFile = csvFile,
                                        apiService = apiService,
                                        fileName = calibration.calibrationId
                                    )
                                }

                                when (success) {
                                    null -> snackbarHostState.showSnackbar("No internet connection.")
                                    false -> snackbarHostState.showSnackbar("No matching cloud system.")
                                    true -> {
                                        dao.updateIsSynced(calibration.calibrationId, true)
                                        snackbarHostState.showSnackbar("Upload successful!")
                                    }
                                    else -> snackbarHostState.showSnackbar("Upload failed.")
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("An error occurred during upload.")
                            } finally {
                                uploadingCalibrationIds = uploadingCalibrationIds - calibration.calibrationId
                            }
                        }
                    }
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            ExpandableSection("Completed Calibrations") {
                CalibrationList(
                    calibrations = completedCalibrations,
                    status = "Completed",
                    customerNameCache = customerNameCache,
                    onItemClick = {}
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}


@Composable
private fun CalibrationList(
    calibrations: List<MetalDetectorConveyorCalibrationLocal>,
    status: String,
    customerNameCache: Map<Int, String>,
    uploadingCalibrationIds: Set<String> = emptySet(),
    onItemClick: (MetalDetectorConveyorCalibrationLocal) -> Unit
) {
    Column {
        if (calibrations.isEmpty()) {
            Text(
                text = "None",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            return
        }

        calibrations.forEach { calibration ->
            val isUploading = uploadingCalibrationIds.contains(calibration.calibrationId)
            MyCalibrationItem(
                calibration = calibration,
                status = status,
                customerName = customerNameCache[calibration.customerId] ?: "Loading…",
                isUploading = isUploading,
                onClick = { onItemClick(calibration) }
            )
        }
    }
}

@Composable
fun MyCalibrationItem(
    calibration: MetalDetectorConveyorCalibrationLocal,
    status: String,
    customerName: String,
    isUploading: Boolean = false,
    onClick: () -> Unit
) {
    val formattedDate = try {
        formatDate(calibration.startDate)
    } catch (_: Exception) {
        "Invalid date"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isUploading) { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calibration ID: ${calibration.calibrationId}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text(
            text = "Customer: $customerName",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Location: ${calibration.lastLocation}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Serial Number: ${calibration.serialNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Started: $formattedDate",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Status: $status",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )



        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
