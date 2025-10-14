package com.example.mecca.formModules

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationHeader(
    label: String,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.headlineSmall,
        maxLines = 1,
    )

}



