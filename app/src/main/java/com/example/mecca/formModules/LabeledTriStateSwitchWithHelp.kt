package com.example.mecca.formModules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp

@Composable
fun LabeledTriStateSwitchWithHelp(
    label: String,
    currentState: YesNoState,
    onStateChange: (YesNoState) -> Unit,
    helpText: String,
    isNAToggleEnabled: Boolean = true
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    var isDisabled by remember { mutableStateOf(currentState == YesNoState.NA) }

    FormRowWrapper(
        label = label,
        naButtonText = if (isDisabled) "Edit" else "N/A",
        isDisabled = isDisabled,
        onNaClick = if (isNAToggleEnabled) {
            {
                isDisabled = !isDisabled
                val newState = if (isDisabled) YesNoState.NA else YesNoState.NO
                onStateChange(newState)
            }
        } else null,
        onHelpClick = { showHelpDialog = true }
    ) { disabled ->

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Switch control
            Switch(
                checked = currentState == YesNoState.YES,
                onCheckedChange = {
                    if (!disabled) {
                        onStateChange(if (it) YesNoState.YES else YesNoState.NO)
                    }
                },
                enabled = !disabled
            )

            // Text label for the state
            Text(
                text = when (currentState) {
                    YesNoState.YES -> "Yes"
                    YesNoState.NO -> "No"
                    YesNoState.NA -> "N/A"
                    YesNoState.UNSPECIFIED -> "Unspecified"
                },
                color = if (disabled) Color.Gray else Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(label) },
            text = { Text(helpText) },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

//@Composable
//fun LabeledTriStateSwitchWithHelp(
//    label: String,
//    currentState: YesNoState,
//    onStateChange: (YesNoState) -> Unit,
//    helpText: String,
//    isNAToggleEnabled: Boolean = true // New parameter to control the "N/A" toggle
//) {
//    var showHelpDialog by remember { mutableStateOf(false) }
//    var isDisabled by remember { mutableStateOf(false) }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Label text takes up more space
//        Text(
//            text = label,
//            style = MaterialTheme.typography.labelLarge,
//            maxLines = 1,
//            modifier = Modifier
//                .weight(3f)
//                .padding(end = 8.dp)
//        )
//
//        // Tri-state switch control
//        Switch(
//            checked = currentState == YesNoState.YES,
//            onCheckedChange = {
//                if (!isDisabled) {
//                    onStateChange(if (it) YesNoState.YES else YesNoState.NO)
//                }
//            },
//            enabled = !isDisabled
//        )
//
//        Spacer(modifier = Modifier.width(8.dp))
//
//        // Text to indicate current state
//        Text(
//            text = when (currentState) {
//                YesNoState.YES -> "Yes"
//                YesNoState.NO -> "No"
//                YesNoState.NA -> "N/A"
//                YesNoState.UNSPECIFIED -> "Unspecified"
//            },
//            color = if (isDisabled) Color.Gray else Color.Black
//        )
//
//        Spacer(modifier = Modifier.weight(4f))
//
//        // Button to toggle between "N/A" and re-enable the switch
//
//        if (isNAToggleEnabled){
//
//            TextButton(
//                onClick = {
//                    if (isDisabled) {
//                        onStateChange(YesNoState.NO) // Reset to a default editable state
//                        isDisabled = false
//                    } else {
//                        onStateChange(YesNoState.NA) // Set "N/A" and disable the switch
//                        isDisabled = true
//                    }
//                },
//                modifier = Modifier.weight(1f)
//            ) {
//                Text(if (isDisabled) "Edit" else "N/A")
//            }
//
//        }else {
//            // Spacer to occupy the space of the button if the toggle is not enabled
//            Spacer(modifier = Modifier.weight(1f))
//        }
//
//
//        // Help IconButton
//        IconButton(
//            onClick = { showHelpDialog = true },
//            modifier = Modifier.weight(0.5f)
//        ) {
//            Icon(
//                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
//                contentDescription = "Help for $label"
//            )
//        }
//    }
//
//    // Show Help Dialog
//    if (showHelpDialog) {
//        AlertDialog(
//            onDismissRequest = { showHelpDialog = false },
//            title = { Text(text = label) },
//            text = { Text(text = helpText) },
//            confirmButton = {
//                TextButton(onClick = { showHelpDialog = false }) {
//                    Text("OK")
//                }
//            }
//        )
//    }
//}

