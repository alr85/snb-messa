package com.example.mecca.formModules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun LabeledTwoTextInputsWithHelp(
    label: String,
    firstInputLabel: String,
    firstInputValue: String,
    onFirstInputValueChange: (String) -> Unit,
    secondInputLabel: String,
    secondInputValue: String,
    onSecondInputValueChange: (String) -> Unit,
    helpText: String,
    firstInputKeyboardType: KeyboardType = KeyboardType.Text,
    secondInputKeyboardType: KeyboardType = KeyboardType.Text,
    isNAToggleEnabled: Boolean = true
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    var isDisabled by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label text
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(3f)
                .padding(end = 8.dp)
        )

        // First Text Input Field
        OutlinedTextField(
            value = firstInputValue,
            onValueChange = {
                if (!isDisabled) {
                    onFirstInputValueChange(it)
                }
            },
            label = { Text(text = firstInputLabel) },
            modifier = Modifier
                .weight(2f)
                .padding(end = 8.dp),
            enabled = !isDisabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = firstInputKeyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Second Text Input Field
        OutlinedTextField(
            value = secondInputValue,
            onValueChange = {
                if (!isDisabled) {
                    onSecondInputValueChange(it)
                }
            },
            label = { Text(text = secondInputLabel) },
            modifier = Modifier
                .weight(3f)
                .padding(end = 8.dp),
            enabled = !isDisabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = secondInputKeyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Button to toggle between "N/A" and re-enable the inputs
        if (isNAToggleEnabled) {
            TextButton(
                onClick = {
                    if (isDisabled) {
                        // Re-enable the inputs for user input
                        onFirstInputValueChange("") // Clear first input
                        onSecondInputValueChange("") // Clear second input
                        isDisabled = false
                    } else {
                        // Set "N/A" and disable the inputs
                        onFirstInputValueChange("N/A")
                        onSecondInputValueChange("N/A")
                        isDisabled = true
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isDisabled) "Edit" else "N/A")
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
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

    // Show Help Dialog
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
