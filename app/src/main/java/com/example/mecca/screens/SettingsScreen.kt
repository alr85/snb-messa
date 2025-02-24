import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mecca.PreferencesHelper
import com.example.mecca.UserViewModel
import kotlin.system.exitProcess

@Composable
fun SettingsScreen(navController: NavHostController, userViewModel: UserViewModel) {

    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }

    // Define the settings list inside the composable

    val settingsItems = listOf(
        SettingItem("My Calibrations"),
        SettingItem("Database Sync"),
        SettingItem("About App"),
        SettingItem("Logout") // Add Logout button
        //SettingItem("Another switch", isSwitch = true, isChecked = false),
        //SettingItem("Account settings"),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Set the entire screen background to white
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Menu",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                style = TextStyle(
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            )

            LazyColumn {
                items(settingsItems) { setting ->
                    SettingRow(setting, navController, showDialog)
                }
            }
        }
    }


    if (showDialog.value) {
        LogoutConfirmationDialog(
            onConfirm = {
                // Clear credentials and close the app
                userViewModel.loginStatus.value = false // Reset login state
                userViewModel.syncStatus.value = false // Optionally reset sync state
                PreferencesHelper.clearCredentials(context)

                Log.d("LoginDebug", "Credentials cleared")
                
                (context as? ComponentActivity)?.apply {
                    finishAffinity() // Close all activities
                    exitProcess(0)   // Fully exit the app
                }
            },
            onDismiss = { showDialog.value = false }
        )
    }
}

@Composable
fun SettingRow(setting: SettingItem, navController: NavHostController, showDialog: MutableState<Boolean>) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Navigate to different screens based on setting name
                    when (setting.name) {
                        "Database Sync" -> navController.navigate("databaseSync")
                        "About App" -> navController.navigate("aboutApp")
                        "My Calibrations" -> navController.navigate("myCalibrations")
                        "Logout" -> {
                            // Show confirmation dialog for logout
                            showDialog.value = true
                        }
                        else -> navController.navigate("databaseSync") // fallback
                    }
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add an icon to the left
            Icon(
                imageVector = when (setting.name) {
                    "Database Sync" -> Icons.Default.CloudSync
                    "Account settings" -> Icons.Filled.Person
                    "About App" -> Icons.Default.Info
                    "My Calibrations" -> Icons.Filled.EditNote
                    "Logout" -> Icons.Default.Refresh
                    else -> Icons.Filled.Settings
                },
                contentDescription = "${setting.name} icon",
                modifier = Modifier.size(24.dp), // Adjust icon size
                tint = Color.Black // Adjust icon color
            )

            Spacer(modifier = Modifier.width(16.dp)) // Space between icon and text

            // Display the setting name
            Text(
                text = setting.name,
                style = TextStyle(fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.weight(1f)) // Push chevron to the right

            // Add a chevron to the right
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Chevron",
                modifier = Modifier.size(24.dp),
                tint = Color.Black // Adjust icon color
            )
        }

        // Divider with inset
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // Insets for the divider
                .height(1.dp)
                .background(Color.Gray) // Divider color
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
    androidx.compose.material.AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Confirm Logout") },
        text = { Text("Are you sure you want to log out and close the app?") },
        confirmButton = {
            androidx.compose.material.TextButton(onClick = { onConfirm() }) {
                Text("Yes")
            }
        },
        dismissButton = {
            androidx.compose.material.TextButton(onClick = { onDismiss() }) {
                Text("No")
            }
        }
    )
}
