package com.snb.inspect.formModules

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.snb.inspect.formModules.inputs.SimpleDropdown

@Composable
fun LabeledDropdownWithHelp(
    label: String,
    options: List<String>,
    selectedOption: String?,                 // nullable is fine
    onSelectionChange: (String) -> Unit,
    helpText: String,
    pvStatus: String? = null,
    pvRules: List<PvRule> = emptyList(),
    isNAToggleEnabled: Boolean = true,
    onNAChange: ((Boolean) -> Unit)? = null,
    isNA: Boolean? = null
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    val isNaState = isNA ?: (selectedOption == "N/A")

    FormRowWrapper(
        label = label,
        naButtonText = if (isNaState) "Edit" else "N/A",
        isDisabled = false, // ALWAYS
        pvStatus = pvStatus,
        pvRules = pvRules,
        onNaClick = if (isNAToggleEnabled) {
            {
                if (onNAChange != null) {
                    onNAChange(!isNaState)
                } else {
                    onSelectionChange(if (isNaState) "" else "N/A")
                }
            }
        } else null,
        onHelpClick = { showHelpDialog = true }
    ) { _ ->
        SimpleDropdown(
            options = options,
            selectedOption = selectedOption,
            onSelectionChange = { if (!isNaState) onSelectionChange(it) },
            isDisabled = isNaState
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
