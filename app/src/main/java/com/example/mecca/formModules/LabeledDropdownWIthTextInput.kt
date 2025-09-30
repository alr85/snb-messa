package com.example.mecca.formModules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun LabeledDropdownWithTextInput(
    label: String,
    dropdownLabel: String,
    options: List<String>,
    selectedOption: String,
    onOptionChange: (String) -> Unit,
    helpText: String,
    inputLabel: String,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    inputKeyboardType: KeyboardType = KeyboardType.Text,
    isNAToggleEnabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
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
                .weight(2.5f)
                .padding(end = 8.dp)
        )

        // Text field for user input
        OutlinedTextField(
            value = inputValue,
            onValueChange = {
                if (!isDisabled) {
                    onInputValueChange(it)
                }
            },
            label = { Text(text = inputLabel) },
            modifier = Modifier
                .weight(3f)
                .padding(end = 8.dp),
            singleLine = true,
            enabled = !isDisabled,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = inputKeyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Gray,
                disabledTextColor = Color.Gray,
                disabledLabelColor = Color.Gray
            )
        )

        // Dropdown Menu
        Box(
            modifier = Modifier
                .weight(3f)
                .padding(end = 8.dp)
        ) {
            OutlinedTextField(
                value = if (isDisabled) "N/A" else selectedOption,
                onValueChange = {},
                readOnly = true,
                label = {Text (text = dropdownLabel)},
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown Arrow",
                        modifier = Modifier.clickable {
                            if (!isDisabled) {
                                expanded = !expanded
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isDisabled,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color.Gray,
                    disabledTextColor = Color.Gray,
                    disabledLabelColor = Color.Gray
                )
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }



        // Button to toggle between "N/A" and re-enable the fields
        if (isNAToggleEnabled) {
            TextButton(
                onClick = {
                    if (isDisabled) {
                        // Re-enable the dropdown and text field for user input
                        onOptionChange("")
                        onInputValueChange("")
                        isDisabled = false
                    } else {
                        // Set "N/A" and disable the dropdown and text field
                        onOptionChange("N/A")
                        onInputValueChange("N/A")
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
