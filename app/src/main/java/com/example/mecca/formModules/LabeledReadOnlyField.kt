package com.example.mecca.formModules

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

@Composable
fun LabeledReadOnlyField(
    label: String,
    value: String,
    helpText: String? = null
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    val fieldShape = RoundedCornerShape(14.dp)

    FormRowWrapper(
        label = label,
        onNaClick = null, // disables the N/A button
        onHelpClick = if (helpText != null) {
            { showHelpDialog = true }
        } else null
    ) {
        // Read-only field for the middle zone
        OutlinedTextField(
            value = value,
            onValueChange = {},
            singleLine = true,
            enabled = false,
            shape = fieldShape,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Gray,
                disabledTextColor = Color.Unspecified,
                disabledLabelColor = Color.Gray
            )
        )
    }

    // Help dialog (only shown if text provided)
    if (showHelpDialog && helpText != null) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(label) },
            text = { Text(helpText) },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

