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
import androidx.compose.ui.text.input.KeyboardType
import com.snb.inspect.formModules.inputs.TwoTextInputs


@Composable
fun LabeledTwoTextInputsWithHelp(
    label: String,
    firstInputLabel: String,
    firstInputValue: String,
    onFirstInputValueChange: (String) -> Unit,
    secondInputLabel: String,
    secondInputValue: String,
    onSecondInputValueChange: (String) -> Unit,
    helpText: String,
    firstInputKeyboardType: KeyboardType = KeyboardType.Text,
    secondInputKeyboardType: KeyboardType = KeyboardType.Text,
    isNAToggleEnabled: Boolean = true,
    firstMaxLength: Int? = null,
    secondMaxLength: Int? = null,
    firstTransform: ((String) -> String)? = null,
    secondTransform: ((String) -> String)? = null,
    pvStatus: String? = null,
    pvRules: List<PvRule> = emptyList(),
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    // Disabled iff BOTH values are "N/A" (your N/A toggle sets both)
    var isDisabled by remember {
        mutableStateOf(firstInputValue == "N/A" && secondInputValue == "N/A")
    }
    LaunchedEffect(firstInputValue, secondInputValue) {
        isDisabled = firstInputValue == "N/A" && secondInputValue == "N/A"
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
                val sentinel = if (next) "N/A" else ""
                onFirstInputValueChange(sentinel)
                onSecondInputValueChange(sentinel)
            }
        } else null,
        onHelpClick = { showHelpDialog = true }
    ) { _ ->
        TwoTextInputs(
            firstLabel = firstInputLabel,
            firstValue = firstInputValue,
            onFirstChange = { v ->
                if (!isDisabled) onFirstInputValueChange(v)
            },
            secondLabel = secondInputLabel,
            secondValue = secondInputValue,
            onSecondChange = { v ->
                if (!isDisabled) onSecondInputValueChange(v)
            },
            firstKeyboard = firstInputKeyboardType,
            secondKeyboard = secondInputKeyboardType,
            firstMaxLength = firstMaxLength,
            secondMaxLength = secondMaxLength,

            firstTransform = firstTransform,
            secondTransform = secondTransform,
            isDisabled = isDisabled
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(text = label) },
            text = { Text(text = helpText) },
            confirmButton = { TextButton(onClick = { showHelpDialog = false }) { Text("OK") } }
        )
    }
}
