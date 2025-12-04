package com.example.mecca.formModules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledCheckboxWithTextFieldAndHelp(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean?) -> Unit,
    textValue: String,
    onTextValueChange: (String) -> Unit,
    helpText: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    // Derive disabled from the stored value (single source of truth)
    fun isSentinel(v: String): Boolean =
        if (keyboardType == KeyboardType.Number) v == "0" else v == "N/A"

    var isDisabled by remember { mutableStateOf(isSentinel(textValue)) }
    LaunchedEffect(textValue, keyboardType) {
        isDisabled = isSentinel(textValue)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Label + checkbox
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(3f)) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onCheckedChange(it) },
                colors = CheckboxDefaults.colors(checkedColor = Color.Red, uncheckedColor = Color.Gray)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Text field
        OutlinedTextField(
            value = textValue,
            onValueChange = { raw ->
                if (!isDisabled) {
                    var cleaned = raw
                    if (keyboardType == KeyboardType.Number) {
                        cleaned = cleaned.replace(',', '.')
                    } else {
                        cleaned = cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }
                    onTextValueChange(cleaned)
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
                keyboardType = keyboardType,
                capitalization = if (keyboardType == KeyboardType.Text)
                    KeyboardCapitalization.Sentences else KeyboardCapitalization.None
            )
        )

        // N/A / Edit toggle
        TextButton(
            onClick = {
                val next = !isDisabled
                isDisabled = next
                if (next) {
                    // set sentinel and lock
                    onTextValueChange(if (keyboardType == KeyboardType.Number) "0" else "N/A")
                } else {
                    // clear and unlock
                    onTextValueChange("")
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Text(if (isDisabled) "Edit" else "N/A")
        }

        // Help
        IconButton(onClick = { showHelpDialog = true }, modifier = Modifier.weight(0.5f)) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = "Help for $label"
            )
        }
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
