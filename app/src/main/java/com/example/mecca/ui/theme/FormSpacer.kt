package com.example.mecca.ui.theme

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FormSpacer(){
    Spacer(modifier = Modifier.height(6.dp))
    HorizontalDivider(thickness = 1.dp)
    Spacer(modifier = Modifier.height(2.dp))

}

