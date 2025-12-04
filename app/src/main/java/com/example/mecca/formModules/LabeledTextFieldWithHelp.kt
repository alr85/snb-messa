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
import androidx.compose.ui.text.input.KeyboardType
import com.example.mecca.formModules.inputs.SimpleTextInput


@Composable
fun LabeledTextFieldWithHelp(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    helpText: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isNAToggleEnabled: Boolean = true
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    // Derive disabled from model value. No stale locals.
    var isDisabled by remember { mutableStateOf(value == "N/A") }
    LaunchedEffect(value) { isDisabled = value == "N/A" }

    FormRowWrapper(
        label = label,
        naButtonText = if (isDisabled) "Edit" else "N/A",
        isDisabled = isDisabled,
        onNaClick = if (isNAToggleEnabled) {
            {
                val next = !isDisabled
                isDisabled = next
                onValueChange(if (next) "N/A" else "")
            }
        } else null,
        onHelpClick = { showHelpDialog = true }
    ) { disabled ->
        SimpleTextInput(
            value = value,
            onValueChange = { raw ->
                if (!disabled) {
                    var cleaned = raw
                    if (keyboardType == KeyboardType.Text) {
                        cleaned = cleaned.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }
                    }
                    onValueChange(cleaned)
                }
            },
            label = label,
            keyboardType = keyboardType,
            isDisabled = disabled
        )

    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(text = label) },
            text = { Text(text = helpText) },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) { Text("OK") }
            }
        )
    }
}
