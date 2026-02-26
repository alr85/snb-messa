package com.example.mecca.formModules.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
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

@Composable
fun SimpleTextInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    isDisabled: Boolean = false,
    maxLength: Int? = null,
    singleLine: Boolean = true,
    transformInput: ((String) -> String)? = null,
    showCounter: Boolean = true, // allow turning off counter if you ever want
    minLines: Int = 1,

    ) {
    val fieldShape = RoundedCornerShape(14.dp)

    val capitalization =
        if (keyboardType == KeyboardType.Text) KeyboardCapitalization.Sentences
        else KeyboardCapitalization.None



    OutlinedTextField(
        value = value,
        onValueChange = { incoming ->
            if (isDisabled) return@OutlinedTextField

            // 1) Optional transform (sanitize / formatting)
            var processed = transformInput?.invoke(incoming) ?: incoming

            // 2) Optional max length clamp
            if (maxLength != null) {
                processed = processed.take(maxLength)
            }

            onValueChange(processed)
        },

        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        enabled = !isDisabled,
        shape = fieldShape,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType,
            capitalization = capitalization
        ),
        supportingText = {
            if (showCounter && maxLength != null) {
                val remaining = maxLength - value.length
                val color =
                    if (remaining < 1) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant

                Text(
                    text = "${value.length} / $maxLength",
                    color = color
                )
            }
        },
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
