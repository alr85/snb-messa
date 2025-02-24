package com.example.mecca.screens

import CalMetalDetectorConveyorSummaryDetails
import CalibrationBanner
import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.mecca.CalibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.CalibrationViewModels.CalibrationNavigationButtons
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSummary(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel(),
    apiService: ApiService
) {
    val progress = viewModel.progress
    val activity = LocalContext.current as? Activity
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isUploading by viewModel.isUploading.collectAsState()

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
                onPreviousClick = { /* Disable or hide the Previous button */ },
                onCancelClick = {
                    viewModel.clearCalibrationData()
                    navController.navigate("serviceHome") {
                        popUpTo("serviceHome") { inclusive = true }
                    }
                },
                onNextClick = { navController.navigate("CalMetalDetectorConveyorBackupPEC") },
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
                    coroutineScope.launch {
                        if (viewModel.cloudSystemId.value != 0 && viewModel.cloudSystemId.value != null) {
                            viewModel.updateCalibrationEnd()

                            val csvSuccess = viewModel.createAndUploadCsv(context, viewModel.calibrationId.value, apiService)
                            if (csvSuccess) {
                                dialogMessage = "Calibration completed and uploaded to the cloud."
                            } else {
                                dialogMessage = "Calibration was completed, but NOT uploaded to the cloud. Please try again later."
                            }
                        } else {
                            dialogMessage = "Calibration was NOT completed. Please ensure the system is synced with the cloud."
                        }
                        showDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Finish"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Finish")
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
