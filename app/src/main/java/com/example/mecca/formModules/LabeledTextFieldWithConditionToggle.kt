package com.example.mecca.formModules

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow


@Composable
fun LabeledTextFieldWithConditionToggle(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    helpText: String,
    conditionLabel: String,
    currentCondition: ConditionState,
    onConditionChange: (ConditionState) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isNAToggleEnabled: Boolean = true,
    title: String
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    var isDisabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

        // First Row: Condition Label | Three-Way Toggle (Good/Satisfactory/Poor) | N/A Toggle | Help Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Condition label
            Text(
                text = conditionLabel,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(3f)
                    .padding(end = 8.dp)
            )

            // Three-way toggle (Good/Satisfactory/Poor)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(5f)
            ) {
                TextButton(
                    onClick = {
                        if (currentCondition != ConditionState.GOOD && !isDisabled) onConditionChange(ConditionState.GOOD)
                    },
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .border(
                            width = 2.dp,
                            color = if (currentCondition == ConditionState.GOOD) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(25)
                        )
                ) {
                    Text(
                        text = "Good",
                        color = if (currentCondition == ConditionState.GOOD) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }

                TextButton(
                    onClick = {
                        if (currentCondition != ConditionState.SATISFACTORY && !isDisabled) onConditionChange(ConditionState.SATISFACTORY)
                    },
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .border(
                            width = 2.dp,
                            color = if (currentCondition == ConditionState.SATISFACTORY) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(25)
                        )
                ) {
                    Text(
                        text = "Satisfactory",
                        color = if (currentCondition == ConditionState.SATISFACTORY) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }

                TextButton(
                    onClick = {
                        if (currentCondition != ConditionState.POOR && !isDisabled) onConditionChange(ConditionState.POOR)
                    },
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .border(
                            width = 2.dp,
                            color = if (currentCondition == ConditionState.POOR) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(25)
                        )
                ) {
                    Text(
                        text = "Poor",
                        color = if (currentCondition == ConditionState.POOR) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }

            if (isNAToggleEnabled) {
                // Button to toggle between "N/A" and re-enable the condition toggle
                TextButton(
                    onClick = {
                        if (isDisabled) {
                            onValueChange("") // Clear input
                            onConditionChange(ConditionState.UNSPECIFIED) // Set condition to default
                            isDisabled = false
                        } else {
                            onValueChange("N/A")
                            onConditionChange(ConditionState.NA) // Set condition to "N/A"
                            isDisabled = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isDisabled) "Edit" else "N/A")
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Help IconButton
            IconButton(
                onClick = { showHelpDialog = true },
                modifier = Modifier.weight(0.5f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = "Help for $label"
                )
            }
        }

        // Second Row: Label | User Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label for the text field
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(3f)
                    .padding(end = 8.dp)
            )

            // User Input Text Field
            OutlinedTextField(
                value = value,
                onValueChange = {
                    if (!isDisabled) {
                        onValueChange(it)
                    }
                },
                modifier = Modifier
                    .weight(5f)
                    .padding(end = 8.dp),
                singleLine = true,
                enabled = !isDisabled,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color.Gray,
                    disabledTextColor = Color.Gray,
                    disabledLabelColor = Color.Gray
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = keyboardType
                )
            )
            Spacer(modifier = Modifier.weight(1.5f))
        }
    }

    // Show Help Dialog if triggered
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(text = label) },
            text = { Text(text = helpText) },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
