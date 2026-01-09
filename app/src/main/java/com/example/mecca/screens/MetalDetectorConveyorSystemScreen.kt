package com.example.mecca.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import com.example.mecca.DAOs.MetalDetectorConveyorCalibrationDAO
import com.example.mecca.PreferencesHelper
import com.example.mecca.activities.MetalDetectorConveyorCalibrationActivity
import com.example.mecca.dataClasses.MdModelsLocal
import com.example.mecca.dataClasses.MetalDetectorWithFullDetails
import com.example.mecca.formatDate
import com.example.mecca.repositories.MetalDetectorModelsRepository
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.ui.theme.DetailItem
import com.example.mecca.ui.theme.ExpandableSection
import com.example.mecca.util.InAppLogger
import com.example.mecca.util.SerialCheckResult
import kotlinx.coroutines.launch
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetalDetectorConveyorSystemScreen(
    navController: NavHostController,
    repositoryMD: MetalDetectorSystemsRepository,
    dao: MetalDetectorConveyorCalibrationDAO,
    repositoryModels: MetalDetectorModelsRepository,
    systemId: Int,
) {
    var mdSystem by remember { mutableStateOf<MetalDetectorWithFullDetails?>(null) }
    var modelDetails by remember { mutableStateOf<MdModelsLocal?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isUploading by remember { mutableStateOf(false) }

    // Bottom sheet menu state
    var showActions by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    suspend fun refresh() {
        mdSystem = repositoryMD.getMetalDetectorsWithFullDetailsUsingLocalId(systemId).firstOrNull()
        val modelId = mdSystem?.modelId
        modelDetails = if (modelId != null && modelId != 0) {
            repositoryModels.getMdModelDetails(modelId)
        } else null
    }

    LaunchedEffect(systemId) { refresh() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        coroutineScope.launch {
            Log.d("MESSA-DEBUG", "Refreshing system details after resume")
            refresh()
        }
    }

    val formattedLastCalibrationDate = try { formatDate(mdSystem?.lastCalibration) } catch (_: Exception) { "Invalid date" }
    val formattedAddedDate = try { formatDate(mdSystem?.addedDate) } catch (_: Exception) { "Invalid date" }

    fun startCalibration() {
        val system = mdSystem ?: return

        // Generate a new calibration ID (your method is fine)
        val newCalibrationId = System.currentTimeMillis().toString(36) + "-" + (100..999).random()

        val (_, _, engineerId) = PreferencesHelper.getCredentials(context)

        val intent = Intent(context, MetalDetectorConveyorCalibrationActivity::class.java).apply {
            putExtra("CALIBRATION_ID", newCalibrationId)
            putExtra("SYSTEM_ID", system.id)
            putExtra("CLOUD_SYSTEM_ID", system.cloudId)
            putExtra("TEMP_SYSTEM_ID", system.tempId)
            putExtra("CUSTOMER_ID", system.fusionID)
            putExtra("SERIAL_NUMBER", system.serialNumber)
            putExtra("MODEL_DESCRIPTION", system.modelDescription)
            putExtra("CUSTOMER_NAME", system.customerName)
            putExtra("MODEL_ID", system.modelId)
            putExtra("ENGINEER_ID", engineerId ?: 0)

            putExtra("DETECTION_SETTING_1_LABEL", modelDetails?.detectionSetting1)
            putExtra("DETECTION_SETTING_2_LABEL", modelDetails?.detectionSetting2)
            putExtra("DETECTION_SETTING_3_LABEL", modelDetails?.detectionSetting3)
            putExtra("DETECTION_SETTING_4_LABEL", modelDetails?.detectionSetting4)
            putExtra("DETECTION_SETTING_5_LABEL", modelDetails?.detectionSetting5)
            putExtra("DETECTION_SETTING_6_LABEL", modelDetails?.detectionSetting6)
            putExtra("DETECTION_SETTING_7_LABEL", modelDetails?.detectionSetting7)
            putExtra("DETECTION_SETTING_8_LABEL", modelDetails?.detectionSetting8)

            putExtra("LAST_LOCATION", system.lastLocation)
        }
        context.startActivity(intent)
    }

    fun startServiceCall() {
        // TODO: hook this up later
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Service call flow not wired yet.")
        }
    }

    suspend fun syncThisSystem() {
        val system = mdSystem ?: return

        InAppLogger.d("Attempting to sync this machine to the cloud...")

        try {
            isUploading = true

            if (system.cloudId != 0) {
                // Has cloudId -> update
                repositoryMD.updateSystem(
                    context = context,
                    cloudId = system.cloudId,
                    localId = system.id,
                    tempId = system.tempId
                )
                snackbarHostState.showSnackbar("System updated successfully.")
                refresh()
                return
            }

            // No cloudId -> check serial
            when (val status = repositoryMD.checkSerialNumberStatus(context, system.serialNumber)) {
                SerialCheckResult.Exists -> {
                    snackbarHostState.showSnackbar("Serial already exists in cloud. Link it or change the serial.")
                }

                SerialCheckResult.NotFound -> {
                    val newCloudId = repositoryMD.addMetalDetectorToCloud(
                        customerID = system.customerId,
                        serialNumber = system.serialNumber,
                        apertureWidth = system.apertureWidth,
                        apertureHeight = system.apertureHeight,
                        systemTypeId = system.systemTypeId,
                        modelId = system.modelId,
                        lastLocation = system.lastLocation,
                        calibrationInterval = 0
                    )

                    if (newCloudId != null && newCloudId != 0) {
                        snackbarHostState.showSnackbar("System added to cloud. Updating local records...")

                        // Update any calibrations referencing tempId
                        dao.updateCalibrationWithCloudId(system.tempId, newCloudId)

                        // Refresh cache
                        repositoryMD.fetchAndStoreMdSystems()
                        refresh()
                        navController.popBackStack()
                    } else {
                        snackbarHostState.showSnackbar("Failed to add system to cloud. Try again later.")
                    }
                }

                SerialCheckResult.ExistsLocalOffline -> {
                    snackbarHostState.showSnackbar("Offline: serial exists locally. Connect to the internet to sync.")
                }

                SerialCheckResult.NotFoundLocalOffline -> {
                    snackbarHostState.showSnackbar("No network. System must be synced when online.")
                }

                is SerialCheckResult.Error -> {
                    snackbarHostState.showSnackbar("Error checking serial: ${status.message ?: "network error"}")
                }
            }
        } catch (e: Exception) {
            InAppLogger.e("Sync flow crashed: ${e.message}")
            snackbarHostState.showSnackbar("Sync failed. Try again later.")
        } finally {
            isUploading = false
        }
    }

    // Bottom sheet content
    if (showActions) {
        ModalBottomSheet(
            onDismissRequest = { showActions = false },
            sheetState = sheetState
        ) {
            ListItem(
                headlineContent = { Text("Start a New Calibration") },
                supportingContent = { Text("Begin a new calibration for this system") },
                leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                modifier = Modifier.clickable {
                    showActions = false
                    startCalibration()
                }
            )

            ListItem(
                headlineContent = { Text("Start a New Service Call") },
                supportingContent = { Text("Log a service call for this system") },
                leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                modifier = Modifier.clickable {
                    showActions = false
                    startServiceCall()
                }
            )

            if (mdSystem?.isSynced == false) {
                Divider()
                ListItem(
                    headlineContent = { Text("Sync to Cloud") },
                    supportingContent = { Text("Upload this system and link local records") },
                    leadingContent = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showActions = false
                        coroutineScope.launch { syncThisSystem() }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(mdSystem?.systemType ?: "System") },
                actions = {
                    FilledTonalButton(onClick = { showActions = true }) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Actions", modifier = Modifier.padding(start = 8.dp))
                        Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                ExpandableSection(title = "System Details", initiallyExpanded = true) {
                    DetailItem(label = "Serial Number", value = mdSystem?.serialNumber ?: "?")
                    DetailItem(label = "Customer", value = mdSystem?.customerName ?: "?")
                    DetailItem(label = "Model", value = mdSystem?.modelDescription ?: "?")
                    DetailItem(label = "Aperture Width", value = "${mdSystem?.apertureWidth ?: "?"} mm")
                    DetailItem(label = "Aperture Height", value = "${mdSystem?.apertureHeight ?: "?"} mm")
                    DetailItem(label = "Location", value = mdSystem?.lastLocation ?: "?")
                    DetailItem(label = "Last Calibrated", value = formattedLastCalibrationDate)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                ExpandableSection(title = "Database Info", initiallyExpanded = false) {
                    DetailItem(label = "Cloud Synced", value = if (mdSystem?.isSynced == true) "Yes" else "No")
                    DetailItem(label = "Local ID", value = mdSystem?.id?.toString() ?: "?")
                    DetailItem(label = "Cloud ID", value = mdSystem?.cloudId?.toString() ?: "?")
                    DetailItem(label = "Temp ID", value = (mdSystem?.tempId ?: 0).toString())
                    DetailItem(label = "Fusion Customer ID", value = mdSystem?.fusionID?.toString() ?: "?")
                    DetailItem(label = "Date added", value = formattedAddedDate)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Add other sections here...
        }

        // More intentional loading overlay
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
