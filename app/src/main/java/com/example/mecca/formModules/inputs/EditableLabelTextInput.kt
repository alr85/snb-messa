package com.example.mecca.formModules.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun EditableLabelTextInput(
    label: String,
    onLabelChange: (String) -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isDisabled: Boolean
) {
    var isEditingLabel by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Editable or static label
        if (isEditingLabel) {
            OutlinedTextField(
                value = label,
                onValueChange = onLabelChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { isEditingLabel = false }),
                placeholder = { Text("Enter label") },
                modifier = Modifier.weight(0.8f)
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                ),
                modifier = Modifier
                    .weight(0.8f)
                    .clickable { isEditingLabel = true },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Main text input
        OutlinedTextField(
            value = value,
            onValueChange = { if (!isDisabled) onValueChange(it) },
            singleLine = true,
            enabled = !isDisabled,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Gray,
                disabledTextColor = Color.Gray,
                disabledLabelColor = Color.Gray
            ),
            modifier = Modifier.weight(1f)
        )
    }
}
