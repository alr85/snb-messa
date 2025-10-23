package com.example.mecca.formModules.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp

@Composable
fun DropdownWithTextInput(
    dropdownLabel: String,
    options: List<String>,
    selectedOption: String,
    onOptionChange: (String) -> Unit,
    inputLabel: String,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    inputKeyboardType: KeyboardType = KeyboardType.Text,
    isDisabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Text input
        OutlinedTextField(
            value = inputValue,
            onValueChange = { if (!isDisabled) onInputValueChange(it) },
            label = { Text(inputLabel) },
            singleLine = true,
            enabled = !isDisabled,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = inputKeyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Gray,
                disabledTextColor = Color.Gray,
                disabledLabelColor = Color.Gray
            ),
            modifier = Modifier.weight(1f)
        )

        // Dropdown
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = if (isDisabled) "N/A" else selectedOption,
                onValueChange = {},
                readOnly = true,
                label = { Text(dropdownLabel) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown Arrow",
                        modifier = Modifier.clickable {
                            if (!isDisabled) expanded = !expanded
                        }
                    )
                },
                enabled = !isDisabled,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color.Gray,
                    disabledTextColor = Color.Gray,
                    disabledLabelColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
