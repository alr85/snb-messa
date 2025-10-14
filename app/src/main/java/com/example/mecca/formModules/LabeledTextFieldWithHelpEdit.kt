package com.example.mecca.formModules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


@Composable
fun LabeledTextFieldWithHelpEdit(
    label: String,
    onLabelChange: (String) -> Unit, // Callback to update label in ViewModel
    value: String,
    onValueChange: (String) -> Unit,
    helpText: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isNAToggleEnabled: Boolean = true // New parameter to control the "N/A" toggle
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    var isDisabled by remember { mutableStateOf(false) } // Track if the field is greyed out or not
    var isEditingLabel by remember { mutableStateOf(false) } // Track if label is being edited

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isEditingLabel) {
            // Editable label text field
            OutlinedTextField(
                value = label,
                onValueChange = {
                    onLabelChange(it) // Update label in ViewModel
                },
                modifier = Modifier
                    .weight(3f)
                    .padding(end = 8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        isEditingLabel = false // Exit editing mode
                    }
                ),
                placeholder = { Text("Enter label") }
            )
        } else {
            // Static label text
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline // Underlined text
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(3f)
                    .padding(end = 8.dp)
                    .clickable { isEditingLabel = true } // Start editing when clicked
            )
        }

        // OutlinedTextField for the main value
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (!isDisabled) { // Only allow text changes if not disabled
                    onValueChange(it)
                }
            },
            modifier = Modifier
                .weight(5f)
                .padding(end = 8.dp),
            singleLine = true,
            enabled = !isDisabled, // Disable input if greyed out
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Gray,
                disabledTextColor = Color.Gray,
                disabledLabelColor = Color.Gray
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = keyboardType
            )
        )

        // Button to toggle between auto-value and re-enable the text field
        if (isNAToggleEnabled) {
            TextButton(
                onClick = {
                    if (isDisabled) {
                        onValueChange("") // Clear the input (or leave it as is)
                        isDisabled = false
                    } else {
                        onValueChange(if (keyboardType == KeyboardType.Number) "0" else "N/A")
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

