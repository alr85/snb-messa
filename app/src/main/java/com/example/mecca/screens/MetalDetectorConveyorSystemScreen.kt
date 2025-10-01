package com.example.mecca

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mecca.DAOs.MetalDetectorConveyorCalibrationDAO
import com.example.mecca.DataClasses.MdModelsLocal
import com.example.mecca.DataClasses.MetalDetectorConveyorCalibrationLocal
import com.example.mecca.DataClasses.MetalDetectorWithFullDetails
import com.example.mecca.Network.isNetworkAvailable
import com.example.mecca.Repositories.MetalDetectorModelsRepository
import com.example.mecca.Repositories.MetalDetectorSystemsRepository
import com.example.mecca.activities.MetalDetectorConveyorCalibrationActivity
import com.example.mecca.ui.theme.DetailItem
import com.example.mecca.ui.theme.ExpandableSection
import kotlinx.coroutines.launch
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetalDetectorConveyorSystemScreen(
    navController: NavHostController,
    repositoryMD: MetalDetectorSystemsRepository,
    dao: MetalDetectorConveyorCalibrationDAO,
    repositoryModels : MetalDetectorModelsRepository,
    systemId: Int,
) {
    var mdSystem by remember { mutableStateOf<MetalDetectorWithFullDetails?>(null) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isUploading by remember { mutableStateOf(false) }
    var modelDetails by remember { mutableStateOf<MdModelsLocal?>(null) }

    // Fetch data from the repository when screen is first loaded
    LaunchedEffect(systemId) {
        mdSystem = repositoryMD.getMetalDetectorsWithFullDetailsUsingLocalId(systemId).firstOrNull()
        modelDetails = repositoryModels.getMdModelDetails(mdSystem?.modelId ?: 0)
    }

    modelDetails?.let { Log.d("calibrationscreen", it.detectionSetting1) }

    // Determine if the system has a cloud ID, otherwise use the Temp ID
    val cloudSystemId = mdSystem?.cloudId
    val tempSystemId = mdSystem?.tempId
    cloudSystemId?.takeIf { it != 0 } ?: tempSystemId

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Top Row with Refresh Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mdSystem?.systemType ?: "?",
                        style = TextStyle(fontSize = 24.sp, textAlign = TextAlign.Center)
                    )

                    // Box to ensure proper alignment of Menu Button and Dropdown Menu
                    Box(
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Customer Systems Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Start a New Calibration",
                                            fontSize = 18.sp,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    isMenuExpanded = false

                                    // Generate a new UUID for the new calibration
                                    val newCalibrationId = UUID.randomUUID().toString()


                                    // Retrieve the engineerId from shared preferences
                                    val (_,_, engineerId) = PreferencesHelper.getCredentials(context)

                                    // Start the calibration activity with all necessary extras
                                    val intent = Intent(
                                        context,
                                        MetalDetectorConveyorCalibrationActivity::class.java
                                    ).apply {
                                        putExtra("CALIBRATION_ID", newCalibrationId)
                                        putExtra("SYSTEM_ID", mdSystem?.id)
                                        putExtra("CLOUD_SYSTEM_ID", mdSystem?.cloudId)
                                        putExtra("TEMP_SYSTEM_ID", mdSystem?.tempId)
                                        putExtra("CUSTOMER_ID", mdSystem?.fusionID)
                                        putExtra("SERIAL_NUMBER", mdSystem?.serialNumber)
                                        putExtra("MODEL_DESCRIPTION", mdSystem?.modelDescription)
                                        putExtra("CUSTOMER_NAME", mdSystem?.customerName)
                                        putExtra("MODEL_ID", mdSystem?.modelId)
                                        putExtra("ENGINEER_ID", engineerId ?: 0) // Default to 0 if null
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
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Start a New Service Call",
                                            fontSize = 18.sp,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    // Add service call logic here
                                }
                            )

                            if (mdSystem?.isSynced == false) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudUpload,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "Upload to Cloud",
                                                fontSize = 18.sp,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        isMenuExpanded = false
                                        mdSystem?.let { system ->
                                            coroutineScope.launch {
                                                if (isNetworkAvailable(context)) {
                                                    val serialExists =
                                                        repositoryMD.isSerialNumberExistsInCloud(
                                                            system.serialNumber
                                                        )
                                                    if (serialExists) {
                                                        snackbarHostState.showSnackbar("Serial number already exists!")
                                                    } else {
                                                        try {
                                                            isUploading = true

                                                            val systemTypeId =
                                                                system.systemTypeId ?: 0

                                                            val newCloudId =
                                                                repositoryMD.addMetalDetectorToCloud(
                                                                    customerID = system.customerId,
                                                                    serialNumber = system.serialNumber,
                                                                    apertureWidth = system.apertureWidth,
                                                                    apertureHeight = system.apertureHeight,
                                                                    systemTypeId = systemTypeId,
                                                                    modelId = system.modelId,
                                                                    lastLocation = system.lastLocation,
                                                                    calibrationInterval = 0
                                                                )

                                                            if (newCloudId != null && newCloudId != 0) {
                                                                snackbarHostState.showSnackbar("System added to cloud... Updating local calibrations...")

                                                                // now update any calibrations with the new cloud id
                                                                dao.updateCalibrationWithCloudId(
                                                                    system.tempId,
                                                                    newCloudId
                                                                )

                                                                snackbarHostState.showSnackbar("Updating local system database...")
                                                                repositoryMD.fetchAndStoreMdSystems()

                                                                navController.popBackStack()



                                                            } else {
                                                                snackbarHostState.showSnackbar("1. Failed to add system to cloud. Try again later.")
                                                            }

                                                        } catch (e: Exception) {
                                                            snackbarHostState.showSnackbar("2. Failed to add system to cloud. Try again later.")
                                                        } finally {
                                                            isUploading = false
                                                        }



                                                    }
                                                } else {
                                                    snackbarHostState.showSnackbar("No internet connection. Unable to upload.")
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                ExpandableSection(
                    title = "System Details",
                    initiallyExpanded = true
                ) {
                    DetailItem(label = "Serial Number", value = mdSystem?.serialNumber ?: "?")
                    DetailItem(label = "Customer", value = mdSystem?.customerName ?: "?")
                    DetailItem(label = "Model", value = mdSystem?.modelDescription ?: "?")
                    DetailItem(
                        label = "Aperture Width",
                        value = "${mdSystem?.apertureWidth ?: "?"} mm"
                    )
                    DetailItem(
                        label = "Aperture Height",
                        value = "${mdSystem?.apertureHeight ?: "?"} mm"
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                ExpandableSection(
                    title = "Database Info",
                    initiallyExpanded = false
                ) {
                    DetailItem(
                        label = "Cloud Synced",
                        value = if (mdSystem?.isSynced == true) "Yes" else "No"
                    )
                    DetailItem(label = "Local ID", value = mdSystem?.id.toString())
                    DetailItem(label = "Cloud ID", value = mdSystem?.cloudId.toString())
                    DetailItem(label = "Temp ID", value = (mdSystem?.tempId ?: 0).toString())
                    DetailItem(label = "Fusion Customer ID", value = mdSystem?.fusionID.toString())
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Remaining expandable sections for calibration information
            // Ensure proper closure and structure for each `item`
        }

        if (isUploading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun CalibrationItem(
    calibration: MetalDetectorConveyorCalibrationLocal,
    status: String,
    onClick: () -> Unit
) {
    val formattedDate = try {
        formatDate(calibration.startDate)
    } catch (e: Exception) {
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
            color = Color.Black
        )
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = "Started on: $formattedDate",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 1.dp,
            color = Color.LightGray
        )
    }
}