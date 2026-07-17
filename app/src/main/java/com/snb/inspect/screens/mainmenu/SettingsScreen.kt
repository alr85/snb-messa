package com.snb.inspect.screens.mainmenu

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.core.content.FileProvider
import com.snb.inspect.PreferencesHelper
import com.snb.inspect.UserViewModel
import com.snb.inspect.ui.theme.SnbDarkGrey
import com.snb.inspect.ui.theme.SnbRed
import java.io.File
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }

    val toolItems = remember {
        listOf(
            SettingItem("Checkweigher Accuracy Test", Icons.Default.Calculate, "checkweigherAccuracy"),
            SettingItem("Checkweigher Speed Calculator", Icons.Default.Speed, "checkweigherSpeedCalculator"),
            SettingItem("M&S MD Sensitivities", Icons.AutoMirrored.Filled.Rule, "msSensitivities"),
            SettingItem("MD Failsafes", Icons.Default.Shield, "mdFailsafes")
        )
    }

    val serviceItems = remember {
        listOf(
            SettingItem("My Calibrations", Icons.Filled.EditNote, "myCalibrations"),
            SettingItem("My Validations", Icons.Filled.EditNote, "myValidations",isComingSoon = false),
            SettingItem("Service Calls", Icons.Default.Construction, "COMING_SOON", isComingSoon = true),
            SettingItem("Weekend Rota", Icons.Default.CalendarMonth, "weekendRota"),
        )
    }

    val documentationItems = remember {
        listOf(
            SettingItem("User Manuals", Icons.Default.Description, "userManualsList"),
            SettingItem("Codes of Practice", Icons.Default.Description, "codesOfPracticeList"),
            SettingItem("Passwords", Icons.Default.LockOpen, "codesOfPracticeList", isComingSoon = true)
        )
    }

    val usefulLinksItems = remember {
        listOf(
            SettingItem("Packaged Goods Law", Icons.Default.Language, "https://www.gov.uk/weights-measures-and-packaging-the-law/packaged-goods"),
            SettingItem("Weights & Measures Guidance", Icons.Default.Language, "https://www.gov.uk/weights-measures-and-packaging-the-law"),
            SettingItem("SNB Electronic Services LTD", Icons.Default.Language, "https://metaldetector-rentals.co.uk/")
        )
    }

    val appManagementItems = remember {
        listOf(
            SettingItem("Database Sync", Icons.Default.CloudSync, "databaseSync"),
            SettingItem("Export Database", Icons.Default.Share, "EXPORT_DB_TRIGGER"),
            SettingItem("Debug Logs", Icons.AutoMirrored.Filled.ListAlt, "logsScreen"),
            SettingItem("About App", Icons.Default.Info, "aboutApp"),
            SettingItem("Logout", Icons.Default.Refresh, "LOGOUT_TRIGGER")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // --- SERVICE SECTION ---
        item {
            SettingsHeader("Service")
        }
        items(serviceItems) { item ->
            SettingRow(item, navController, showDialog)
        }

        item { Spacer(Modifier.height(16.dp)) }

        // --- DOCUMENTATION SECTION ---
        item {
            SettingsHeader("Documentation")
        }
        items(documentationItems) { item ->
            SettingRow(item, navController, showDialog)
        }

        item { Spacer(Modifier.height(16.dp)) }

        // --- TOOLS SECTION ---
        item {
            SettingsHeader("Support Tools")
        }
        items(toolItems) { item ->
            SettingRow(item, navController, showDialog)
        }

        item { Spacer(Modifier.height(16.dp)) }

        // --- LINKS SECTION ---
        item {
            SettingsHeader("Useful Links")
        }
        items(usefulLinksItems) { item ->
            SettingRow(item, navController, showDialog)
        }

        item { Spacer(Modifier.height(16.dp)) }

        // --- MANAGEMENT SECTION ---
        item {
            SettingsHeader("App Management")
        }
        items(appManagementItems) { item ->
            SettingRow(item, navController, showDialog)
        }
    }

    if (showDialog.value) {
        LogoutConfirmationDialog(
            onConfirm = {
                userViewModel.loginStatus.value = false
                userViewModel.syncStatus.value = false
                PreferencesHelper.clearCredentials(context)
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
private fun SettingsHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = SnbRed,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun SettingRow(
    item: SettingItem,
    navController: NavHostController,
    showDialog: MutableState<Boolean>
) {
    val context = LocalContext.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !item.isComingSoon) {
                    when {
                        item.route == "LOGOUT_TRIGGER" -> showDialog.value = true
                        item.route == "EXPORT_DB_TRIGGER" -> exportDatabase(context)
                        item.route.startsWith("http") -> {
                            val intent = Intent(Intent.ACTION_VIEW, item.route.toUri())
                            context.startActivity(intent)
                        }
                        else -> navController.navigate(item.route)
                    }
                }
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (item.isComingSoon) Color.Gray else SnbDarkGrey
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (item.isComingSoon) Color.Gray else Color.Black
                )
            }

            if (item.isComingSoon) {
                Surface(
                    color = Color(0xFFEEEEEE),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "COMING SOON",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 56.dp, end = 16.dp)
                .height(1.dp)
                .background(Color(0xFFEEEEEE))
        )
    }
}

data class SettingItem(
    val name: String,
    val icon: ImageVector,
    val route: String,
    val isComingSoon: Boolean = false
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

fun exportDatabase(context: android.content.Context) {
    try {
        val dbName = "app_database"
        val dbFile = context.getDatabasePath(dbName)
        val shmFile = context.getDatabasePath("$dbName-shm")
        val walFile = context.getDatabasePath("$dbName-wal")

        val filesToShare = listOf(dbFile, shmFile, walFile).filter { it.exists() }

        if (filesToShare.isEmpty()) {
            Toast.makeText(context, "Database files not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Copy files to cache to make sharing easier/safer
        val cacheDir = File(context.cacheDir, "db_export")
        cacheDir.mkdirs()

        val uris = filesToShare.map { file ->
            val destFile = File(cacheDir, file.name + (if (file.name == dbName) ".db" else ""))
            file.inputStream().use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", destFile)
        }

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "application/octet-stream"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Export Database"))

    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}
