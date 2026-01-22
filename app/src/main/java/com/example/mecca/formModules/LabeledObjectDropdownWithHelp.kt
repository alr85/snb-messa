package com.example.mecca.formModules

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.mecca.formModules.inputs.SimpleDropdown

@Composable
fun <T> LabeledObjectDropdownWithHelp(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onSelectionChange: (T?) -> Unit,
    optionLabel: (T) -> String,
    helpText: String,
    placeholder: String = "Select...",
    isNAToggleEnabled: Boolean = false
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    // If NA is enabled, we represent it as a "fake" state:
    // selectedOption == null means "nothing selected", and "N/A" is stored via a sentinel label.
    val selectedLabel = selectedOption?.let(optionLabel) ?: ""
    val isNa = isNAToggleEnabled && selectedLabel == "N/A"

    FormRowWrapper(
        label = label,
        naButtonText = if (isNa) "Edit" else "N/A",
        isDisabled = false, // ALWAYS per your standard
        onNaClick = if (isNAToggleEnabled) {
            {
                // Toggle N/A:
                // We can’t store "N/A" inside T, so we clear selection.
                // If you truly need an NA state here, store it in the ViewModel as a String instead.
                onSelectionChange(null)
            }
        } else null,
        onHelpClick = { showHelpDialog = true }
    ) { _ ->
        // Map objects -> string labels for SimpleDropdown
        val stringOptions = options.map(optionLabel)

        SimpleDropdown(
            options = stringOptions,
            selectedOption = selectedOption?.let(optionLabel),
            onSelectionChange = { chosenLabel ->
                if (isNa) return@SimpleDropdown
                val chosen = options.firstOrNull { optionLabel(it) == chosenLabel }
                onSelectionChange(chosen)
            },
            isDisabled = isNa,
            placeholder = placeholder
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