package com.example.mecca.formModules.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
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
import com.example.mecca.ui.theme.SnbRed
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdown(
    options: List<String>,
    selectedOption: String?,
    onSelectionChange: (String) -> Unit,
    isDisabled: Boolean,
    label: String = "Select an option",
    placeholder: String = "Select…"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (!isDisabled) expanded = !expanded
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = if (isDisabled) "N/A" else selectedOption.orEmpty(),
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
            modifier = Modifier.heightIn(max = 320.dp) // keeps it modern + manageable
        ) {
            val scrollState = rememberScrollState()
            var viewportHeightPx by remember { mutableIntStateOf(0) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .onSizeChanged { viewportHeightPx = it.height }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(end = 16.dp) // Space for scrollbar
                ) {
                    options.forEach { option ->
                        val isSelected = option == selectedOption

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = SnbRed
                                    )
                                }
                            },
                            onClick = {
                                onSelectionChange(option)
                                expanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }

                // Custom Scrollbar
                if (scrollState.maxValue > 0 && viewportHeightPx > 0) {
                    val density = LocalDensity.current
                    val maxValue = scrollState.maxValue.toFloat()
                    val totalContentHeight = (viewportHeightPx + maxValue)
                    
                    val thumbHeightPx = (viewportHeightPx.toFloat() / totalContentHeight) * viewportHeightPx.toFloat()
                    val minThumbPx = with(density) { 30.dp.toPx() }
                    val finalThumbHeightPx = thumbHeightPx.coerceAtLeast(minThumbPx)

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .width(16.dp)
                            .height(with(density) { viewportHeightPx.toDp() })
                            .background(Color.Black.copy(alpha = 0.05f))
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .width(4.dp)
                                .height(with(density) { finalThumbHeightPx.toDp() })
                                .offset {
                                    val scrollProgress = scrollState.value.toFloat() / maxValue
                                    val availableTrackHeight = viewportHeightPx - finalThumbHeightPx
                                    IntOffset(0, (scrollProgress * availableTrackHeight).roundToInt())
                                }
                                .background(SnbRed, RoundedCornerShape(50))
                        )
                    }
                }
            }
        }
    }
}
