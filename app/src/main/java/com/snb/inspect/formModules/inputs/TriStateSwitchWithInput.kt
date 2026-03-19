package com.snb.inspect.formModules.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.formModules.YesNoState
import com.snb.inspect.ui.theme.FormInputDisabledBorderColor
import com.snb.inspect.ui.theme.FormInputDisabledContainerColor
import com.snb.inspect.ui.theme.FormInputDisabledLabelColor
import com.snb.inspect.ui.theme.FormInputDisabledPlaceholderColor
import com.snb.inspect.ui.theme.FormInputDisabledTextColor
import com.snb.inspect.ui.theme.FormInputFocusedBorderColor
import com.snb.inspect.ui.theme.FormInputFocusedContainerColor
import com.snb.inspect.ui.theme.FormInputFocusedLabelColor
import com.snb.inspect.ui.theme.FormInputFocusedPlaceholderColor
import com.snb.inspect.ui.theme.FormInputFocusedTextColor
import com.snb.inspect.ui.theme.FormInputUnfocusedBorderColor
import com.snb.inspect.ui.theme.FormInputUnfocusedContainerColor
import com.snb.inspect.ui.theme.FormInputUnfocusedLabelColor
import com.snb.inspect.ui.theme.FormInputUnfocusedPlaceholderColor
import com.snb.inspect.ui.theme.FormInputUnfocusedTextColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriStateSwitchWithInput(
    currentState: YesNoState = YesNoState.UNSPECIFIED,
    onStateChange: (YesNoState) -> Unit,
    inputLabel: String,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    inputKeyboardType: KeyboardType = KeyboardType.Text,
    isDisabled: Boolean
) {
    val fieldShape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // YES / NO segmented control
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.weight(0.8f)
        ) {
            val items = listOf(
                "Yes" to YesNoState.YES,
                "No" to YesNoState.NO
            )

            items.forEach { (label, state) ->
                SegmentedButton(
                    selected = currentState == state,
                    onClick = {
                        if (!isDisabled) onStateChange(state)
                    },
                    enabled = !isDisabled,
                    shape = fieldShape,

                ) {
                    Text(label)
                }
            }
        }

        // Input field
        OutlinedTextField(
            value = inputValue,
            onValueChange = { raw ->
                if (!isDisabled) {
                    var cleaned = raw.replace(',', '.')
                    if (inputKeyboardType == KeyboardType.Text) {
                        cleaned = cleaned.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }
                    }
                    onInputValueChange(cleaned)
                }
            },
            label = { Text(inputLabel) },
            enabled = !isDisabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = inputKeyboardType,
                capitalization = if (inputKeyboardType == KeyboardType.Text)
                    KeyboardCapitalization.Sentences
                else
                    KeyboardCapitalization.None
            ),
            modifier = Modifier.weight(1.2f),
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
    }
}

