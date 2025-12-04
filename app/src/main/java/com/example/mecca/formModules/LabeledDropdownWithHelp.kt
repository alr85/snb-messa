package com.example.mecca.formModules

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.mecca.formModules.inputs.SimpleDropdown

@Composable
fun LabeledDropdownWithHelp(
    label: String,
    options: List<String>,
    selectedOption: String?,                 // nullable is fine
    onSelectionChange: (String) -> Unit,
    helpText: String,
    isNAToggleEnabled: Boolean = true
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    // Disabled iff the model says "N/A"
    var isDisabled by remember { mutableStateOf(selectedOption == "N/A") }
    LaunchedEffect(selectedOption) { isDisabled = (selectedOption == "N/A") }

    FormRowWrapper(
        label = label,
        naButtonText = if (isDisabled) "Edit" else "N/A",
        isDisabled = isDisabled,
        onNaClick = if (isNAToggleEnabled) {
            {
                val next = !isDisabled
                isDisabled = next
                onSelectionChange(if (next) "N/A" else "")
            }
        } else null,
        onHelpClick = { showHelpDialog = true }
    ) { disabled ->
        SimpleDropdown(
            options = options,
            selectedOption = selectedOption,
            onSelectionChange = { if (!disabled) onSelectionChange(it) },
            isDisabled = disabled
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(label) },
            text = { Text(helpText) },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) { Text("OK") }
            }
        )
    }
}



//@Composable
//fun LabeledDropdownWithHelp(
//    label: String,
//    options: List<String>,
//    selectedOption: String?,
//    onSelectionChange: (String) -> Unit,
//    helpText: String,
//    isNAToggleEnabled: Boolean = true // Parameter to control the "N/A" toggle
//) {
//    var expanded by remember { mutableStateOf(false) }
//    var showHelpDialog by remember { mutableStateOf(false) }
//    var isDisabled by remember { mutableStateOf(false) }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Label text
//        Text(
//            text = label,
//            style = MaterialTheme.typography.labelLarge,
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis,
//            modifier = Modifier
//                .weight(3f)
//                .padding(end = 8.dp)
//        )
//
//        // Box to contain the dropdown
//        Box(
//            modifier = Modifier
//                .weight(5f)
//                .padding(end = 8.dp)
//        ) {
//            // TextField that represents the dropdown
//            OutlinedTextField(
//                value = if (isDisabled) "N/A" else selectedOption ?: "",
//                onValueChange = {},
//                readOnly = true,
//                label = { Text("Select an option") },
//                trailingIcon = {
//                    Icon(
//                        imageVector = Icons.Default.ArrowDropDown,
//                        contentDescription = "Dropdown Arrow",
//                        modifier = Modifier.clickable {
//                            if (!isDisabled) {
//                                expanded = !expanded
//                            }
//                        }
//                    )
//                },
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !isDisabled,
//                colors = OutlinedTextFieldDefaults.colors(
//                    disabledBorderColor = Color.Gray,
//                    disabledTextColor = Color.Gray,
//                    disabledLabelColor = Color.Gray
//                )
//            )
//
//            // Dropdown Menu
//            DropdownMenu(
//                expanded = expanded,
//                onDismissRequest = { expanded = false }
//            ) {
//                options.forEach { option ->
//                    DropdownMenuItem(
//                        text = { Text(option) },
//                        onClick = {
//                            // Update the selected option
//                            onSelectionChange(option)
//                            expanded = false // Close the dropdown after selection
//                        }
//                    )
//                }
//            }
//        }
//
//        // Button to toggle between "N/A" and re-enable dropdown
//        if (isNAToggleEnabled) {
//            TextButton(
//                onClick = {
//                    if (isDisabled) {
//                        // Re-enable the dropdown for user input
//                        onSelectionChange("") // Clear selection
//                        isDisabled = false
//                    } else {
//                        // Set "N/A" and disable the dropdown
//                        onSelectionChange("N/A")
//                        isDisabled = true
//                    }
//                },
//                modifier = Modifier.weight(1f)
//            ) {
//                Text(if (isDisabled) "Edit" else "N/A")
//            }
//        } else {
//            // Spacer to occupy the space of the button if the toggle is not enabled
//            Spacer(modifier = Modifier.weight(1f))
//        }
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