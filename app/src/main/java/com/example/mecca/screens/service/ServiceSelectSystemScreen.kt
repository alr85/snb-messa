package com.example.mecca.screens.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.example.mecca.AppChromeViewModel
import com.example.mecca.FetchResult
import com.example.mecca.R
import com.example.mecca.dataClasses.MetalDetectorWithFullDetails
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.ui.theme.SnbDarkGrey
import com.example.mecca.ui.theme.SnbRed
import kotlinx.coroutines.launch

@Composable
fun ServiceSelectSystemScreen(
    navController: NavHostController,
    repository: MetalDetectorSystemsRepository,
    customerID: Int,
    customerName: String,
    customerPostcode: String,
    customerAddress: String,
    snackbarHostState: SnackbarHostState,
    chromeVm: AppChromeViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Screen state
    var systems by remember { mutableStateOf<List<MetalDetectorWithFullDetails>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Floating action menu state
    var showMenu by rememberSaveable { mutableStateOf(false) }

    // Load machines when customer changes
    LaunchedEffect(customerID) {
        systems = getMetalDetectors(repository, customerID)
    }

    LaunchedEffect(Unit) {
        chromeVm.setMenuAction { showMenu = !showMenu }
    }

    val scrollState = rememberScrollState()

    // ========= UI =========
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {

            // ---- Main content ----
            val filteredSystems = remember(systems) {
                systems.sortedBy { it.serialNumber }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text(
                            text = customerName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "$customerAddress, $customerPostcode",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            navigateToPostcode(context, customerPostcode)
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Navigate to site",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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

            // Space at the bottom for the FAB
            Spacer(modifier = Modifier.height(100.dp))
        }

        // Animated Expressive FAB replacement
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            val rotation by animateFloatAsState(
                targetValue = if (showMenu) 45f else 0f,
                label = "rotation"
            )

            Column(
                modifier = Modifier.width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Secondary action items that appear above the FAB
                AnimatedVisibility(
                    visible = showMenu,
                    enter = fadeIn() + expandIn(expandFrom = Alignment.BottomEnd),
                    exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.BottomEnd)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // New Metal Detector
                        FloatingActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                showMenu = false
                                val encodedName = Uri.encode(customerName)
                                navController.navigate("AddNewMetalDetectorScreen/$customerID/$encodedName")
                            },
                            containerColor = SnbRed,
                            contentColor = Color.White,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "New Metal Detector",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(24.dp))
                            }
                        }

                        // Navigate to Site
                        FloatingActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                showMenu = false
                                navigateToPostcode(context, customerPostcode)
                            },
                            containerColor = SnbRed,
                            contentColor = Color.White,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Navigate to Site",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.Navigation, null, modifier = Modifier.size(24.dp))
                            }
                        }

                        // Refresh Database
                        FloatingActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                showMenu = false
                                if (isRefreshing) return@FloatingActionButton
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
                            },
                            containerColor = SnbRed,
                            contentColor = Color.White,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Refresh Database",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.CloudSync, null, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }

                // Main FAB
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            if (showMenu) "Close" else "Actions",
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
                    onClick = { showMenu = !showMenu },
                    expanded = !showMenu,
                    containerColor = if (showMenu) SnbDarkGrey else SnbRed,
                    contentColor = if (showMenu) Color.White else Color.White
                )
            }
        }
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = mdSystem.serialNumber,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = if (mdSystem.isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
                contentDescription = if (mdSystem.isSynced) "Synced to cloud" else "Not synced to cloud",
                tint = if (mdSystem.isSynced) Color.Green else Color.Red,
                modifier = Modifier.size(20.dp)
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
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = mdSystem.lastLocation,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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

fun navigateToPostcode(context: Context, postcode: String) {
    try {
        val gmmIntentUri =
            "google.navigation:q=${Uri.encode(postcode)}".toUri()

        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        context.startActivity(mapIntent)

    } catch (e: Exception) {
        Log.e("NavigationIntent", "Failed to launch navigation", e)
        Toast.makeText(
            context,
            "Unable to open Google Maps.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
