package com.example.mecca.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.example.mecca.AppDatabase
import com.example.mecca.FetchResult
import com.example.mecca.R
import com.example.mecca.dataClasses.MetalDetectorWithFullDetails
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.util.InAppLogger
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceSelectSystemScreen(
    navController: NavHostController,
    db: AppDatabase, // <- unused currently (warning). Remove if you don’t need it.
    repository: MetalDetectorSystemsRepository,
    customerID: Int,
    customerName: String,
    customerPostcode: String
) {
    val context = LocalContext.current

    val metalDetectorsCalibrationsList =
        remember { mutableStateOf<List<MetalDetectorWithFullDetails>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Bottom sheet menu state
    var showMenu by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Load machines when screen loads
    LaunchedEffect(customerID) {
        metalDetectorsCalibrationsList.value = getMetalDetectors(repository, customerID)
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "$customerName – $customerPostcode",
                        maxLines = 1
                    )
                },
                actions = {


                    FilledTonalButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Tune, contentDescription = null)
                        Text("Actions", modifier = Modifier.padding(start = 6.dp))
                    }

                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->


        // Modern menu bottom sheet
        if (showMenu) {
            ModalBottomSheet(
                onDismissRequest = { showMenu = false },
                sheetState = sheetState
            ) {
                // Menu items
                ListItem(
                    headlineContent = { Text("New Metal Detector") },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    },
                    supportingContent = { Text("Add a new metal detector for this customer") },
                    modifier = Modifier.clickable {
                        showMenu = false
                        navController.navigate("AddNewMetalDetectorScreen/$customerID/$customerName")
                    }
                )

                ListItem(
                    headlineContent = { Text("New Checkweigher") },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    },
                    supportingContent = { Text("Coming soon") },
                    // Disabled look + no click
                )

                ListItem(
                    headlineContent = { Text("New Static Scale") },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    },
                    supportingContent = { Text("Coming soon") },
                )

                ListItem(
                    headlineContent = { Text("New X-Ray") },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    },
                    supportingContent = { Text("Coming soon") },
                )

                Divider()

                ListItem(
                    headlineContent = { Text("Navigate to Site") },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = null)
                    },
                    supportingContent = { Text(customerPostcode) },
                    modifier = Modifier.clickable {
                        showMenu = false
                        try {
                            val gmmIntentUri =
                                "google.navigation:q=${Uri.encode(customerPostcode)}".toUri()
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            Log.e("NavigationIntent", "Failed to launch navigation", e)
                            Toast.makeText(context, "Unable to open Google Maps.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                ListItem(
                    headlineContent = { Text("Refresh Database") },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null)
                    },
                    supportingContent = { Text("Sync local and cloud systems") },
                    modifier = Modifier.clickable {
                        showMenu = false
                        coroutineScope.launch {
                            val ok = syncMetalDetectors(repository)
                            if (ok) {
                                metalDetectorsCalibrationsList.value =
                                    getMetalDetectors(repository, customerID)
                            } else {
                                snackbarHostState.showSnackbar("Sync failed. Please try again.")
                            }
                        }
                    }
                )

                // Bottom padding so it doesn’t feel cramped
                Box(modifier = Modifier.height(24.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {


                val filteredSystems = metalDetectorsCalibrationsList.value.sortedBy { it.serialNumber }
                Log.d("DEBUG", "UNFiltered Systems: ${metalDetectorsCalibrationsList.value}")
                Log.d("DEBUG", "Filtered Systems: $filteredSystems")

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    items(filteredSystems) { mdSystem ->
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    // Navigate directly, no selectedSystem trap
                                    navController.navigate("MetalDetectorConveyorSystemScreen/${mdSystem.id}")
                                }
                                .border(
                                    BorderStroke(1.dp, Color.Gray),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .width(180.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = mdSystem.serialNumber,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.CenterStart),
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp
                                )

                                Icon(
                                    imageVector = if (mdSystem.isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = if (mdSystem.isSynced) "Synced to cloud" else "Not synced to cloud",
                                    tint = if (mdSystem.isSynced) Color.Green else Color.Red,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(top = 8.dp, end = 8.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = when (mdSystem.systemTypeId) {
                                        1 -> painterResource(id = R.drawable.belt_straight)
                                        2 -> painterResource(id = R.drawable.pipe_straight)
                                        3 -> painterResource(id = R.drawable.drop_straight)
                                        4 -> painterResource(id = R.drawable.pharma)
                                        else -> painterResource(id = R.drawable.belt_straight)
                                    },
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Text(
                                text = mdSystem.modelDescription,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )

                            Text(
                                text = mdSystem.lastLocation,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun syncMetalDetectors(
    repository: MetalDetectorSystemsRepository
): Boolean {
    Log.d("SyncMetalDetectors", "Starting sync of metal detectors")

    // Get a list of unsynced systems
    val unsyncedSystems = repository.getMetalDetectorUsingCloudId(null)
        .filter { !it.isSynced }
    val unsyncedCount = unsyncedSystems.count()
    Log.d("SyncMetalDetectors", "Unsynced Metal Detectors: $unsyncedCount")

    var overallSuccess = true

    // Upload unsynced systems
    if (unsyncedCount > 0) {
        unsyncedSystems.forEach { mdSystem ->
            val newCloudId = repository.addMetalDetectorToCloud(
                customerID = mdSystem.customerId,
                serialNumber = mdSystem.serialNumber,
                apertureWidth = mdSystem.apertureWidth,
                apertureHeight = mdSystem.apertureHeight,
                systemTypeId = mdSystem.systemTypeId,
                modelId = mdSystem.modelId,
                lastLocation = mdSystem.lastLocation,
                calibrationInterval = mdSystem.calibrationInterval
            )

            if (newCloudId != null) {
                Log.d("SyncMetalDetectors", "Upload successful for system: ${mdSystem.serialNumber}, Cloud ID: $newCloudId")

                // Attempt to update the sync status in the local database
                when (repository.updateSyncStatus(mdSystem.tempId, true, newCloudId)) {
                    is FetchResult.Success -> {
                        Log.d("SyncMetalDetectors", "Local update successful for system: ${mdSystem.serialNumber}")
                    }
                    is FetchResult.Failure -> {
                        Log.e("SyncMetalDetectors", "Failed to update local sync status for system: ${mdSystem.serialNumber}")
                        overallSuccess = false // Mark overall success as false if updating fails
                    }
                }
            } else {
                Log.e("SyncMetalDetectors", "Failed to upload system: ${mdSystem.serialNumber}")
                overallSuccess = false // Mark overall success as false if uploading fails
            }
        }
    }

    // Sync local database with the cloud (only if all unsynced uploads were successful)
    if (overallSuccess) {
        when (val result = repository.fetchAndStoreMdSystems()) {
            is FetchResult.Success -> {
                Log.d("SyncMetalDetectors", "Full database sync successful: ${result.message}")
            }
            is FetchResult.Failure -> {
                Log.e("SyncMetalDetectors", "Full database sync failed: ${result.errorMessage}")
                overallSuccess = false // Mark overall success as false if sync fails
            }
        }
    }

    if (overallSuccess) {
        Log.d("SyncMetalDetectors", "Sync operation completed successfully")
    } else {
        Log.e("SyncMetalDetectors", "Sync operation completed with errors")
    }

    return overallSuccess
}

suspend fun getMetalDetectors(
    repository: MetalDetectorSystemsRepository,
    customerID: Int
): List<MetalDetectorWithFullDetails> {
    // Update the local list after successful sync
    return repository.getMetalDetectorUsingCloudId(null)
        .filter { it.customerId == customerID }
}