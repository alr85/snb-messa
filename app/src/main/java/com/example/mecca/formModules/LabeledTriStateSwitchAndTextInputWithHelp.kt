package com.example.mecca.formModules

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun LabeledTriStateSwitchAndTextInputWithHelp(
    label: String,
    currentState: YesNoState = YesNoState.NO, // Default set to NO
    onStateChange: (YesNoState) -> Unit,
    helpText: String,
    inputLabel: String,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    inputKeyboardType: KeyboardType = KeyboardType.Text
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    var isDisabled by remember { mutableStateOf(false) }

    // Initialize current state to NO if it is UNSPECIFIED
    var localCurrentState by remember { mutableStateOf(if (currentState == YesNoState.UNSPECIFIED) YesNoState.NO else currentState) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label text takes up more space
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            modifier = Modifier
                .weight(3f)
                .padding(end = 8.dp)
        )

        // Switch to toggle between Yes, No, etc.
        Switch(
            checked = localCurrentState == YesNoState.YES,
            onCheckedChange = {
                if (!isDisabled) {
                    localCurrentState = if (it) YesNoState.YES else YesNoState.NO
                    onStateChange(localCurrentState)
                    if (localCurrentState == YesNoState.YES) {
                        isDisabled = false
                    } else {
                        onInputValueChange("N/A")
                    }
                }
            },
            enabled = !isDisabled
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Text to display current state
        Text(
            text = when (localCurrentState) {
                YesNoState.YES -> "Yes"
                YesNoState.NO -> "No"
                YesNoState.NA -> "N/A"
                YesNoState.UNSPECIFIED -> "Unspecified"
            },
            color = if (isDisabled) Color.Gray else Color.Black
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Text input field or Spacer based on the state
        if (localCurrentState == YesNoState.YES) {
            OutlinedTextField(
                value = inputValue,
                onValueChange = {
                    if (!isDisabled) {
                        onInputValueChange(it)
                    }
                },
                label = { Text(text = inputLabel) },
                modifier = Modifier
                    .weight(4f)
                    .padding(end = 8.dp),
                enabled = !isDisabled,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = inputKeyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Gray
                )
            )
        } else {
            Spacer(modifier = Modifier.weight(4f).padding(end = 8.dp))
        }

        // Button to toggle between "N/A" and re-enable the switch and input
        TextButton(
            onClick = {
                if (isDisabled) {
                    localCurrentState = YesNoState.NO // Reset to a default editable state (No)
                    onStateChange(localCurrentState)
                    onInputValueChange("") // Clear input
                    isDisabled = false
                } else {
                    localCurrentState = YesNoState.NA // Set "N/A" and disable switch and input
                    onStateChange(localCurrentState)
                    onInputValueChange("N/A")
                    isDisabled = true
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Text(if (isDisabled) "Edit" else "N/A")
        }

        // Help IconButton
        IconButton(
            onClick = { showHelpDialog = true },
            modifier = Modifier.weight(0.5f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = "Help for $label"
            )
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(text = label) },
            text = { Text(text = helpText) },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
