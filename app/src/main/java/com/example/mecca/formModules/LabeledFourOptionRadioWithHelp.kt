package com.example.mecca.formModules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledFourOptionRadioWithHelp(
    label: String,
    value: String?,                     // "Pass", "Fail", "N/A", "Not Fitted"
    onValueChange: (String) -> Unit,
    helpText: String,
    isDisabled: Boolean = false,
    showNotFittedOption: Boolean = true,
    notFittedEnabled: Boolean = true
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    val baseOptions = listOf("Pass", "Fail", "N/A")
    val options = remember(showNotFittedOption) {
        if (showNotFittedOption) baseOptions + "Not Fitted" else baseOptions
    }

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

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEachIndexed { index, option ->

                    val optionEnabled =
                        !disabled && (option != "Not Fitted" || notFittedEnabled)

                    SegmentedButton(
                        selected = value == option,
                        onClick = { if (optionEnabled) onValueChange(option) },
                        enabled = optionEnabled,
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        colors = SegmentedButtonDefaults.colors(
                            // match your new rule: unselected = white
                            inactiveContainerColor = Color.White,
                            activeContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                            activeContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledInactiveContainerColor = Color.White,
                            disabledActiveContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(option, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
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


