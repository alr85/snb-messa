package com.example.mecca.screens.menu

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.ApiService
import com.example.mecca.AppChromeViewModel
import com.example.mecca.AppDatabase
import com.example.mecca.daos.MetalDetectorConveyorCalibrationDAO
import com.example.mecca.network.isNetworkAvailable
import com.example.mecca.TopBarState
import com.example.mecca.repositories.CustomerRepository
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.activities.MetalDetectorConveyorCalibrationActivity
import com.example.mecca.dataClasses.MetalDetectorConveyorCalibrationLocal
import com.example.mecca.formatDate
import com.example.mecca.ui.theme.ExpandableSection
import com.example.mecca.util.CsvUploader
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCalibrationsScreen(
    dao: MetalDetectorConveyorCalibrationDAO,
    customerRepository: CustomerRepository,
    apiService: ApiService,
    chromeVm: AppChromeViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect once. Don’t collect flows inside expandable content blocks.
    val unfinishedCalibrations by dao.getAllUnfinishedCalibrations()
        .collectAsState(initial = emptyList())

    val pendingCalibrations by dao.getAllPendingCalibrations()
        .collectAsState(initial = emptyList())

    val completedCalibrations by dao.getAllCompletedCalibrations()
        .collectAsState(initial = emptyList())

    // Cache customer names so we don’t launch N coroutines per list item.
    var customerNameCache by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    LaunchedEffect(
        unfinishedCalibrations,
        pendingCalibrations,
        completedCalibrations
    ) {
        val allCustomerIds = (unfinishedCalibrations + pendingCalibrations + completedCalibrations)
            .map { it.customerId }
            .distinct()

        // Only fetch missing names
        val missingIds = allCustomerIds.filterNot { customerNameCache.containsKey(it) }
        if (missingIds.isEmpty()) return@LaunchedEffect

        val newEntries = missingIds.associateWith { id ->
            customerRepository.getCustomerName(fusionId = id) ?: "Unknown"
        }

        customerNameCache = customerNameCache + newEntries
    }

    LaunchedEffect(Unit) {
        chromeVm.setTopBar(
            TopBarState(
                title = "My Calibrations",
                showBack = true,
                showCall = false,
                showMenu = false,
            )
        )
    }



    Scaffold(

//        topBar = {
//            MyTopAppBar(
//                navController = navController,
//                title = "My Calibrations",
//                showBack = true,
//                showCall = false
//            )
//        },


        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },

    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {


            item {
                ExpandableSection(title = "Incomplete Calibrations", initiallyExpanded = false) {
                    CalibrationList(
                        calibrations = unfinishedCalibrations,
                        status = "Incomplete",
                        customerNameCache = customerNameCache,
                        onItemClick = { calibration ->
                            val intent = Intent(context, MetalDetectorConveyorCalibrationActivity::class.java).apply {
                                putExtra("CALIBRATION_ID", calibration.calibrationId)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                ExpandableSection(title = "Pending Calibrations for Upload", initiallyExpanded = false) {
                    CalibrationList(
                        calibrations = pendingCalibrations,
                        status = "Pending",
                        customerNameCache = customerNameCache,
                        onItemClick = { calibration ->
                            coroutineScope.launch {
                                if (!isNetworkAvailable(context)) {
                                    snackbarHostState.showSnackbar("No internet connection. Unable to upload.")
                                    return@launch
                                }

                                if (calibration.cloudSystemId == 0) {
                                    snackbarHostState.showSnackbar("No matching system found. Add this system to the cloud first.")
                                    return@launch
                                }

                                val csvFile = File(
                                    context.filesDir,
                                    "calibration_data_${calibration.calibrationId}.csv"
                                )

                                val success = CsvUploader.uploadCsvFile(
                                    csvFile = csvFile,
                                    apiService = apiService,
                                    fileName = calibration.calibrationId
                                )

                                if (success) {
                                    dao.updateIsSynced(calibration.calibrationId, true)
                                    snackbarHostState.showSnackbar("Upload successful!")
                                } else {
                                    snackbarHostState.showSnackbar("Upload failed.")
                                }
                            }
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                ExpandableSection(title = "Completed Calibrations", initiallyExpanded = false) {
                    CalibrationList(
                        calibrations = completedCalibrations,
                        status = "Completed",
                        customerNameCache = customerNameCache,
                        onItemClick = { /* no-op for now */ }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun CalibrationList(
    calibrations: List<MetalDetectorConveyorCalibrationLocal>,
    status: String,
    customerNameCache: Map<Int, String>,
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
            MyCalibrationItem(
                calibration = calibration,
                status = status,
                customerName = customerNameCache[calibration.customerId] ?: "Loading…",
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
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            text = "Calibration ID: ${calibration.calibrationId}",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
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
