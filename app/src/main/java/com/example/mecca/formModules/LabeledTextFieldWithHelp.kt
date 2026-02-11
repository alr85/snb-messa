package com.example.mecca.formModules

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
    rowDisabled: Boolean = false,
    maxLength: Int? = null,
    singleLine: Boolean = true,
    transformInput: ((String) -> String)? = null,
    showCounter: Boolean = true,
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    val isNa = value == "N/A"
    val inputDisabled = rowDisabled || isNa

    FormRowWrapper(
        label = label,
        naButtonText = if (isNa) "Edit" else "N/A",
        isDisabled = false, // keep wrapper consistent with your existing behavior
        onNaClick = if (isNAToggleEnabled) {
            { onValueChange(if (isNa) "" else "N/A") }
        } else null,
        onHelpClick = { showHelpDialog = true },
    ) { _ ->
        SimpleTextInput(
            value = value,
            onValueChange = { raw ->
                if (!inputDisabled) onValueChange(raw)
            },
            label = label,
            keyboardType = keyboardType,
            isDisabled = inputDisabled,
            maxLength = maxLength,
            singleLine = singleLine,
            transformInput = transformInput,
            showCounter = showCounter
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
