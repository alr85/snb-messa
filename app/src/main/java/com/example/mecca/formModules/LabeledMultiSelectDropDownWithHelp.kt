package com.example.mecca.formModules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun LabeledMultiSelectDropdownWithHelp(
    label: String,
    value: String,
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    helpText: String,
    isNAToggleEnabled: Boolean = true // Parameter to control the "N/A" toggle
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
                .weight(3f)
                .padding(end = 8.dp)
        )

        // Box to contain the dropdown
        Box(
            modifier = Modifier
                .weight(5f)
                .padding(end = 8.dp)
        ) {
            // TextField that represents the dropdown
            OutlinedTextField(
                value = if (isDisabled) "N/A" else selectedOptions.joinToString(" | "),
                onValueChange = {},
                readOnly = true,
                label = { Text("Select options") },
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

            // Dropdown Menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    val isSelected = selectedOptions.contains(option)

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null // Handling the change manually
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = option)
                            }
                        },
                        onClick = {
                            // Update the selected options without closing the dropdown
                            val newSelectedOptions = if (isSelected) {
                                selectedOptions - option
                            } else {
                                selectedOptions + option
                            }
                            onSelectionChange(newSelectedOptions)
                        }
                    )
                }
            }
        }

        // Button to toggle between "N/A" and re-enable dropdown
        if (isNAToggleEnabled) {
            TextButton(
                onClick = {
                    if (isDisabled) {
                        // Re-enable the dropdown for user input
                        onSelectionChange(emptyList()) // Clear selections
                        isDisabled = false
                    } else {
                        // Set "N/A" and disable the dropdown
                        onSelectionChange(listOf("N/A"))
                        isDisabled = true
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isDisabled) "Edit" else "N/A")
            }
        } else {
            // Spacer to occupy the space of the button if the toggle is not enabled
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



