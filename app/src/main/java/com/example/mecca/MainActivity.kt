package com.example.mecca

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.mecca.Network.isNetworkAvailable
import com.example.mecca.repositories.UserRepository
import com.example.mecca.screens.LoginScreen
import com.example.mecca.ui.theme.AppNavGraph
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

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
            val loginError by userViewModel.loginError.collectAsState()

            when {
                // Case 1: sync not yet complete or failed
                !syncStatus -> {
                    SyncUsersScreen(
                        message = loginError,
                        onRetry = { userViewModel.syncUsers(this) }
                    )
                }

                // Case 2: login already successful
                loginStatus -> {
                    MyApp(db, userViewModel)
                }

                // Case 3: ready for user to log in
                else -> {
                    LoginScreen(
                        userViewModel = userViewModel,
                        defaultUsername = savedUsername,
                        defaultPassword = savedPassword,
                        loginError = loginError,
                        onLoginClick = { username, password ->
                            userViewModel.login(this, username, password)
                        }
                    )
                }
            }
        }


        // Start syncing users when the app launches
        userViewModel.syncUsers(this)
    }
}

@Composable
fun SyncUsersScreen(
    message: String?,
    onRetry: () -> Unit
) {
    // Optional: simple auto-retry countdown when there’s an error
    // Useful for “Azure is waking up” without making the user babysit it.
    var retryInSeconds by remember { mutableIntStateOf(if (message != null) 10 else 0) }

    LaunchedEffect(message) {
        if (message != null) {
            retryInSeconds = 10
            while (retryInSeconds > 0) {
                delay(1000)
                retryInSeconds--
            }
            onRetry()
        } else {
            retryInSeconds = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            // This is the key bit: avoids status bar/cutout overlap
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (message == null) {
                CircularProgressIndicator()
                Text(
                    text = "Syncing users…",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Waking the server up and pulling the latest user list.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            } else {
                // Error state
                Text(
                    text = "Couldn’t sync users",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFB71C1C),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                Button(onClick = onRetry) {
                    Text("Retry now")
                }

                // Optional auto retry status
                if (retryInSeconds > 0) {
                    Text(
                        text = "Retrying automatically in ${retryInSeconds}s…",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Optional: allow offline login path if you want later
                // OutlinedButton(onClick = { /* continue offline */ }) { Text("Continue offline") }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MyApp(db: AppDatabase, userViewModel: UserViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    var isOffline by remember { mutableStateOf(false) }
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
    var showBottomBar by rememberSaveable { mutableStateOf(true) }
    val chromeVm: AppChromeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val topBarState by chromeVm.topBarState.collectAsState()

    //Detect offline status
    LaunchedEffect(Unit) {
        while (true) {
            isOffline = !isNetworkAvailable(context)
            delay(3000)
        }
    }

    // detect current route
    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            showBottomBar = when {
                destination.route?.startsWith("CalibrationProcess") == true ||
                        destination.route?.startsWith("login") == true ||
                        destination.route?.startsWith("CalMetalDetectorConveyor") == true -> false
                else -> true
            }
        }
    }

    // detect if keyboard visible
    val imeVisible = WindowInsets.isImeVisible




    val items = listOf(
        NavigationBarItem("Schedule", Icons.Filled.DateRange, Icons.Default.DateRange),
        NavigationBarItem("Service", Icons.Filled.Build, Icons.Default.Build),
        NavigationBarItem("Messages", Icons.Filled.Email, Icons.Default.Email),
        NavigationBarItem("Menu", Icons.Filled.Menu, Icons.Default.Menu)
    )



    Scaffold(
        modifier = Modifier.fillMaxSize(), //.background(Color.LightGray),
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            if (navController.currentBackStackEntry?.destination?.route != "login") {
                MyTopAppBar(
                    navController = navController,
                    title = topBarState.title,
                    showBack = topBarState.showBack,
                    showCall = topBarState.showCall,
                    onMenuClick = if (topBarState.showMenu) topBarState.onMenuClick else null
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.LightGray) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedItemIndex == index,
                            onClick = {
                                selectedItemIndex = index
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
                                    imageVector = if (index == selectedItemIndex) item.selectedIcon else item.unselectedIcon,
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
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            OfflineBanner(isOffline)
            AppNavGraph(
                navController = navController,
                db = db,
                userViewModel = userViewModel,
                chromeVm = chromeVm // pass it down
            )
        }
    }
}


@Composable
fun OfflineBanner(isOffline: Boolean) {
    AnimatedVisibility(
        visible = isOffline,
        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
            initialOffsetY = { -it } // slide down from top
        ),
        exit = fadeOut(animationSpec = tween(400)) + slideOutVertically(
            targetOffsetY = { -it } // slide back up
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFB71C1C)) // Deep red
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Offline mode: changes will require synchronising when network is available.",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}




data class NavigationBarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

