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
    isNAToggleEnabled: Boolean = true,
    rowDisabled: Boolean = false // optional: only use if you truly want to lock the whole row
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    val isNa = value == "N/A"
    val inputDisabled = rowDisabled || isNa

    FormRowWrapper(
        label = label,
        naButtonText = if (isNa) "Edit" else "N/A",
        // IMPORTANT: don't disable the wrapper just because it's N/A
        isDisabled = rowDisabled,
        onNaClick = if (isNAToggleEnabled) {
            {
                // Toggle N/A regardless of inputDisabled, as long as the row isn't locked
                if (!rowDisabled) {
                    onValueChange(if (isNa) "" else "N/A")
                }
            }
        } else null,
        onHelpClick = { showHelpDialog = true }
    ) { _ ->
        SimpleTextInput(
            value = value,
            onValueChange = { raw ->
                if (!inputDisabled) {
                    val cleaned =
                        if (keyboardType == KeyboardType.Text) {
                            raw.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            }
                        } else raw

                    onValueChange(cleaned)
                }
            },
            label = label,
            keyboardType = keyboardType,
            isDisabled = inputDisabled
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
