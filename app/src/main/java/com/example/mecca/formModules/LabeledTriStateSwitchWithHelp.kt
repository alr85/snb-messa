package com.example.mecca.formModules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledTriStateSwitchWithHelp(
    label: String,
    currentState: YesNoState = YesNoState.NO,
    onStateChange: (YesNoState) -> Unit,
    helpText: String,
    isNAToggleEnabled: Boolean = true
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    // Local mirror so UI responds instantly
    var localState by remember {
        mutableStateOf(
            if (currentState == YesNoState.UNSPECIFIED) YesNoState.NO else currentState
        )
    }
    var isDisabled by remember { mutableStateOf(currentState == YesNoState.NA) }

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
                isDisabled = !isDisabled
                val newState = if (isDisabled) YesNoState.NA else YesNoState.NO
                localState = newState
                onStateChange(newState)
            }
        } else null,
        onHelpClick = { showHelpDialog = true }
    ) { disabledFromWrapper ->

        val enabled = !disabledFromWrapper

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Result",
                style = MaterialTheme.typography.labelMedium
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = listOf(
                    "Yes" to YesNoState.YES,
                    "No" to YesNoState.NO
                )

                options.forEachIndexed { index, (text, state) ->
                    val selected = localState == state

                    SegmentedButton(
                        selected = selected,
                        onClick = {
                            if (enabled) {
                                localState = state
                                onStateChange(state)
                            }
                        },
                        enabled = enabled,
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        icon = {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null
                                )
                            }
                        },
                        colors = SegmentedButtonDefaults.colors(
                            inactiveContainerColor = Color.White,
                            activeContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                            activeContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledInactiveContainerColor = Color.White,
                            disabledActiveContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(text)
                    }
                }
            }

            // Optional little state line (keeps behaviour similar to your old "Yes/No" text)
            if (localState == YesNoState.NA) {
                Text(
                    text = "N/A",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
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
