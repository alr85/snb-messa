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
import com.example.mecca.formModules.inputs.DropdownWithTextInput

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
    isNAToggleEnabled: Boolean = true,
    inputMaxLength: Int? = null,
    inputSingleLine: Boolean = true,
    inputTransform: ((String) -> String)? = null,
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    var isDisabled by remember {
        mutableStateOf(selectedOption == "N/A" && inputValue == "N/A")
    }
    LaunchedEffect(selectedOption, inputValue) {
        isDisabled = (selectedOption == "N/A" && inputValue == "N/A")
    }

    FormRowWrapper(
        label = label,
        naButtonText = if (isDisabled) "Edit" else "N/A",
        isDisabled = isDisabled,
        onNaClick = if (isNAToggleEnabled) {
            {
                val next = !isDisabled
                isDisabled = next
                val sentinel = if (next) "N/A" else ""
                onOptionChange(sentinel)
                onInputValueChange(sentinel)
            }
        } else null,
        onHelpClick = { showHelpDialog = true }
    ) { disabled ->
        DropdownWithTextInput(
            dropdownLabel = dropdownLabel,
            options = options,
            selectedOption = selectedOption,
            onOptionChange = { if (!disabled) onOptionChange(it) },

            inputLabel = inputLabel,
            inputValue = inputValue,
            onInputValueChange = { if (!disabled) onInputValueChange(it) },
            inputKeyboardType = inputKeyboardType,
            inputMaxLength = inputMaxLength,
            inputSingleLine = inputSingleLine,
            inputTransform = inputTransform,

            isDisabled = disabled
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(label) },
            text = { Text(helpText) },
            confirmButton = { TextButton(onClick = { showHelpDialog = false }) { Text("OK") } }
        )
    }
}
