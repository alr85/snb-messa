package com.snb.inspect.screens.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
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
import com.snb.inspect.AppChromeViewModel
import com.snb.inspect.FetchResult
import com.snb.inspect.R
import com.snb.inspect.dataClasses.MetalDetectorWithFullDetails
import com.snb.inspect.repositories.MetalDetectorSystemsRepository
import com.snb.inspect.ui.theme.SnbDarkGrey
import com.snb.inspect.ui.theme.SnbRed
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

    // Screen state - REACTIVE OBSERVATION
    val systems by repository.observeMetalDetectorsByCustomerId(customerID).collectAsState(initial = emptyList())
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    // Floating action menu state
    var showMenu by rememberSaveable { mutableStateOf(false) }

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
            val filteredSystems = remember(systems, searchQuery) {
                systems
                    .filter { it.serialNumber.contains(searchQuery, ignoreCase = true) }
                    .sortedBy { it.serialNumber }
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

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search Serial Number...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                                        val success = syncMetalDetectors(context, repository)
                                        if (success) {
                                            Toast.makeText(context, "Database refreshed successfully.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Refresh failed. Using local data.", Toast.LENGTH_SHORT).show()
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
    val syncColor by animateColorAsState(
        targetValue = if (mdSystem.isSynced) Color.Green else Color.Red,
        label = "syncStatusColor"
    )

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
                tint = syncColor,
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
            text = title + " coming soon",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Sync unsynced systems up to the cloud, then pull down the latest systems.
 */
private suspend fun syncMetalDetectors(
    context: Context,
    repository: MetalDetectorSystemsRepository
): Boolean {
    Log.d("SyncMetalDetectors", "Starting sync of metal detectors")

    // Use uploadUnsyncedSystems from repository which is now "bulletproof"
    repository.uploadUnsyncedSystems(context)
    
    // Pull latest cloud data down
    val fetchResult = repository.fetchAndStoreMdSystems()

    return fetchResult is FetchResult.Success
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
            "❌ Unable to open Google Maps. Please check if it's installed.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
