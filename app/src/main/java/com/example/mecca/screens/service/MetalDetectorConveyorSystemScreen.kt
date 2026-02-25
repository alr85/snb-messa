package com.example.mecca.screens.service

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.draw.rotate
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
import com.example.mecca.ui.theme.MyAppTheme
import com.example.mecca.ui.theme.SnbDarkGrey
import com.example.mecca.ui.theme.SnbRed
import com.example.mecca.util.InAppLogger
import com.example.mecca.util.SerialCheckResult
import kotlinx.coroutines.launch

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
        chromeVm.setMenuAction { showActions = !showActions }
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
            putExtra("SYSTEM_FULL_DETAILS", system)
            putExtra("ENGINEER_ID", engineerId ?: 0)
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

            item { Spacer(Modifier.height(100.dp)) }
        }

        // Animated Expressive FAB replacement
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            val rotation by animateFloatAsState(
                targetValue = if (showActions) 45f else 0f,
                label = "rotation"
            )

            Column(
                modifier = Modifier.width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Secondary action items that appear above the FAB
                AnimatedVisibility(
                    visible = showActions,
                    enter = fadeIn() + expandIn(expandFrom = Alignment.BottomEnd),
                    exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.BottomEnd)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Start Calibration
                        FloatingActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                showActions = false
                                startCalibration()
                            },
                            containerColor = SnbRed,
                            contentColor = Color.White,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Start Calibration",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.Tune, null, modifier = Modifier.size(24.dp))
                            }
                        }

                        // Start Service Call
                        FloatingActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                showActions = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Service flow not wired yet.")
                                }
                            },
                            containerColor = SnbRed,
                            contentColor = Color.White,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Start Service Call",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.Handyman, null, modifier = Modifier.size(24.dp))
                            }
                        }

                        // Sync to Cloud
                        if (mdSystem?.isSynced == false) {
                            FloatingActionButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    showActions = false
                                    scope.launch { syncThisSystem() }
                                },
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sync to Cloud",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }

                // Main FAB
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            if (showActions) "Close" else "Actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.rotate(rotation).size(28.dp)
                        )
                    },
                    onClick = { showActions = !showActions },
                    expanded = !showActions,

                    containerColor = if (showActions) SnbDarkGrey else SnbRed,
                    contentColor = if (showActions) Color.White else Color.White
                )
            }
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
