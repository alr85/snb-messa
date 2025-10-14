package com.example.mecca

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.mecca.Repositories.UserRepository
import com.example.mecca.screens.LoginScreen
import com.example.mecca.ui.theme.AppNavGraph


class MainActivity : ComponentActivity() {
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MyApplication
        val db = app.database
        val apiService = app.apiService
        val userRepository = UserRepository(db.userDao(), apiService)
        userViewModel = UserViewModel(userRepository)

        val savedCredentials = PreferencesHelper.getCredentials(this)
        val savedUsername = savedCredentials.first
        val savedPassword = savedCredentials.second

        setContent {
            val syncStatus by userViewModel.syncStatus.collectAsState()
            val loginStatus by userViewModel.loginStatus.collectAsState()

            when {
                !syncStatus -> {
                    // Show syncing message
                    Text(
                        text = "Syncing users... Please wait.",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(16.dp),
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
                loginStatus -> {
                    // Navigate to main app if logged in
                    MyApp(db, userViewModel)
                }
                else -> {
                    // Show login screen
                    LoginScreen(
                        userViewModel = userViewModel,
                        defaultUsername = savedUsername,
                        defaultPassword = savedPassword,
                        loginError = userViewModel.loginError.collectAsState().value,
                        onLoginClick = { username, password ->
                            userViewModel.login(this, username, password)
                        }
                    )
                }
            }
        }

        // Start syncing users when the app launches
        userViewModel.syncUsers()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(db: AppDatabase, userViewModel: UserViewModel) {
    val navController = rememberNavController()

    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
    var showBottomBar by rememberSaveable { mutableStateOf(true) }

    // Navigation bar items
    val items = listOf(
        NavigationBarItem(
            title = "Diary",
            selectedIcon = Icons.Filled.DateRange,
            unselectedIcon = Icons.Default.DateRange
        ),
        NavigationBarItem(
            title = "Service",
            selectedIcon = Icons.Filled.Build,
            unselectedIcon = Icons.Default.Build
        ),
        NavigationBarItem(
            title = "Messages",
            selectedIcon = Icons.Filled.Email,
            unselectedIcon = Icons.Default.Email
        ),
        NavigationBarItem(
            title = "Menu",
            selectedIcon = Icons.Filled.Menu,
            unselectedIcon = Icons.Default.Menu
        )
    )

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            showBottomBar = when {
                destination.route?.startsWith("CalibrationProcess") == true ||
                        destination.route?.startsWith("login") == true ||
                        destination.route?.startsWith("CalMetalDetectorConveyor") == true -> false // Hide for calibration routes
                else -> true // Show for other routes
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(color = Color.LightGray),
        topBar = {
            // Hide the TopAppBar if on the login screen
            if (navController.currentBackStackEntry?.destination?.route != "login") {
                MyTopAppBar(navController = navController)
            }
        },
        bottomBar = {
            // Conditionally show or hide the bottom bar based on the current route
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.LightGray
                ) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedItemIndex == index,
                            onClick = {
                                selectedItemIndex = index
                                Log.d("NavigationDebug", "Navigating to ${item.title} at index $index")
                                when (index) {
                                    0 -> navController.navigate("serviceHome")
                                    1 -> navController.navigate("serviceSelectCustomer")
                                    2 -> navController.navigate("messagesHomeScreen")
                                    3 -> navController.navigate("menu")
                                }
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    color = if (selectedItemIndex == index) Color.Red else Color.Unspecified,
                                    fontWeight = if (selectedItemIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = if (index == selectedItemIndex) {
                                        item.selectedIcon
                                    } else item.unselectedIcon,
                                    contentDescription = item.title,
                                    tint = if (selectedItemIndex == index) Color.Red else Color.Unspecified
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Red,
                                unselectedIconColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AppNavGraph(navController = navController, db = db, userViewModel = userViewModel)
            }
        }
    )
}

//@Composable
//fun NoNetworkNoLoginDialog(onRetry: () -> Unit, onClose: () -> Unit) {
//    androidx.compose.material.AlertDialog(
//        onDismissRequest = { },
//        title = { Text("No Network and No Login") },
//        text = { Text("Please connect to a network or log in to continue.") },
//        confirmButton = {
//            androidx.compose.material.TextButton(onClick = { onRetry() }) {
//                Text("Retry")
//            }
//        },
//        dismissButton = {
//            androidx.compose.material.TextButton(onClick = { onClose() }) {
//                Text("Close App")
//            }
//        }
//    )
//}


data class NavigationBarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
