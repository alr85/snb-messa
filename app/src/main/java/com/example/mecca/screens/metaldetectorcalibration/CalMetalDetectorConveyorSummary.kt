package com.example.mecca.screens.metaldetectorcalibration

import com.example.mecca.CalibrationBanner
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.ApiService
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
import kotlinx.coroutines.launch

//private suspend fun handleFinish(
//    viewModel: CalibrationMetalDetectorConveyorViewModel,
//    apiService: ApiService,
//    context: android.content.Context,
//    onResult: (String) -> Unit
//) {
//    if (viewModel.cloudSystemId.value != 0 && viewModel.cloudSystemId.value != null) {
//        viewModel.updateCalibrationEnd()
//
//        val csvSuccess = viewModel.createAndUploadCsv(
//            context,
//            viewModel.calibrationId.value,
//            apiService
//        )
//        if (csvSuccess) {
//            onResult("Calibration completed and uploaded to the cloud.")
//        } else {
//            onResult("Calibration was completed, but NOT uploaded to the cloud. Please try again later.")
//        }
//    } else {
//        onResult("Calibration was NOT completed. Please ensure the system is synced with the cloud.")
//    }
//}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSummary(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel(),
    apiService: ApiService
) {

    // Stops the next button from being pressed until the screen is rendered
    LaunchedEffect(Unit) {
        viewModel.finishNavigation()
    }


    val progress = viewModel.progress
    val activity = LocalActivity.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isUploading by viewModel.isUploading.collectAsState()

    var showLocationChangeDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Calibration Banner
        item {
            CalibrationBanner(
                progress = progress,
                viewModel = viewModel
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Navigation Buttons
        item {
            CalibrationNavigationButtons(
                onPreviousClick = { viewModel.updateComplianceConfirmation() },
                onCancelClick = {
                    viewModel.clearCalibrationData()
                    navController.navigate("serviceHome") {
                        popUpTo("serviceHome") { inclusive = true }
                    }
                },
                onNextClick = {  },
                isNextEnabled = false,
                isFirstStep = false,
                navController = navController,
                viewModel = viewModel,
                onSaveAndExitClick = { /* Custom save logic here */ }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }


        // Summary Details
        item {
            CalMetalDetectorConveyorSummaryDetails(viewModel = viewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Uploading Indicator
        if (isUploading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Uploading CSV... Please wait", style = MaterialTheme.typography.bodyMedium)
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        // Finish Button
        item {
            OutlinedButton(
                onClick = {
                    val oldLocation = viewModel.lastLocation.value
                    val newLocation = viewModel.systemLocation.value

                    if (oldLocation != newLocation) {
                        showLocationChangeDialog = true
                    } else {
                        coroutineScope.launch {
                            viewModel.finaliseCalibrationAndUpload(context, apiService) { message ->
                                dialogMessage = message
                                showResultDialog = true
                            }
                        }
                    }
                }
,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Finish"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Finish")

            }
            // --- Location change confirmation dialog ---
            if (showLocationChangeDialog) {
                AlertDialog(
                    onDismissRequest = { showLocationChangeDialog = false },
                    title = { Text("Confirm Location Change") },
                    text = {
                        Text(
                            "The system location has changed from " +
                                    "'${viewModel.lastLocation.value}' to '${viewModel.systemLocation.value}'. " +
                                    "Do you want to update this in the database and cloud?"
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showLocationChangeDialog = false
                            coroutineScope.launch {
                                // Step 1: Update Room with new location
                                Log.d("MESSA-DEBUG","User confirmed location change, updating local database with new location")
                                viewModel.updateSystemLocationLocally()

                                // Step 2–3: Run the full finish logic (sync + CSV upload)
                                Log.d("MESSA-DEBUG","User confirmed location change, updating starting 'finaliseCalibrationAndUpload'")
                                viewModel.finaliseCalibrationAndUpload(context, apiService) { message ->
                                    dialogMessage = message
                                    showResultDialog = true
                                }
                            }
                        }) {
                            Text("Yes")
                        }
                    }
,
                    dismissButton = {
                        TextButton(onClick = { showLocationChangeDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }

// --- Result Dialog ---
            if (showResultDialog) {
                AlertDialog(
                    onDismissRequest = { showResultDialog = false },
                    title = { Text("Calibration End") },
                    text = { Text(dialogMessage) },
                    confirmButton = {
                        OutlinedButton(onClick = {
                            showResultDialog = false
                            activity?.finish()
                        }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }

    // Result Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Calibration End") },
            text = { Text(text = dialogMessage) },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        showDialog = false
                        activity?.finish()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}
