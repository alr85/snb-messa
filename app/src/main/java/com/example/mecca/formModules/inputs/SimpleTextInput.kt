package com.example.mecca.formModules.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.room.parser.Section
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

@Composable
fun SimpleTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isDisabled: Boolean = false
) {

    val fieldShape = RoundedCornerShape(14.dp)

    OutlinedTextField(
        value = value,
        onValueChange = { if (!isDisabled) onValueChange(it) },
        label = { Section.Text(label) },
        singleLine = true,
        enabled = !isDisabled,
        shape = fieldShape,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType,capitalization = KeyboardCapitalization.Sentences),
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
}

