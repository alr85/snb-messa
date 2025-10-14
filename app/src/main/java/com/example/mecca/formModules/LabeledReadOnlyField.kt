package com.example.mecca.formModules

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun LabeledReadOnlyField(
    label: String,
    value: String,
    helpText: String? = null // Optional: if you want the same help dialog style
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label text
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(3f)
                .padding(end = 8.dp)
        )

        // OutlinedTextField styled the same, but read-only
        OutlinedTextField(
            value = value,
            onValueChange = {}, // no-op
            modifier = Modifier
                .weight(6f)
                .padding(end = 8.dp),
            singleLine = true,
            enabled = false, // disables editing
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Gray,
                disabledTextColor = Color.Unspecified,
                disabledLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        // Optional help button if you want it
        if (helpText != null) {
            IconButton(
                onClick = { showHelpDialog = true },
                modifier = Modifier.weight(0.5f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = "Help for $label"
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(0.5f))
        }
    }

    if (showHelpDialog && helpText != null) {
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
