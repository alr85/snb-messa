package com.example.mecca.formModules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
    value: String?,
    onValueChange: (String) -> Unit,
    helpText: String,
    isDisabled: Boolean = false
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    val options = listOf("Pass", "Fail", "Not Fitted", "N/A")

    FormRowWrapper(
        label = label,
        isDisabled = isDisabled,
        onNaClick = null,
        onHelpClick = { showHelpDialog = true }
    ) { disabled ->

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            // Row 1
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Pass", "Fail").forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = value == option,
                            onClick = { if (!disabled) onValueChange(option) },
                            enabled = !disabled
                        )
                        Text(
                            option,
                            modifier = Modifier.padding(start = 4.dp),
                            color = if (disabled) Color.Gray else Color.Unspecified
                        )
                    }
                }
            }

            // Row 2
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Not Fitted", "N/A").forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = value == option,
                            onClick = { if (!disabled) onValueChange(option) },
                            enabled = !disabled
                        )
                        Text(
                            option,
                            modifier = Modifier.padding(start = 4.dp),
                            color = if (disabled) Color.Gray else Color.Unspecified
                        )
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
