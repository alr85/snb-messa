package com.example.mecca

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

    progress: Float, // Progress between 0.0 and 1.0
    viewModel: CalibrationMetalDetectorConveyorViewModel // Pass the view model to access details for the dialog
) {
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showBackDisabledDialog by remember { mutableStateOf(false) }

    // Disable the system back button while in calibration
    androidx.activity.compose.BackHandler(enabled = true) {
        showBackDisabledDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(10.dp)
    ) {
        // Banner content
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Company logo on the left with original colors
            Box(
                modifier = Modifier
                    .width(150.dp) // Fixed width for the logo
                    .padding(start = 2.dp) // Add left padding
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_electronics),
                    contentScale = ContentScale.Fit, // Ensures the image fits within the bounds
                    contentDescription = "Company Logo"
                )
            }

            Spacer(modifier = Modifier.width(2.dp)) // Add some space between logo and text

            // Centered text in the remaining space
            Box(
                modifier = Modifier
                    .weight(1f) // Allocate the remaining space
                    .fillMaxWidth(), // Ensure it uses all the space allocated
                contentAlignment = Alignment.Center // Center content in the Box
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Metal Detector Calibration",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Calibration ID: ${viewModel.calibrationId.value}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray, // Use a predefined grey color
                            //modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }



            }
        }



        Spacer(modifier = Modifier.height(12.dp))



        Text(
            text = "${viewModel.modelDescription.value} ( ${viewModel.serialNumber.value} ) ",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(6.dp))


        // Progress Bar - Clickable to show details
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDetailsDialog = true }, // Make the progress bar clickable to open the dialog
        )
    }

    // Dialog to display calibration details
    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },

            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp) // Limit height to prevent the dialog from taking up the full screen
                        .verticalScroll(rememberScrollState()) // Enable scrolling if content exceeds the limit
                        .padding(16.dp)
                ) {
                    CalMetalDetectorConveyorSummaryDetails(viewModel = viewModel)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDetailsDialog = false // Close dialog when confirmed
                    }
                ) {
                    Text("Close")
                }
            },
            containerColor = Color.White
        )
    }

    // Back button disabled dialog
    if (showBackDisabledDialog) {
        AlertDialog(
            onDismissRequest = { showBackDisabledDialog = false },
            confirmButton = {
                TextButton(onClick = { showBackDisabledDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Action Disabled") },
            text = { Text("To prevent data loss - the normal back button is disabled during calibration. Please use the navigation buttons at the top of the screen") },
            containerColor = Color.White
        )
    }
}
