package com.snb.inspect.screens.service.sov

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.snb.inspect.ApiService
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledReadOnlyField
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar
import kotlinx.coroutines.launch

@Composable
fun SovSummaryScreen(viewModel: SensitivityOptimisationValidationViewModel, apiService: ApiService) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isUploading by viewModel.isUploading.collectAsState()
    
    var showLocationChangeDialog by remember { mutableStateOf(false) }
    var pendingLocationCandidate by remember { mutableStateOf<String?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    ScrollableWithScrollbar(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(16.dp))
            CalibrationHeader("Validation Summary")
            Spacer(Modifier.height(16.dp))

            LabeledReadOnlyField(label = "Product", value = viewModel.productDescription.value)
            FormSpacer()

            LabeledReadOnlyField(label = "Current Location", value = viewModel.lastLocation.value)
            FormSpacer()

            if (viewModel.newLocation.value != viewModel.lastLocation.value) {
                LabeledReadOnlyField(label = "New Location", value = viewModel.newLocation.value)
                FormSpacer()
            }
            
            LabeledReadOnlyField(label = "As Found (Fe/NF/SS)", value = "${viewModel.sensitivityAsFoundFerrous.value} / ${viewModel.sensitivityAsFoundNonFerrous.value} / ${viewModel.sensitivityAsFoundStainless.value}")
            FormSpacer()

            LabeledReadOnlyField(label = "As Left (Fe/NF/SS)", value = "${viewModel.sensitivityAsLeftFerrous.value} / ${viewModel.sensitivityAsLeftNonFerrous.value} / ${viewModel.sensitivityAsLeftStainless.value}")
            FormSpacer()

            CalibrationHeader("Validation Results")
            FormSpacer()
            
            val val1TotalPasses = (viewModel.val1LeadingPasses.value.toIntOrNull() ?: 0) + (viewModel.val1MiddlePasses.value.toIntOrNull() ?: 0) + (viewModel.val1TrailingPasses.value.toIntOrNull() ?: 0)
            val val1TotalSuccesses = (viewModel.val1LeadingSuccesses.value.toIntOrNull() ?: 0) + (viewModel.val1MiddleSuccesses.value.toIntOrNull() ?: 0) + (viewModel.val1TrailingSuccesses.value.toIntOrNull() ?: 0)
            Text("${viewModel.validationTest1Description.value}: $val1TotalSuccesses / $val1TotalPasses")

            val val2TotalPasses = (viewModel.val2LeadingPasses.value.toIntOrNull() ?: 0) + (viewModel.val2MiddlePasses.value.toIntOrNull() ?: 0) + (viewModel.val2TrailingPasses.value.toIntOrNull() ?: 0)
            val val2TotalSuccesses = (viewModel.val2LeadingSuccesses.value.toIntOrNull() ?: 0) + (viewModel.val2MiddleSuccesses.value.toIntOrNull() ?: 0) + (viewModel.val2TrailingSuccesses.value.toIntOrNull() ?: 0)
            Text("${viewModel.validationTest2Description.value}: $val2TotalSuccesses / $val2TotalPasses")

            val val3TotalPasses = (viewModel.val3LeadingPasses.value.toIntOrNull() ?: 0) + (viewModel.val3MiddlePasses.value.toIntOrNull() ?: 0) + (viewModel.val3TrailingPasses.value.toIntOrNull() ?: 0)
            val val3TotalSuccesses = (viewModel.val3LeadingSuccesses.value.toIntOrNull() ?: 0) + (viewModel.val3MiddleSuccesses.value.toIntOrNull() ?: 0) + (viewModel.val3TrailingSuccesses.value.toIntOrNull() ?: 0)
            Text("${viewModel.validationTest3Description.value}: $val3TotalSuccesses / $val3TotalPasses")
            FormSpacer()

            if (viewModel.sensitivityAsLeftFerrous.value != "N/A" || viewModel.sensitivityAsLeftNonFerrous.value != "N/A" || viewModel.sensitivityAsLeftStainless.value != "N/A") {
                CalibrationHeader("Lowest Signals")
                FormSpacer()
                
                if (viewModel.sensitivityAsLeftFerrous.value != "N/A") {
                    val ferrousSignal = if (viewModel.system.systemTypeId == 1) 
                        "${viewModel.minSignalAsLeftFerrousLeading.value} / ${viewModel.minSignalAsLeftFerrousMiddle.value} / ${viewModel.minSignalAsLeftFerrousTrailing.value}"
                        else viewModel.minSignalAsLeftFerrousLeading.value
                    LabeledReadOnlyField(label = "Ferrous (L/M/T)", value = ferrousSignal)
                    FormSpacer()
                }

                if (viewModel.sensitivityAsLeftNonFerrous.value != "N/A") {
                    val nonFerrousSignal = if (viewModel.system.systemTypeId == 1) 
                        "${viewModel.minSignalAsLeftNonFerrousLeading.value} / ${viewModel.minSignalAsLeftNonFerrousMiddle.value} / ${viewModel.minSignalAsLeftNonFerrousTrailing.value}"
                        else viewModel.minSignalAsLeftNonFerrousLeading.value
                    LabeledReadOnlyField(label = "Non-Ferrous (L/M/T)", value = nonFerrousSignal)
                    FormSpacer()
                }

                if (viewModel.sensitivityAsLeftStainless.value != "N/A") {
                    val stainlessSignal = if (viewModel.system.systemTypeId == 1) 
                        "${viewModel.minSignalAsLeftStainlessLeading.value} / ${viewModel.minSignalAsLeftStainlessMiddle.value} / ${viewModel.minSignalAsLeftStainlessTrailing.value}"
                        else viewModel.minSignalAsLeftStainlessLeading.value
                    LabeledReadOnlyField(label = "Stainless (L/M/T)", value = stainlessSignal)
                    FormSpacer()
                }
            }

            LabeledTextFieldWithHelp(
                label = "Customer Name",
                value = viewModel.customerName.value,
                onValueChange = { viewModel.customerName.value = it },
                helpText = "Name of the customer representative witnessing the test."
            )
            FormSpacer()

            Button(
                onClick = {
                    val oldLocation = viewModel.lastLocation.value.trim()
                    val proposed = viewModel.newLocation.value.trim()

                    val candidate: String? = when {
                        proposed.isBlank() -> null
                        proposed.equals(oldLocation, ignoreCase = true) -> null
                        else -> proposed
                    }

                    if (candidate != null) {
                        pendingLocationCandidate = candidate
                        showLocationChangeDialog = true
                    } else {
                        coroutineScope.launch {
                            viewModel.finaliseAndUpload(context, apiService) { message ->
                                dialogMessage = message
                                showResultDialog = true
                            }
                        }
                    }
                },
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finalise and Upload Validation")
            }

            Spacer(Modifier.height(60.dp))
        }
    }

    // Location-change confirmation dialog
    if (showLocationChangeDialog) {
        val candidate = pendingLocationCandidate
        AlertDialog(
            onDismissRequest = { if (!isUploading) showLocationChangeDialog = false },
            title = { Text("Confirm Location Change") },
            text = {
                Text(
                    "The system location has changed from " +
                            "'${viewModel.lastLocation.value}' to '$candidate'. " +
                            "Are you sure you want to update this in the database?"
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isUploading,
                    onClick = {
                        showLocationChangeDialog = false
                        coroutineScope.launch {
                            viewModel.setNewLocation(candidate!!)
                            viewModel.updateSystemLocationLocally()
                            viewModel.finaliseAndUpload(context, apiService) { message ->
                                dialogMessage = message
                                showResultDialog = true
                            }
                        }
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isUploading,
                    onClick = { showLocationChangeDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }

    // Result dialog
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("Validation Finished") },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(onClick = {
                    showResultDialog = false
                    (context as? Activity)?.finish()
                }) {
                    Text("OK")
                }
            }
        )
    }

    // Uploading overlay
    if (isUploading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
