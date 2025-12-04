package com.example.mecca.formModules.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.room.parser.Section

@Composable
fun SimpleTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isDisabled: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (!isDisabled) onValueChange(it) },
        label = { Section.Text(label) },
        singleLine = true,
        enabled = !isDisabled,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = Color.Gray,
            disabledTextColor = Color.Gray,
            disabledLabelColor = Color.Gray
        )
    )
}

