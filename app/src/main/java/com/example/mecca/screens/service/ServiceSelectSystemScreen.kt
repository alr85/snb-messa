package com.example.mecca.screens.service

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.example.mecca.AppChromeViewModel
import com.example.mecca.FetchResult
import com.example.mecca.R
import com.example.mecca.dataClasses.MetalDetectorWithFullDetails
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceSelectSystemScreen(
    navController: NavHostController,
    repository: MetalDetectorSystemsRepository,
    customerID: Int,
    customerName: String,
    customerPostcode: String,
    snackbarHostState: SnackbarHostState,
    chromeVm: AppChromeViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Screen state
    var systems by remember { mutableStateOf<List<MetalDetectorWithFullDetails>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Bottom sheet menu state
    var showMenu by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Load machines when customer changes
    LaunchedEffect(customerID) {
        systems = getMetalDetectors(repository, customerID)
    }

    /**
     * MENU BUTTON WIRING
     *
     * Your top bar lives in the root scaffold (MyApp).
     * The route controls whether the menu icon is visible.
     * This screen ONLY provides the click action (open bottom sheet).
     *
     * DisposableEffect ensures we clean up when leaving the screen,
     * so menu actions don’t leak into other screens.
     */
    DisposableEffect(Unit) {
        chromeVm.setMenuAction { showMenu = true }
        onDispose {
            chromeVm.setMenuAction(null)
        }
    }

    val scrollState = rememberScrollState()

    // ========= UI =========
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {

        // ---- Bottom sheet menu ----
        if (showMenu) {
            val sheetBg = Color.White

            ModalBottomSheet(
                onDismissRequest = { showMenu = false },
                sheetState = sheetState,
                containerColor = sheetBg
            ) {
                ListItem(
                    headlineContent = { Text("New Metal Detector") },
                    leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                    supportingContent = { Text("Add a new metal detector for this customer") },
                    colors = ListItemDefaults.colors(containerColor = sheetBg),
                    modifier = Modifier.clickable {
                        showMenu = false

                        // ⚠️ Customer names can contain spaces / symbols, so encode them.
                        val encodedName = Uri.encode(customerName)

                        navController.navigate("AddNewMetalDetectorScreen/$customerID/$encodedName")
                    }
                )

                ListItem(
                    headlineContent = { Text("New Checkweigher") },
                    leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                    supportingContent = { Text("Coming soon") },
                    colors = ListItemDefaults.colors(containerColor = sheetBg)
                )

                ListItem(
                    headlineContent = { Text("New Static Scale") },
                    leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                    supportingContent = { Text("Coming soon") },
                    colors = ListItemDefaults.colors(containerColor = sheetBg)
                )

                ListItem(
                    headlineContent = { Text("New X-Ray") },
                    leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                    supportingContent = { Text("Coming soon") },
                    colors = ListItemDefaults.colors(containerColor = sheetBg)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                ListItem(
                    headlineContent = { Text("Navigate to Site") },
                    leadingContent = { Icon(Icons.Default.Navigation, contentDescription = null) },
                    supportingContent = { Text(customerPostcode) },
                    colors = ListItemDefaults.colors(containerColor = sheetBg),
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
                    leadingContent = { Icon(Icons.Default.CloudSync, contentDescription = null) },
                    supportingContent = { Text("Sync local and cloud systems") },
                    colors = ListItemDefaults.colors(containerColor = sheetBg),
                    modifier = Modifier.clickable {
                        showMenu = false
                        if (isRefreshing) return@clickable

                        scope.launch {
                            isRefreshing = true
                            try {
                                val ok = syncMetalDetectors(repository)
                                if (ok) {
                                    systems = getMetalDetectors(repository, customerID)
                                } else {
                                    snackbarHostState.showSnackbar("Sync failed. Please try again.")
                                }
                            } finally {
                                isRefreshing = false
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // ---- Main content ----
        val filteredSystems = remember(systems) {
            systems.sortedBy { it.serialNumber }
        }

        Text(
            text = "Metal Detectors",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            items(
                items = filteredSystems,
                key = { it.id }
            ) { mdSystem ->
                SystemCard(
                    mdSystem = mdSystem,
                    onClick = {
                        navController.navigate("MetalDetectorConveyorSystemScreen/${mdSystem.id}")
                    }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        ComingSoonRow("Checkweighers")
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        ComingSoonRow("X-Ray Systems")
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        ComingSoonRow("Static Scales")
    }
}

@Composable
private fun SystemCard(
    mdSystem: MetalDetectorWithFullDetails,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
            .border(BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(8.dp))
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

@Composable
private fun ComingSoonRow(title: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Coming soon",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Sync unsynced systems up to the cloud, then pull down the latest systems.
 * Returns true if the overall operation succeeded.
 *
 * NOTE: This function does network + DB work, so call it from a coroutine.
 */
private suspend fun syncMetalDetectors(
    repository: MetalDetectorSystemsRepository
): Boolean {
    Log.d("SyncMetalDetectors", "Starting sync of metal detectors")

    val unsyncedSystems = repository.getMetalDetectorUsingCloudId(null)
        .filter { !it.isSynced }

    Log.d("SyncMetalDetectors", "Unsynced Metal Detectors: ${unsyncedSystems.size}")

    var overallSuccess = true

    // Upload unsynced systems
    for (mdSystem in unsyncedSystems) {
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
            when (repository.updateSyncStatus(mdSystem.tempId, true, newCloudId)) {
                is FetchResult.Success -> Log.d("SyncMetalDetectors", "Local update OK: ${mdSystem.serialNumber}")
                is FetchResult.Failure -> {
                    Log.e("SyncMetalDetectors", "Local update FAILED: ${mdSystem.serialNumber}")
                    overallSuccess = false
                }
            }
        } else {
            Log.e("SyncMetalDetectors", "Upload FAILED: ${mdSystem.serialNumber}")
            overallSuccess = false
        }
    }

    // Pull latest cloud data down
    if (overallSuccess) {
        when (val result = repository.fetchAndStoreMdSystems()) {
            is FetchResult.Success -> Log.d("SyncMetalDetectors", "Full sync OK: ${result.message}")
            is FetchResult.Failure -> {
                Log.e("SyncMetalDetectors", "Full sync FAILED: ${result.errorMessage}")
                overallSuccess = false
            }
        }
    }

    return overallSuccess
}

private suspend fun getMetalDetectors(
    repository: MetalDetectorSystemsRepository,
    customerID: Int
): List<MetalDetectorWithFullDetails> {
    return repository.getMetalDetectorUsingCloudId(null)
        .filter { it.customerId == customerID }
}
