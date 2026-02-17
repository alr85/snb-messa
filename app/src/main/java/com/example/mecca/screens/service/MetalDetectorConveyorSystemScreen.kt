package com.example.mecca.screens.service

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import com.example.mecca.AppChromeViewModel
import com.example.mecca.PreferencesHelper
import com.example.mecca.activities.MetalDetectorConveyorCalibrationActivity
import com.example.mecca.daos.MetalDetectorConveyorCalibrationDAO
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetalDetectorConveyorSystemScreen(
    navController: NavHostController,
    repositoryMD: MetalDetectorSystemsRepository,
    dao: MetalDetectorConveyorCalibrationDAO,
    repositoryModels: MetalDetectorModelsRepository,
    systemId: Int,
    chromeVm: AppChromeViewModel,
    snackbarHostState: SnackbarHostState

    ) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mdSystem by remember { mutableStateOf<MetalDetectorWithFullDetails?>(null) }
    var modelDetails by remember { mutableStateOf<MdModelsLocal?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    var showActions by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    suspend fun refresh() {
        mdSystem = repositoryMD
            .getMetalDetectorsWithFullDetailsUsingLocalId(systemId)
            .firstOrNull()

        modelDetails = mdSystem?.modelId
            ?.takeIf { it != 0 }
            ?.let { repositoryModels.getMdModelDetails(it) }
    }

    // Initial load
    LaunchedEffect(systemId) { refresh() }

    // Refresh when returning from calibration
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch { refresh() }
    }

    /**
     * MENU WIRING
     *
     * Root scaffold owns the icon.
     * This screen only supplies the click behaviour.
     */
    LaunchedEffect(Unit) {
        chromeVm.setMenuAction { showActions = true }
    }



    val formattedLastCalibrationDate =
        runCatching { formatDate(mdSystem?.lastCalibration) }.getOrElse { "Invalid date" }

    val formattedAddedDate =
        runCatching { formatDate(mdSystem?.addedDate) }.getOrElse { "Invalid date" }

    fun startCalibration() {
        val system = mdSystem ?: return

        val newCalibrationId =
            System.currentTimeMillis().toString(36) + "-" + (100..999).random()

        val (_, _, engineerId) = PreferencesHelper.getCredentials(context)

        val intent = Intent(context, MetalDetectorConveyorCalibrationActivity::class.java).apply {
            putExtra("CALIBRATION_ID", newCalibrationId)
            putExtra("SYSTEM_ID", system.id)
            putExtra("CLOUD_SYSTEM_ID", system.cloudId)
            putExtra("TEMP_SYSTEM_ID", system.tempId)
            putExtra("SYSTEM_TYPE_ID", system.systemTypeId)
            putExtra("CUSTOMER_ID", system.fusionID)
            putExtra("SERIAL_NUMBER", system.serialNumber)
            putExtra("MODEL_DESCRIPTION", system.modelDescription)
            putExtra("CUSTOMER_NAME", system.customerName)
            putExtra("MODEL_ID", system.modelId)
            putExtra("ENGINEER_ID", engineerId ?: 0)
            putExtra("SYSTEM_TYPE_DESCRIPTION", system.systemType)

            putExtra("LAST_LOCATION", system.lastLocation)

            // detection labels
            putExtra("DETECTION_SETTING_1_LABEL", modelDetails?.detectionSetting1)
            putExtra("DETECTION_SETTING_2_LABEL", modelDetails?.detectionSetting2)
            putExtra("DETECTION_SETTING_3_LABEL", modelDetails?.detectionSetting3)
            putExtra("DETECTION_SETTING_4_LABEL", modelDetails?.detectionSetting4)
            putExtra("DETECTION_SETTING_5_LABEL", modelDetails?.detectionSetting5)
            putExtra("DETECTION_SETTING_6_LABEL", modelDetails?.detectionSetting6)
            putExtra("DETECTION_SETTING_7_LABEL", modelDetails?.detectionSetting7)
            putExtra("DETECTION_SETTING_8_LABEL", modelDetails?.detectionSetting8)
        }

        context.startActivity(intent)
    }

    suspend fun syncThisSystem() {

        val system = mdSystem ?: return

        try {
            isUploading = true

            if (system.cloudId != 0) {

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

            when (val status =
                repositoryMD.checkSerialNumberStatus(context, system.serialNumber)) {

                SerialCheckResult.Exists ->
                    snackbarHostState.showSnackbar("Serial already exists in cloud.")

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

                        dao.updateCalibrationWithCloudId(system.tempId, newCloudId)

                        repositoryMD.fetchAndStoreMdSystems()

                        snackbarHostState.showSnackbar("System added to cloud.")
                        navController.popBackStack()
                    } else {
                        snackbarHostState.showSnackbar("Failed to add system.")
                    }
                }

                SerialCheckResult.ExistsLocalOffline ->
                    snackbarHostState.showSnackbar("Offline: serial exists locally.")

                SerialCheckResult.NotFoundLocalOffline ->
                    snackbarHostState.showSnackbar("No network. Sync later.")

                is SerialCheckResult.Error ->
                    snackbarHostState.showSnackbar("Error: ${status.message}")
            }

        } catch (e: Exception) {

            InAppLogger.e("Sync crashed: ${e.message}")
            snackbarHostState.showSnackbar("Sync failed.")

        } finally {
            isUploading = false
        }
    }

    // ---------- Bottom Sheet ----------
    if (showActions) {
        val sheetBg = Color.White

        ModalBottomSheet(
            onDismissRequest = { showActions = false },
            sheetState = sheetState,
            containerColor = sheetBg
        ) {

            ListItem(
                headlineContent = { Text("Start Calibration", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Begin a new calibration") },
                leadingContent = { Icon(Icons.Default.Tune, null) },
                colors = ListItemDefaults.colors(containerColor = sheetBg),
                modifier = Modifier.clickable {
                    showActions = false
                    startCalibration()
                }
            )

            ListItem(
                headlineContent = { Text("Start Service Call", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Coming soon") },
                leadingContent = { Icon(Icons.Default.Handyman, null) },
                colors = ListItemDefaults.colors(containerColor = sheetBg),
                modifier = Modifier.clickable {
                    showActions = false

                    scope.launch {
                        snackbarHostState.showSnackbar("Service flow not wired yet.")
                    }
                }
            )

            if (mdSystem?.isSynced == false) {

                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Sync to Cloud", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Upload this system") },
                    leadingContent = { Icon(Icons.Default.CloudUpload, null) },
                    colors = ListItemDefaults.colors(containerColor = sheetBg),
                    modifier = Modifier.clickable {
                        showActions = false
                        scope.launch { syncThisSystem() }
                    }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ---------- CONTENT ----------
    Box(Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {

            item {
                ExpandableSection("System Details", true) {

                    DetailItem("System Type", mdSystem?.systemType ?: "?")
                    DetailItem("Serial Number", mdSystem?.serialNumber ?: "?")
                    DetailItem("Customer", mdSystem?.customerName ?: "?")
                    DetailItem("Model", mdSystem?.modelDescription ?: "?")
                    DetailItem("Aperture Width", "${mdSystem?.apertureWidth ?: "?"} mm")
                    DetailItem("Aperture Height", "${mdSystem?.apertureHeight ?: "?"} mm")
                    DetailItem("Location", mdSystem?.lastLocation ?: "?")
                    DetailItem("Last Calibrated", formattedLastCalibrationDate)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                ExpandableSection("Database Info", false) {

                    DetailItem("Cloud Synced", if (mdSystem?.isSynced == true) "Yes" else "No")
                    DetailItem("Local ID", mdSystem?.id?.toString() ?: "?")
                    DetailItem("Cloud ID", mdSystem?.cloudId?.toString() ?: "?")
                    DetailItem("Temp ID", (mdSystem?.tempId ?: 0).toString())
                    DetailItem("Fusion Customer ID", mdSystem?.fusionID?.toString() ?: "?")
                    DetailItem("Date added", formattedAddedDate)
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }

        // Upload overlay
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
