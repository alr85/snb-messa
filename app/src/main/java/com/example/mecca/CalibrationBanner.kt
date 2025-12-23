package com.example.mecca

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorSummaryDetails


@Composable
fun CalibrationBanner(
    progress: Float,                     // now comes from NavController state
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showBackDisabledDialog by remember { mutableStateOf(false) }

    // Disable system back
    BackHandler(enabled = true) {
        showBackDisabledDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(10.dp)
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        // ---------------- Banner Row ----------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Logo
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .padding(start = 2.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_electronics),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Company Logo"
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Title and Calibration ID
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(
                        text = "Metal Detector Calibration",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Calibration ID: ${viewModel.calibrationId.value}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Model / Serial
        Text(
            text = "${viewModel.modelDescription.value} ( ${viewModel.serialNumber.value} )",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // ---------------- Progress Bar ----------------
        LinearProgressIndicator(
            progress = { progress },        // progress now comes from the wrapper
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDetailsDialog = true }
        )
    }

    // ---------------- Details Dialog ----------------
    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    CalMetalDetectorConveyorSummaryDetails(viewModel = viewModel)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = Color.White
        )
    }

    // ---------------- Back Disabled Dialog ----------------
    if (showBackDisabledDialog) {
        AlertDialog(
            onDismissRequest = { showBackDisabledDialog = false },
            confirmButton = {
                TextButton(onClick = { showBackDisabledDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Action Disabled") },
            text = {
                Text(
                    "To prevent data loss, the normal back button is disabled during calibration. " +
                            "Please use the navigation buttons at the bottom of the screen."
                )
            },
            containerColor = Color.White
        )
    }
}
