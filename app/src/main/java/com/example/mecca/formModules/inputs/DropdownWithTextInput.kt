package com.example.mecca.formModules.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.ui.theme.FormInputDisabledBorderColor
import com.example.mecca.ui.theme.FormInputDisabledContainerColor
import com.example.mecca.ui.theme.FormInputDisabledLabelColor
import com.example.mecca.ui.theme.FormInputDisabledPlaceholderColor
import com.example.mecca.ui.theme.FormInputDisabledTextColor
import com.example.mecca.ui.theme.FormInputFocusedBorderColor
import com.example.mecca.ui.theme.FormInputFocusedContainerColor
import com.example.mecca.ui.theme.FormInputFocusedLabelColor
import com.example.mecca.ui.theme.FormInputFocusedPlaceholderColor
import com.example.mecca.ui.theme.FormInputFocusedTextColor
import com.example.mecca.ui.theme.FormInputUnfocusedBorderColor
import com.example.mecca.ui.theme.FormInputUnfocusedContainerColor
import com.example.mecca.ui.theme.FormInputUnfocusedLabelColor
import com.example.mecca.ui.theme.FormInputUnfocusedPlaceholderColor
import com.example.mecca.ui.theme.FormInputUnfocusedTextColor

@OptIn(ExperimentalMaterial3Api::class)
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

    val fieldShape = RoundedCornerShape(14.dp)

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
            modifier = Modifier.weight(1f),
            shape = fieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FormInputFocusedContainerColor,
                unfocusedContainerColor = FormInputUnfocusedContainerColor,
                disabledContainerColor = FormInputDisabledContainerColor,

                focusedBorderColor = FormInputFocusedBorderColor,
                unfocusedBorderColor = FormInputUnfocusedBorderColor,
                disabledBorderColor = FormInputDisabledBorderColor,

                focusedTextColor = FormInputFocusedTextColor,
                unfocusedTextColor = FormInputUnfocusedTextColor,
                disabledTextColor = FormInputDisabledTextColor,

                focusedLabelColor = FormInputFocusedLabelColor,
                unfocusedLabelColor = FormInputUnfocusedLabelColor,
                disabledLabelColor = FormInputDisabledLabelColor,

                focusedPlaceholderColor = FormInputFocusedPlaceholderColor,
                unfocusedPlaceholderColor = FormInputUnfocusedPlaceholderColor,
                disabledPlaceholderColor = FormInputDisabledPlaceholderColor
            )
        )

        // Dropdown (Material3 Exposed)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (!isDisabled) expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = if (isDisabled) "N/A" else selectedOption,
                onValueChange = {},
                readOnly = true,
                enabled = !isDisabled,
                label = { Text(dropdownLabel) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                        enabled = !isDisabled
                    ),
                shape = fieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FormInputFocusedContainerColor,
                    unfocusedContainerColor = FormInputUnfocusedContainerColor,
                    disabledContainerColor = FormInputDisabledContainerColor,

                    focusedBorderColor = FormInputFocusedBorderColor,
                    unfocusedBorderColor = FormInputUnfocusedBorderColor,
                    disabledBorderColor = FormInputDisabledBorderColor,

                    focusedTextColor = FormInputFocusedTextColor,
                    unfocusedTextColor = FormInputUnfocusedTextColor,
                    disabledTextColor = FormInputDisabledTextColor,

                    focusedLabelColor = FormInputFocusedLabelColor,
                    unfocusedLabelColor = FormInputUnfocusedLabelColor,
                    disabledLabelColor = FormInputDisabledLabelColor,

                    focusedPlaceholderColor = FormInputFocusedPlaceholderColor,
                    unfocusedPlaceholderColor = FormInputUnfocusedPlaceholderColor,
                    disabledPlaceholderColor = FormInputDisabledPlaceholderColor
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 320.dp)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            onOptionChange(option)
                            expanded = false
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
