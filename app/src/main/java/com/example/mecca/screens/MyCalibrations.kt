package com.example.mecca.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.runtime.collectAsState
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
import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.DAOs.MetalDetectorConveyorCalibrationDAO
import com.example.mecca.dataClasses.MetalDetectorConveyorCalibrationLocal
import com.example.mecca.Network.isNetworkAvailable
import com.example.mecca.Repositories.CustomerRepository
import com.example.mecca.Repositories.MetalDetectorSystemsRepository
import com.example.mecca.activities.MetalDetectorConveyorCalibrationActivity
import com.example.mecca.formatDate
import com.example.mecca.ui.theme.ExpandableSection
import com.example.mecca.util.CsvUploader
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCalibrationsScreen(
    //navController: NavHostController,
    //db: AppDatabase,
    //repositoryMD: MetalDetectorSystemsRepository,
    dao: MetalDetectorConveyorCalibrationDAO, // DAO for fetching unfinished calibrations
    customerRepository: CustomerRepository,
    apiService: ApiService,
    navController: NavHostController,
    db: AppDatabase,
    repositoryMD: MetalDetectorSystemsRepository
) {

    var isMenuExpanded by remember { mutableStateOf(false) } // Tracks menu visibility
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }


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
                        text = "My Calibrations",
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


//                            DropdownMenuItem(
//                                text = {
//                                    Row(
//                                        verticalAlignment = Alignment.CenterVertically,
//                                        modifier = Modifier.padding(8.dp)
//                                    ) {
//                                        Icon(
//                                            imageVector = Icons.Default.Add,
//                                            contentDescription = null,
//                                            modifier = Modifier.size(20.dp)
//                                        )
//                                        Text(
//                                            text = "Start a New Service Call",
//                                            fontSize = 18.sp,
//                                            modifier = Modifier.padding(start = 8.dp)
//                                        )
//                                    }
//                                },
//                                onClick = {
//                                    isMenuExpanded = false
//
//                                }
//                            )
                        }
                    }
                }
            }


            item {
                ExpandableSection(
                    title = "Incomplete Calibrations",
                    initiallyExpanded = false
                ) {
                        Column {
                            val unfinishedCalibrations by dao.getAllUnfinishedCalibrations()
                                .collectAsState(initial = emptyList())
                            unfinishedCalibrations.forEach { calibration ->
                                MyCalibrationItem(
                                    calibration = calibration,
                                    status = "Incomplete",
                                    customerRepository = customerRepository,
                                    onClick = {
                                        val intent = Intent(
                                            context,
                                            MetalDetectorConveyorCalibrationActivity::class.java
                                        ).apply {
                                            putExtra("CALIBRATION_ID", calibration.calibrationId)
                                        }
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }

                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                ExpandableSection(
                    title = "Pending Calibrations for Upload",
                    initiallyExpanded = false
                ) {
                    Column {
                        val pendingCalibrations by dao.getAllPendingCalibrations()
                            .collectAsState(initial = emptyList())

                        pendingCalibrations.forEach { calibration ->
                            MyCalibrationItem(
                                calibration = calibration,
                                status = "Pending",
                                customerRepository = customerRepository,
                                onClick = {
                                    coroutineScope.launch {
                                        if (isNetworkAvailable(context)) {

                                            Log.d("MESSA-DEBUG", "Starting upload: ${calibration.calibrationId}")
                                            Log.d("MESSA-DEBUG", "Checking for a matching system: ${calibration.cloudSystemId}")



                                            // --- Ensure system exists in the cloud ---
                                            if (calibration.cloudSystemId == 0) {
                                                Log.d("MESSA-DEBUG", "No cloud ID found")
                                                snackbarHostState.showSnackbar("No matching system found. Please add this system to the cloud first")
                                                return@launch
                                            }

                                            Log.d("MESSA-DEBUG", "Building the CSV file")

                                            // --- Build CSV file path ---
                                            val csvFile = File(
                                                context.filesDir,
                                                "calibration_data_${calibration.calibrationId}.csv"
                                            )

                                            Log.d("MESSA-DEBUG", "Uploading the CSV file: $csvFile")

                                            val success = CsvUploader.uploadCsvFile(
                                                csvFile = csvFile,
                                                apiService = apiService,
                                                fileName = calibration.calibrationId
                                            )
                                            if (success) {
                                                Log.d("MESSA-DEBUG", "Upload of csvFile: $csvFile successful ")
                                                Log.d("MESSA-DEBUG", "Updating 'isSynced' flag to true ")
                                                dao.updateIsSynced(calibration.calibrationId, true)
                                                Log.d("MESSA-DEBUG", "Upload complete")
                                                snackbarHostState.showSnackbar("Upload successful!")
                                            } else {
                                                snackbarHostState.showSnackbar("Upload failed.")
                                            }

                                        } else {
                                            snackbarHostState.showSnackbar("No internet connection. Unable to upload.")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }


            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                ExpandableSection(
                    title = "Completed Calibrations",
                    initiallyExpanded = false
                ) {
                        Column {
                            val completedCalibrations by dao.getAllCompletedCalibrations()
                                .collectAsState(initial = emptyList())
                            completedCalibrations.forEach { calibration ->
                                MyCalibrationItem(
                                    calibration = calibration,
                                    status = "Completed",
                                    onClick = {},
                                    customerRepository = customerRepository,
                                )
                            }
                        }

                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}





@Composable
fun MyCalibrationItem(
    calibration: MetalDetectorConveyorCalibrationLocal,
    status: String,
    customerRepository: CustomerRepository,
    onClick: () -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    var customerName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val name = customerRepository.getCustomerName(fusionId = calibration.customerId)
            customerName = name
        }
    }

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
            text = "Customer Name: $customerName", // Display the customer name here
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = "Serial Number: ${calibration.serialNumber}", // Display the customer name here
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = "Status: $status",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = "Location: ${calibration.systemLocation}",
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


