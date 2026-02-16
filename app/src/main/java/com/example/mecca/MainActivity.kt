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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.mecca.calibrationViewModels.CustomerViewModel
import com.example.mecca.calibrationViewModels.NoticeViewModel
import com.example.mecca.network.isNetworkAvailable
import com.example.mecca.repositories.CustomerRepository
import com.example.mecca.repositories.NoticeRepository
import com.example.mecca.repositories.UserRepository
import com.example.mecca.screens.LoginScreen
import com.example.mecca.ui.theme.AppNavGraph
import com.example.mecca.util.SyncPreferences
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var customerViewModel: CustomerViewModel
    private lateinit var noticeViewModel: NoticeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        android.util.Log.d("MESSA DEBUG", "onCreate. savedInstanceState is null = ${savedInstanceState == null}")

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        val app = application as MyApplication
        val db = app.database
        val apiService = app.apiService
        val syncPrefs = SyncPreferences(this)

        // Repositories
        val userRepository = UserRepository(db.userDao(), apiService)
        val customerRepository = CustomerRepository(apiService, db, syncPrefs)
        val noticeRepository = NoticeRepository(apiService, db)

        // ViewModels
        userViewModel = UserViewModel(userRepository)
        customerViewModel = CustomerViewModel(customerRepository)
        noticeViewModel = NoticeViewModel(noticeRepository)

        val savedCredentials = PreferencesHelper.getCredentials(this)
        val isPersistedLogin = PreferencesHelper.isLoggedIn(this)

        // Always sync users on launch
        userViewModel.syncUsers(this)

        setContent {

            val syncStatus by userViewModel.syncStatus.collectAsState()
            val loginStatus by userViewModel.loginStatus.collectAsState()
            val loginError by userViewModel.loginError.collectAsState()

            when {

                //----------------------------------------
                // 1. WAIT FOR USER SYNC
                //----------------------------------------

                !syncStatus -> {
                    SyncUsersScreen(
                        message = loginError,
                        onRetry = { userViewModel.syncUsers(this) }
                    )
                }

                //----------------------------------------
                // 2. AUTO LOGIN IF SESSION EXISTS
                //----------------------------------------

                loginStatus || isPersistedLogin -> {

                    LaunchedEffect(Unit) {

                        // If ViewModel forgot login but prefs say yes → restore session
                        if (!loginStatus && isPersistedLogin) {

                            val savedUsername = savedCredentials.first
                            val savedPassword = savedCredentials.second

                            if (!savedUsername.isNullOrBlank() && !savedPassword.isNullOrBlank()) {

                                android.util.Log.d("MESSA DEBUG", "Restoring persisted login")

                                userViewModel.login(
                                    this@MainActivity,
                                    savedUsername,
                                    savedPassword
                                )
                            }
                        }

                        // Boot sync (runs once)
                        customerViewModel.syncCustomers()
                        noticeViewModel.syncNotices()
                    }

                    MyApp(
                        db = db,
                        userViewModel = userViewModel,
                        customerViewModel = customerViewModel,
                        noticeViewModel = noticeViewModel
                    )
                }

                //----------------------------------------
                // 3. SHOW LOGIN
                //----------------------------------------

                else -> {
                    LoginScreen(
                        userViewModel = userViewModel,
                        defaultUsername = savedCredentials.first,
                        defaultPassword = savedCredentials.second,
                        loginError = loginError,
                        onLoginClick = { username, password ->
                            userViewModel.login(this, username, password)
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        android.util.Log.d("MESSA DEBUG", "onDestroy called")
        super.onDestroy()
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

//
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
//@Composable
//fun AppNavGraph(
//    navController: NavHostController,
//    db: AppDatabase,
//    userViewModel: UserViewModel,
//    customerViewModel: CustomerViewModel,
//    noticeViewModel: NoticeViewModel,
//    chromeVm: AppChromeViewModel
//)
//
// {
//    val navController = rememberNavController()
//    val context = LocalContext.current
//
//    var isOffline by remember { mutableStateOf(false) }
//    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
//    var showBottomBar by rememberSaveable { mutableStateOf(true) }
//    val chromeVm: AppChromeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
//    val topBarState by chromeVm.topBarState.collectAsState()
//
//    //Detect offline status
//    LaunchedEffect(Unit) {
//        while (true) {
//            isOffline = !isNetworkAvailable(context)
//            delay(3000)
//        }
//    }
//
//    // detect current route
//    LaunchedEffect(navController) {
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            showBottomBar = when {
//                destination.route?.startsWith("CalibrationProcess") == true ||
//                        destination.route?.startsWith("login") == true ||
//                        destination.route?.startsWith("CalMetalDetectorConveyor") == true -> false
//                else -> true
//            }
//        }
//    }
//
//    // detect if keyboard visible
//    WindowInsets.isImeVisible
//
//
//
//
//    val items = listOf(
//        //NavigationBarItem("Schedule", Icons.Filled.DateRange, Icons.Default.DateRange),
//        NavigationBarItem("Service", Icons.Filled.Build, Icons.Default.Build),
//        NavigationBarItem("Messages", Icons.Filled.MailOutline, Icons.Default.MailOutline),
//        NavigationBarItem("More", Icons.Filled.MoreHoriz, Icons.Default.MoreHoriz)
//    )
//
//
//
//    Scaffold(
//        modifier = Modifier.fillMaxSize(), //.background(Color.LightGray),
//        contentWindowInsets = WindowInsets.systemBars,
//        topBar = {
//            if (navController.currentBackStackEntry?.destination?.route != "login") {
//                MyTopAppBar(
//                    navController = navController,
//                    title = topBarState.title,
//                    showBack = topBarState.showBack,
//                    showCall = topBarState.showCall,
//                    onMenuClick = if (topBarState.showMenu) topBarState.onMenuClick else null
//                )
//            }
//        },
//        bottomBar = {
//            if (showBottomBar) {
//                NavigationBar(containerColor = Color.LightGray) {
//                    items.forEachIndexed { index, item ->
//                        NavigationBarItem(
//                            selected = selectedItemIndex == index,
//                            onClick = {
//                                selectedItemIndex = index
//                                when (index) {
//                                    //0 -> navController.navigate("serviceHome")
//                                    0 -> navController.navigate("serviceSelectCustomer")
//                                    1 -> navController.navigate("notices")
//                                    2 -> navController.navigate("menu")
//                                }
//                            },
//                            label = {
//                                Text(
//                                    text = item.title,
//                                    color = if (selectedItemIndex == index) Color.Red else Color.Unspecified,
//                                    fontWeight = if (selectedItemIndex == index) FontWeight.Bold else FontWeight.Normal
//                                )
//                            },
//                            icon = {
//                                Icon(
//                                    imageVector = if (index == selectedItemIndex) item.selectedIcon else item.unselectedIcon,
//                                    contentDescription = item.title,
//                                    tint = if (selectedItemIndex == index) Color.Red else Color.Unspecified
//                                )
//                            },
//                            colors = NavigationBarItemDefaults.colors(
//                                selectedIconColor = Color.Red,
//                                unselectedIconColor = Color.Gray,
//                                indicatorColor = Color.Transparent
//                            )
//                        )
//                    }
//                }
//            }
//        }
//    ) { innerPadding ->
//        Column(Modifier.padding(innerPadding)) {
//            OfflineBanner(isOffline)
//            AppNavGraph(
//                navController = navController,
//                db = db,
//                userViewModel = userViewModel,
//                customerViewModel = customerViewModel,
//                noticeViewModel = noticeViewModel,
//                chromeVm = chromeVm
//            )
//
//        }
//    }
//}


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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(
    db: AppDatabase,
    userViewModel: UserViewModel,
    customerViewModel: CustomerViewModel,
    noticeViewModel: NoticeViewModel
) {

    val navController = rememberNavController()
    val chromeVm: AppChromeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val context = LocalContext.current

    var isOffline by remember { mutableStateOf(false) }

    // Network watcher
    LaunchedEffect(Unit) {
        while (true) {
            isOffline = !isNetworkAvailable(context)
            delay(3000)
        }
    }

    val items = listOf(
        NavigationBarItem("Service", Icons.Filled.Build, Icons.Default.Build),
        NavigationBarItem("Messages", Icons.Filled.MailOutline, Icons.Default.MailOutline),
        NavigationBarItem("More", Icons.Filled.MoreHoriz, Icons.Default.MoreHoriz)
    )

    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(

        topBar = {
            MyTopAppBar(
                navController = navController,
                title = chromeVm.topBarState.collectAsState().value.title,
                showBack = chromeVm.topBarState.collectAsState().value.showBack,
                showCall = chromeVm.topBarState.collectAsState().value.showCall,
                onMenuClick = null
            )
        },

        bottomBar = {
            NavigationBar(containerColor = Color.LightGray) {

                items.forEachIndexed { index, item ->

                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = {

                            selectedItemIndex = index

                            when (index) {
                                0 -> navController.navigate("serviceSelectCustomer")
                                1 -> navController.navigate("notices")
                                2 -> navController.navigate("menu")
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.selectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) }
                    )
                }
            }
        }

    ) { innerPadding ->

        Column(Modifier.padding(innerPadding)) {

            OfflineBanner(isOffline)

            // ⭐ THIS is where navigation belongs
            AppNavGraph(
                navController = navController,
                db = db,
                userViewModel = userViewModel,
                customerViewModel = customerViewModel,
                noticeViewModel = noticeViewModel,
                chromeVm = chromeVm
            )
        }
    }
}
