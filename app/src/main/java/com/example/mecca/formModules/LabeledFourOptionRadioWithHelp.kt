package com.example.mecca.formModules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledFourOptionRadioWithHelp(
    label: String,
    value: String?,                     // "Pass", "Fail", "N/A", "N/F"
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

        // 2x2 grid for four, or just 3 options if N/F hidden
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowOptions.forEach { option ->

                        val optionEnabled =
                            !disabled &&
                                    (option != "Not Fitted" || notFittedEnabled)

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = value == option,
                                onClick = { if (optionEnabled) onValueChange(option) },
                                enabled = optionEnabled
                            )

                            val textColor =
                                if (optionEnabled) Color.Unspecified else Color.Gray

                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp),
                                color = textColor
                            )
                        }
                    }

                    // keep layout stable if odd count (3 options)
                    if (rowOptions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
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

