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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledRadioButtonWithHelp(
    label: String,
    value: Boolean?,                // true = Yes, false = No, null = unset
    onValueChange: (Boolean) -> Unit,
    helpText: String,
    isDisabled: Boolean = false
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    FormRowWrapper(
        label = label,
        isDisabled = isDisabled,
        onNaClick = null,
        onHelpClick = { showHelpDialog = true }
    ) { disabled ->

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Result",
                style = MaterialTheme.typography.labelMedium
            )

            val options = listOf(
                "Yes" to true,
                "No" to false
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, (text, boolValue) ->
                    val selected = value == boolValue

                    SegmentedButton(
                        selected = selected,
                        onClick = { if (!disabled) onValueChange(boolValue) },
                        enabled = !disabled,
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

            if (value == null) {
                Text(
                    text = "Select Yes or No to continue.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(text = label) },
            text = { Text(helpText) },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) { Text("OK") }
            }
        )
    }
}

