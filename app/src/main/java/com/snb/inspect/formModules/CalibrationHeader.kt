package com.snb.inspect.formModules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel

val LocalCalibrationViewModel = staticCompositionLocalOf<CalibrationMetalDetectorConveyorViewModel?> { null }
val LocalCalibrationNavController = staticCompositionLocalOf<NavController?> { null }
val LocalCalibrationRouteOrder = staticCompositionLocalOf<List<String>> { emptyList() }
val LocalCalibrationCurrentRoute = staticCompositionLocalOf<String?> { null }

@Composable
fun CalibrationHeader(
    label: String,
    isValid: Boolean = true
) {
    val viewModel = LocalCalibrationViewModel.current
    val navController = LocalCalibrationNavController.current
    val routeOrder = LocalCalibrationRouteOrder.current
    val currentRoute = LocalCalibrationCurrentRoute.current

    var expanded by remember { mutableStateOf(false) }

    val screenValidities by if (viewModel != null) {
        viewModel.screenValidities.collectAsState()
    } else {
        remember { mutableStateOf(emptyMap<String, Boolean>()) }
    }

    LaunchedEffect(isValid, currentRoute) {
        if (viewModel != null && currentRoute != null) {
            viewModel.setScreenValidity(currentRoute, isValid)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (viewModel != null && navController != null && routeOrder.isNotEmpty() && currentRoute != null) {
            // Centered Navigation Trigger
            Row(
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = "Navigate",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Far-Right Status Indicator
            Icon(
                imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = if (isValid) "Valid" else "Incomplete",
                tint = if (isValid) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.CenterEnd)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val isAllValid = viewModel.isCalibrationValid(routeOrder)

                routeOrder.forEach { r ->
                    val isSummary = r.contains("Summary")
                    if (isSummary && !isAllValid) return@forEach

                    val baseRoute = r.split("/").first()
                    val isScreenValid = screenValidities[baseRoute] == true
                    val displayName = viewModel.getDisplayNameForRoute(r)

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isScreenValid) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = if (isScreenValid) "Valid" else "Incomplete",
                                    tint = if (isScreenValid) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = displayName)
                            }
                        },
                        onClick = {
                            expanded = false
                            viewModel.persistCurrentScreen(currentRoute)
                            navController.navigate(r)
                        }
                    )
                }
            }
        } else {
            // Simple version (fallback)
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.align(Alignment.Center)
            )
            
            Icon(
                imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = if (isValid) "Valid" else "Incomplete",
                tint = if (isValid) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.CenterEnd)
            )
        }
    }
}
