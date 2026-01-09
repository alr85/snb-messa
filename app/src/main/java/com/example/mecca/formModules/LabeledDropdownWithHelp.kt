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
    val isNa = selectedOption == "N/A"
    val inputDisabled = isNa

    FormRowWrapper(
        label = label,
        naButtonText = if (isNa) "Edit" else "N/A",
        isDisabled = false, // ALWAYS
        onNaClick = if (isNAToggleEnabled) {
            { onSelectionChange(if (isNa) "" else "N/A") }
        } else null,
        onHelpClick = { showHelpDialog = true }
    ) { _ ->
        SimpleDropdown(
            options = options,
            selectedOption = selectedOption,
            onSelectionChange = { if (!inputDisabled) onSelectionChange(it) },
            isDisabled = inputDisabled
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


