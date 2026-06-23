package com.snb.inspect.screens.service.sov

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.snb.inspect.ApiService
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
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

    var engineerConfirmed by remember { mutableStateOf(false) }

    // Track confirmed sections
    val confirmedSections = remember { mutableStateMapOf<String, Boolean>() }

    // Define the list of required sections that must be checked
    val requiredSections = remember(viewModel.sensitivityAsLeftFerrous.value, viewModel.sensitivityAsLeftNonFerrous.value, viewModel.sensitivityAsLeftStainless.value) {
        val list = mutableListOf(
            "Validation Details",
            "Sensitivity (As Left)",
            "Validation Results",
            "Pack Validation"
        )
        if (viewModel.sensitivityAsLeftFerrous.value != "N/A" || 
            viewModel.sensitivityAsLeftNonFerrous.value != "N/A" || 
            viewModel.sensitivityAsLeftStainless.value != "N/A") {
            list.add("Lowest Signals")
        }
        list
    }

    val allSectionsVerified = requiredSections.all { confirmedSections[it] == true }

    // Auto-reset signature if verification is retracted
    LaunchedEffect(allSectionsVerified) {
        if (!allSectionsVerified) engineerConfirmed = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                SovSummaryDetails(
                    viewModel = viewModel,
                    isConfirmationMode = true,
                    confirmedSections = confirmedSections,
                    onSectionConfirmChange = { section, confirmed ->
                        confirmedSections[section] = confirmed
                    }
                )
            }

            item {
                LabeledTextFieldWithHelp(
                    label = "Customer Name",
                    value = viewModel.customerName.value,
                    onValueChange = { viewModel.customerName.value = it },
                    helpText = "Name of the customer representative witnessing the test."
                )
                FormSpacer()
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (allSectionsVerified) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Engineer Declaration",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (allSectionsVerified) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.error
                    )

                    if (!allSectionsVerified) {
                        Text(
                            text = "Please verify all required sections above before signing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = engineerConfirmed,
                            onCheckedChange = { engineerConfirmed = it },
                            enabled = allSectionsVerified && !isUploading
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "I confirm this validation has been completed in accordance with company procedures and that the recorded information is accurate to the best of my knowledge. This digital confirmation is equivalent to a handwritten signature.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (allSectionsVerified) MaterialTheme.colorScheme.onSurface
                            else Color.Gray
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
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
                    enabled = engineerConfirmed && allSectionsVerified && !isUploading && viewModel.customerName.value.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Finalise"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finalise and Upload Validation")
                }
            }
        }

        // Uploading overlay
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Uploading validation to cloud...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
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
}
