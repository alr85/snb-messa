package com.example.mecca.screens.mainmenu

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.AppChromeViewModel
import com.example.mecca.PreferencesHelper
import com.example.mecca.TopBarState
import com.example.mecca.UserViewModel
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }

    val settingsItems = remember {
        listOf(
            SettingItem("My Calibrations"),
            SettingItem("Database Sync"),
            SettingItem("Debug Logs"),
            SettingItem("About App"),
            SettingItem("Logout")
        )
    }


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),

            contentPadding = PaddingValues(
                top = 12.dp,
                bottom = 24.dp)

        ) {


            items(settingsItems) { setting ->
                SettingRow(
                    setting = setting,
                    navController = navController,
                    showDialog = showDialog
                )
            }
        }


    if (showDialog.value) {
        LogoutConfirmationDialog(
            onConfirm = {
                userViewModel.loginStatus.value = false
                userViewModel.syncStatus.value = false
                PreferencesHelper.clearCredentials(context)

                Log.d("LoginDebug", "Credentials cleared")

                (context as? ComponentActivity)?.apply {
                    finishAffinity()
                    exitProcess(0)
                }
            },
            onDismiss = { showDialog.value = false }
        )
    }
}


@Composable
fun SettingRow(
    setting: SettingItem,
    navController: NavHostController,
    showDialog: MutableState<Boolean>
) {
    val icon = when (setting.name) {
        "Database Sync" -> Icons.Default.CloudSync
        "About App" -> Icons.Default.Info
        "My Calibrations" -> Icons.Filled.EditNote
        "Debug Logs" -> Icons.AutoMirrored.Filled.ListAlt
        "Logout" -> Icons.Default.Refresh
        else -> Icons.Filled.Settings
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    when (setting.name) {
                        "Database Sync" -> navController.navigate("databaseSync")
                        "About App" -> navController.navigate("aboutApp")
                        "My Calibrations" -> navController.navigate("myCalibrations")
                        "Debug Logs" -> navController.navigate("logsScreen")
                        "Logout" -> showDialog.value = true
                        else -> navController.navigate("databaseSync")
                    }
                }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = setting.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Divider (use theme, not raw gray)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 56.dp, end = 16.dp) // inset so it aligns after the icon
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}


data class SettingItem(
    val name: String,
    val isSwitch: Boolean = false, // true for switch, false for regular click items
    val isChecked: Boolean = false // Used for switch settings
)

@Composable
fun LogoutConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Confirm Logout") },
        text = { Text("Are you sure you want to log out and close the app?") },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("No")
            }
        }
    )
}
