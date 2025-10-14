package com.example.mecca.calibrationViewModels


import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.ui.theme.AppConstants

@Composable
fun CalibrationNavigationButtons(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel,
    onPreviousClick: () -> Unit,
    onCancelClick: () -> Unit,
    onNextClick: () -> Unit,
    onSaveAndExitClick: () -> Unit,
    isNextEnabled: Boolean,
    isFirstStep: Boolean
) {
    var showDialog by remember { mutableStateOf(false) }
    val activity = LocalActivity.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Step Button (disabled on the first step)

        if (!isFirstStep) {
            OutlinedButton(

                onClick = {
                    navController.popBackStack()
                    onPreviousClick()
                    viewModel.decrementStep() },
                enabled = !viewModel.isNavigating.collectAsState().value,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Step",
                    modifier = Modifier.size(AppConstants.CalibrationNavigationButtonIconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Back",
                    fontSize = AppConstants.CalibrationNavigationButtonFontSize)
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Cancel Calibration Button
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel Calibration",
                modifier = Modifier.size(AppConstants.CalibrationNavigationButtonIconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cancel",
                fontSize = AppConstants.CalibrationNavigationButtonFontSize)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Save and Exit Button
        OutlinedButton(
            onClick = {
                onSaveAndExitClick() // Perform save operation
                activity?.finish() // Close the activity without deleting the database row
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Save and Exit",
                modifier = Modifier.size(AppConstants.CalibrationNavigationButtonIconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Exit",
            fontSize = AppConstants.CalibrationNavigationButtonFontSize)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Next Step Button
        Button(
            onClick = {
                viewModel.startNavigation()
                onNextClick()
                viewModel.incrementStep()
            },
            enabled = isNextEnabled && !viewModel.isNavigating.collectAsState().value,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "Next",
                fontSize = AppConstants.CalibrationNavigationButtonFontSize)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next Step",
                modifier = Modifier.size(AppConstants.CalibrationNavigationButtonIconSize)
            )
        }

        // Show confirmation dialog for Cancel button
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Are you sure?") },
                text = { Text("Cancelling will discard all calibration data. Do you want to proceed?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        viewModel.clearCalibrationData()
                        viewModel.deleteCalibration(viewModel.calibrationId.value)
                        onCancelClick()
                        activity?.finish() // Close the activity
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("No")
                    }
                }
            )
        }
    }
}
