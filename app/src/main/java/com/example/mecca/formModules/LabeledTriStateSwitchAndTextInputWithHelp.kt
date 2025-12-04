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
import com.example.mecca.formModules.inputs.TriStateSwitchWithInput



@Composable
fun LabeledTriStateSwitchAndTextInputWithHelp(
    label: String,
    currentState: YesNoState = YesNoState.NO,
    onStateChange: (YesNoState) -> Unit,
    helpText: String,
    inputLabel: String,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    inputKeyboardType: KeyboardType = KeyboardType.Text
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    var isDisabled by remember { mutableStateOf(currentState == YesNoState.NA) }
    var localCurrentState by remember {
        mutableStateOf(if (currentState == YesNoState.UNSPECIFIED) YesNoState.NO else currentState)
    }

    LaunchedEffect(currentState) {
        isDisabled = currentState == YesNoState.NA
        localCurrentState = if (currentState == YesNoState.UNSPECIFIED) YesNoState.NO else currentState
    }

    FormRowWrapper(
        label = label,
        naButtonText = if (isDisabled) "Edit" else "N/A",
        isDisabled = isDisabled,
        onNaClick = {
            isDisabled = !isDisabled
            val newState = if (isDisabled) YesNoState.NA else YesNoState.NO
            localCurrentState = newState
            onStateChange(newState)
            onInputValueChange(if (isDisabled) "N/A" else "")
        },
        onHelpClick = { showHelpDialog = true }
    ) { disabled ->
        TriStateSwitchWithInput(
            currentState = localCurrentState,               // single source of truth for child
            onStateChange = { new ->
                localCurrentState = new                    // update row-local
                onStateChange(new)                         // then notify VM
            },
            inputLabel = inputLabel,
            inputValue = inputValue,
            onInputValueChange = onInputValueChange,
            inputKeyboardType = inputKeyboardType,
            isDisabled = disabled
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
