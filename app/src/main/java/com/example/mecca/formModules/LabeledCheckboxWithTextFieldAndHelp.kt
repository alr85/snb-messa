package com.example.mecca.formModules

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledCheckboxWithTextFieldAndHelp(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean?) -> Unit,
    textValue: String,
    onTextValueChange: (String) -> Unit,
    helpText: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    var isDisabled by remember { mutableStateOf(false) } // Track if the field is greyed out or not

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Label and Checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(3f)
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onCheckedChange(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.Red,
                    uncheckedColor = Color.Gray
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // OutlinedTextField
        OutlinedTextField(
            value = textValue,
            onValueChange = {
                if (!isDisabled) { // Only allow text changes if not disabled
                    onTextValueChange(it)
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
        TextButton(
            onClick = {
                if (isDisabled) {
                    // If disabled, re-enable the field and allow user input
                    onTextValueChange("") // Clear the input (or leave it as is)
                    isDisabled = false
                } else {
                    // If not disabled, set "N/A" or "0" and disable the field
                    if (keyboardType == KeyboardType.Number) {
                        onTextValueChange("0")
                    } else {
                        onTextValueChange("N/A")
                    }
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
