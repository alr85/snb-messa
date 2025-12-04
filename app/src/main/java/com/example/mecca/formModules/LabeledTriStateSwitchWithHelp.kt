package com.example.mecca.formModules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LabeledTriStateSwitchWithHelp(
    label: String,
    currentState: YesNoState = YesNoState.NO,
    onStateChange: (YesNoState) -> Unit,
    helpText: String,
    isNAToggleEnabled: Boolean = true
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    // Local mirror of state so the UI is responsive immediately
    var localState by remember {
        mutableStateOf(
            if (currentState == YesNoState.UNSPECIFIED) YesNoState.NO else currentState
        )
    }
    // Local disabled flag, just like your dropdown+text component
    var isDisabled by remember { mutableStateOf(currentState == YesNoState.NA) }

    // Keep locals in sync if parent changes state externally (restore, validation, etc.)
    LaunchedEffect(currentState) {
        isDisabled = currentState == YesNoState.NA
        localState = if (currentState == YesNoState.UNSPECIFIED) YesNoState.NO else currentState
    }

    FormRowWrapper(
        label = label,
        naButtonText = if (isDisabled) "Edit" else "N/A",
        isDisabled = isDisabled,
        onNaClick = if (isNAToggleEnabled) {
            {
                // Toggle NA <-> NO exactly like your other module toggles "N/A" <-> ""
                isDisabled = !isDisabled
                val newState = if (isDisabled) YesNoState.NA else YesNoState.NO
                localState = newState
                onStateChange(newState)
            }
        } else null,
        onHelpClick = { showHelpDialog = true }
    ) { disabledFromWrapper ->

        // Binary switch: YES/NO only. Disabled when NA.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Switch(
                checked = localState == YesNoState.YES,
                onCheckedChange = { checked ->
                    if (!disabledFromWrapper) {
                        val newState = if (checked) YesNoState.YES else YesNoState.NO
                        localState = newState
                        onStateChange(newState)
                    }
                },
                enabled = !disabledFromWrapper
            )

            Text(
                text = when (localState) {
                    YesNoState.YES -> "Yes"
                    YesNoState.NO -> "No"
                    YesNoState.NA -> "N/A"
                    YesNoState.UNSPECIFIED -> "Unspecified"
                },
                color = if (disabledFromWrapper) Color.Gray else Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
        }
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

