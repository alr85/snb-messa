package com.example.mecca.formModules.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
fun MultiSelectDropdown(
    value: String,
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    isDisabled: Boolean,
    label: String = "Select options",
    placeholder: String = "Select…"
) {
    var expanded by remember { mutableStateOf(false) }

    // What to show in the field:
    // - If you pass a non-blank value, use it (lets you format externally)
    // - Else join selected options
    val displayText = when {
        isDisabled -> "N/A"
        value.isNotBlank() -> value
        selectedOptions.isNotEmpty() -> selectedOptions.joinToString(" | ")
        else -> ""
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (!isDisabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            enabled = !isDisabled,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                    enabled = !isDisabled
                ),
            shape = RoundedCornerShape(14.dp),
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
            modifier = Modifier.heightIn(max = 360.dp)
        ) {
            options.forEach { option ->
                val isSelected = option in selectedOptions

                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Modern, compact selection indicator
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null
                                )
                            } else {
                                // keep alignment consistent when not selected
                                Spacer(Modifier.size(24.dp))
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    },
                    onClick = {
                        val newSelection =
                            if (isSelected) selectedOptions - option else selectedOptions + option
                        onSelectionChange(newSelection)
                        // IMPORTANT: do NOT close menu on each click (multi-select UX)
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            // Optional: Clear / Done row at the bottom (feels very modern)
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onSelectionChange(emptyList()) },
                    enabled = !isDisabled && selectedOptions.isNotEmpty()
                ) {
                    Text("Clear")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { expanded = false },
                    enabled = !isDisabled
                ) {
                    Text("Done")
                }
            }
        }
    }
}
