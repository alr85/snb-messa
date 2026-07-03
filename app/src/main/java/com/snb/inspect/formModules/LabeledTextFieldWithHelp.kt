package com.snb.inspect.formModules

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import com.snb.inspect.formModules.inputs.SimpleTextInput


@Composable
fun LabeledTextFieldWithHelp(
    label: String = "",
    value: String,
    onValueChange: (String) -> Unit,
    helpText: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isNAToggleEnabled: Boolean = true,
    rowDisabled: Boolean = false,
    pvStatus: String? = null,
    pvRules: List<PvRule> = emptyList(),
    maxLength: Int? = null,
    singleLine: Boolean = true,
    transformInput: ((String) -> String)? = null,
    showCounter: Boolean = true,
    showInputLabel: Boolean = false,
    showHelpOnFocusIfEmpty: Boolean = false,
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    val isNa = value == "N/A"
    val inputDisabled = rowDisabled || isNa

    FormRowWrapper(
        label = label,
        naButtonText = if (isNa) "Edit" else "N/A",
        isDisabled = false, // keep wrapper consistent with your existing behavior
        pvStatus = pvStatus,
        pvRules = pvRules,
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
            label = if (showInputLabel) label else "",
            keyboardType = keyboardType,
            isDisabled = inputDisabled,
            maxLength = maxLength,
            singleLine = singleLine,
            transformInput = transformInput,
            showCounter = showCounter,
            modifier = Modifier.onFocusChanged { focusState ->
                if (showHelpOnFocusIfEmpty && focusState.isFocused && value.isBlank()) {
                    showHelpDialog = true
                }
            }
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
