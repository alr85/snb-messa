package com.snb.inspect.formModules

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.snb.inspect.formModules.inputs.MultiSelectDropdown

@Composable
fun LabeledMultiSelectDropdownWithHelp(
    label: String,
    value: String, // display string
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    helpText: String,
    pvStatus: String? = null,
    pvRules: List<PvRule> = emptyList(),
    isNAToggleEnabled: Boolean = true
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    fun isNaOnly(sel: List<String>) =
        sel.size == 1 && sel.firstOrNull() == "N/A"

    // Derive disabled from model state; resync on changes
    var isDisabled by remember { mutableStateOf(isNaOnly(selectedOptions)) }
    LaunchedEffect(selectedOptions) {
        isDisabled = isNaOnly(selectedOptions)
    }

    // Guard: when enabled, prevent mixing "N/A" with real options
    val guardedOnSelectionChange: (List<String>) -> Unit = { newSel ->
        if (!isDisabled) {
            val cleaned = if ("N/A" in newSel && newSel.size > 1)
                newSel.filterNot { it == "N/A" }
            else newSel
            onSelectionChange(cleaned)
        }
    }

    FormRowWrapper(
        label = label,
        naButtonText = if (isDisabled) "Edit" else "N/A",
        isDisabled = isDisabled,
        pvStatus = pvStatus,
        pvRules = pvRules,
        onNaClick = if (isNAToggleEnabled) {
            {
                val next = !isDisabled
                isDisabled = next
                onSelectionChange(if (next) listOf("N/A") else emptyList())
            }
        } else null,
        onHelpClick = { showHelpDialog = true }
    ) { disabled ->
        MultiSelectDropdown(
            value = value,
            options = options,
            selectedOptions = selectedOptions,
            onSelectionChange = guardedOnSelectionChange,
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
