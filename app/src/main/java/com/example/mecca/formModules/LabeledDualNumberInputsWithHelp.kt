package com.example.mecca.formModules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun LabeledDualNumberInputsWithHelp(
    label: String,
    firstLabel: String,
    firstValue: String,
    onFirstValueChange: (String) -> Unit,
    secondLabel: String,
    secondValue: String,
    onSecondValueChange: (String) -> Unit,
    helpText: String
) {
    FormRowWrapper(
        label = label,
        isDisabled = false,
        onNaClick = null,
        onHelpClick = {
            // This module relies on help being a dialog in your other style,
            // but FormRowWrapper only gives a click callback.
            // If you want a dialog here too, mirror your LabeledTextFieldWithHelp pattern.
        }
    ) { disabled ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = firstValue,
                onValueChange = { if (!disabled) onFirstValueChange(it.filter(Char::isDigit)) },
                label = { Text(firstLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Red,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.DarkGray,
                    unfocusedLabelColor = Color.Gray,
                )
            )

            Spacer(Modifier.width(8.dp))
            Text("x")
            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = secondValue,
                onValueChange = { if (!disabled) onSecondValueChange(it.filter(Char::isDigit)) },
                label = { Text(secondLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Red,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.DarkGray,
                    unfocusedLabelColor = Color.Gray,
                )
            )
        }
    }
}
