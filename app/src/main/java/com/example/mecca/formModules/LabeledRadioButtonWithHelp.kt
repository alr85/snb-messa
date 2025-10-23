package com.example.mecca.formModules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledRadioButtonWithHelp(
    label: String,
    value: Boolean?, // true = Yes, false = No, null = unset
    onValueChange: (Boolean) -> Unit,
    helpText: String
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    FormRowWrapper(
        label = label,
        onNaClick = null, // no N/A button for radio inputs
        onHelpClick = { showHelpDialog = true }
    ) { _ ->

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Yes option
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = value == true,
                    onClick = { onValueChange(true) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color.Red,
                        unselectedColor = Color.Gray
                    )
                )
                Text(
                    text = "Yes",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // No option
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = value == false,
                    onClick = { onValueChange(false) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color.Red,
                        unselectedColor = Color.Gray
                    )
                )
                Text(
                    text = "No",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(text = label) },
            text = { Text(helpText) },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun LabeledRadioButtonWithHelp(
//    label: String,
//    value: Boolean?, // Use nullable Boolean to track Yes/No or if unselected
//    onValueChange: (Boolean) -> Unit,
//    helpText: String
//) {
//    var showHelpDialog by remember { mutableStateOf(false) }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        // Label text takes up more space
//        Text(
//            text = label,
//            style = MaterialTheme.typography.labelLarge,
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis,
//            modifier = Modifier
//                .weight(3f)
//                .padding(end = 8.dp)
//        )
//
//        // Yes Radio Button
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.weight(3f)
//        ) {
//            RadioButton(
//                selected = value == true,
//                onClick = { onValueChange(true) },
//                colors = RadioButtonDefaults.colors(
//                    selectedColor = Color.Red,
//                    unselectedColor = Color.Gray
//                )
//            )
//            Text(
//                text = "Yes",
//                style = MaterialTheme.typography.bodyMedium,
//                modifier = Modifier.padding(start = 4.dp)
//            )
//        }
//
//        // No Radio Button
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.weight(3f)
//        ) {
//            RadioButton(
//                selected = value == false,
//                onClick = { onValueChange(false) },
//                colors = RadioButtonDefaults.colors(
//                    selectedColor = Color.Red,
//                    unselectedColor = Color.Gray
//                )
//            )
//            Text(
//                text = "No",
//                style = MaterialTheme.typography.bodyMedium,
//                modifier = Modifier.padding(start = 4.dp)
//            )
//        }
//
//        // Help IconButton
//        IconButton(
//            onClick = { showHelpDialog = true },
//            modifier = Modifier.weight(0.5f)
//        ) {
//            Icon(
//                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
//                contentDescription = "Help for $label"
//            )
//        }
//    }
//
//    // Show Help Dialog
//    if (showHelpDialog) {
//        AlertDialog(
//            onDismissRequest = { showHelpDialog = false },
//            title = { Text(text = label) },
//            text = { Text(text = helpText) },
//            confirmButton = {
//                TextButton(onClick = { showHelpDialog = false }) {
//                    Text("OK")
//                }
//            }
//        )
//    }
//}
