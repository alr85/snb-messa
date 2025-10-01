import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mecca.AppDatabase
import com.example.mecca.DataClasses.MetalDetectorWithFullDetails
import com.example.mecca.FetchResult
import com.example.mecca.R
import com.example.mecca.Repositories.MetalDetectorSystemsRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceSelectSystemScreen(
    navController: NavHostController,
    db: AppDatabase,
    repository: MetalDetectorSystemsRepository,
    customerID: Int,
    customerName: String,
    customerPostcode: String
) {
    var selectedSystem by remember { mutableStateOf<MetalDetectorWithFullDetails?>(null) }
    val metalDetectorsCalibrationsList = remember { mutableStateOf<List<MetalDetectorWithFullDetails>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    var isMenuExpanded by remember { mutableStateOf(false) } // Tracks menu visibility

    val snackbarHostState = remember { SnackbarHostState() }

    // Load the relevant machines from the Room database when the screen loads
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val machines = getMetalDetectors(repository, customerID)
            metalDetectorsCalibrationsList.value = machines
        }
    }

    LaunchedEffect(selectedSystem) {
        selectedSystem?.let {
            navController.navigate("MetalDetectorConveyorSystemScreen/${it.id}")
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Top Row with Refresh Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Systems at $customerName - $customerPostcode",
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
                                        modifier = Modifier.padding(8.dp) // Adjust padding as needed
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp) // Adjust icon size
                                        )
                                        Text(
                                            text = "New Metal Detector",
                                            fontSize = 18.sp,
                                            modifier = Modifier.padding(start = 8.dp) // Space between icon and text
                                        )
                                    }
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    navController.navigate("AddNewMetalDetectorScreen/${customerID}/${customerName}")
                                },
                                enabled = true
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp) // Adjust padding as needed
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add, // Example icon
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp) // Adjust icon size
                                        )
                                        Text(
                                            text = "New Checkweigher",
                                            fontSize = 18.sp, // Customize text size
                                            modifier = Modifier.padding(start = 8.dp) // Space between icon and text
                                        )
                                    }
                                },
                                onClick = {
                                    isMenuExpanded = false
                                },
                                enabled = false
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp) // Adjust padding as needed
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add, // Example icon
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp) // Adjust icon size
                                        )
                                        Text(
                                            text = "New Static Scale",
                                            fontSize = 18.sp, // Customize text size
                                            modifier = Modifier.padding(start = 8.dp) // Space between icon and text
                                        )
                                    }
                                },
                                onClick = {
                                    isMenuExpanded = false
                                },
                                enabled = false
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp) // Adjust padding as needed
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add, // Example icon
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp) // Adjust icon size
                                        )
                                        Text(
                                            text = "New X-Ray",
                                            fontSize = 18.sp, // Customize text size
                                            modifier = Modifier.padding(start = 8.dp) // Space between icon and text
                                        )
                                    }
                                },
                                onClick = {
                                    isMenuExpanded = false
                                },
                                enabled = false
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudSync,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Refresh Database",
                                            fontSize = 18.sp,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    coroutineScope.launch {
                                        if (syncMetalDetectors(repository)) {
                                            metalDetectorsCalibrationsList.value = getMetalDetectors(repository, customerID)
                                        } else {
                                            // Show a snackbar when the sync fails
                                            snackbarHostState.showSnackbar("Sync failed. Please try again.")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // Filtered and sorted systems list
                val filteredSystems = metalDetectorsCalibrationsList.value.sortedBy { it.serialNumber }
                Log.d("DEBUG", "UNFiltered Systems: $metalDetectorsCalibrationsList")
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
                                .clickable { selectedSystem = mdSystem }
                                .border(
                                    BorderStroke(1.dp, Color.Gray),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .width(180.dp)
                        ) {
                            // Top row containing the system serial number and the sync status circle
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = mdSystem.serialNumber,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.CenterStart),
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp
                                )

                                // Sync status icon in the top right corner
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

                            // Image section
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter =
                                    when(mdSystem.systemTypeId){
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

                            // Model description
                            Text(
                                text = mdSystem.modelDescription,
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