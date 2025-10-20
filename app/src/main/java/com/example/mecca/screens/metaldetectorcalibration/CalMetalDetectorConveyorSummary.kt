package com.example.mecca.screens.metaldetectorcalibration

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.ApiService
import com.example.mecca.CalibrationBanner
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSummary(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel(),
    apiService: ApiService
) {
    // Prevent premature navigation
    LaunchedEffect(Unit) { viewModel.finishNavigation() }

    val progress = viewModel.progress
    val activity = LocalActivity.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isUploading by viewModel.isUploading.collectAsState()

    var showLocationChangeDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Banner
            item {
                CalibrationBanner(progress = progress, viewModel = viewModel)
            }

            // Nav buttons
            item {
                CalibrationNavigationButtons(
                    onPreviousClick = { viewModel.updateComplianceConfirmation() },
                    onCancelClick = {
                        viewModel.clearCalibrationData()
                        navController.navigate("serviceHome") {
                            popUpTo("serviceHome") { inclusive = true }
                        }
                    },
                    onNextClick = { },
                    isNextEnabled = false,
                    isFirstStep = false,
                    navController = navController,
                    viewModel = viewModel,
                    onSaveAndExitClick = { /* custom save logic */ }
                )
            }

            // Summary info
            item {
                CalMetalDetectorConveyorSummaryDetails(viewModel = viewModel)
            }

            // Finish button
            item {
                OutlinedButton(
                    onClick = {
                        val oldLocation = viewModel.lastLocation.value
                        val newLocation = viewModel.newLocation.value

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
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Finish"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finish")
                }
            }
        }

        // 🟡 Upload overlay (blocks input + shows spinner)
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Uploading calibration to cloud...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Location-change confirmation dialog
        if (showLocationChangeDialog) {
            AlertDialog(
                onDismissRequest = { showLocationChangeDialog = false },
                title = { Text("Confirm Location Change") },
                text = {
                    Text(
                        "The system location has changed from " +
                                "'${viewModel.lastLocation.value}' to '${viewModel.newLocation.value}'. " +
                                "Do you want to update this in the database and cloud?"
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showLocationChangeDialog = false
                        coroutineScope.launch {
                            Log.d("MESSA-DEBUG", "User confirmed location change — updating local DB")
                            viewModel.updateSystemLocationLocally()
                            Log.d("MESSA-DEBUG", "Starting finaliseCalibrationAndUpload")
                            viewModel.finaliseCalibrationAndUpload(context, apiService) { message ->
                                dialogMessage = message
                                showResultDialog = true
                            }
                        }
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLocationChangeDialog = false }) {
                        Text("No")
                    }
                }
            )
        }

        // Upload result dialog
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
