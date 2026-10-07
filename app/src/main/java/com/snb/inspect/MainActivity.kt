package com.snb.inspect


import android.Manifest
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.snb.inspect.calibrationViewModels.CustomerViewModel
import com.snb.inspect.calibrationViewModels.CustomerViewModelFactory
import com.snb.inspect.calibrationViewModels.NoticeViewModel
import com.snb.inspect.calibrationViewModels.NoticeViewModelFactory
import com.snb.inspect.network.NetworkMonitor
import com.snb.inspect.network.rememberIsOffline
import com.snb.inspect.repositories.CheckweigherCalibrationRepository
import com.snb.inspect.repositories.CheckweigherSystemsRepository
import com.snb.inspect.repositories.CustomerRepository
import com.snb.inspect.repositories.CwSystemNotesRepository
import com.snb.inspect.repositories.MdSystemNotesRepository
import com.snb.inspect.repositories.MeasuringEquipmentRepository
import com.snb.inspect.repositories.MetalDetectorSystemsRepository
import com.snb.inspect.repositories.MetalDetectorConveyorCalibrationRepository
import com.snb.inspect.repositories.MetalDetectorModelsRepository
import com.snb.inspect.repositories.NoticeRepository
import com.snb.inspect.repositories.RetailerSensitivitiesRepository
import com.snb.inspect.repositories.SystemTypeRepository
import com.snb.inspect.repositories.UserRepository
import com.snb.inspect.screens.LoginScreen
import com.snb.inspect.ui.theme.AppNavGraph
import com.snb.inspect.ui.theme.SnbDarkGrey
import com.snb.inspect.ui.theme.SnbRed
import com.snb.inspect.util.DataBackupManager
import com.snb.inspect.util.InAppLogger
import com.snb.inspect.util.SyncPreferences
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


enum class AppInitState {
    CHECKING_UPDATES,
    SYNCING_BEFORE_UPDATE,
    READY_TO_LAUNCH
}

class MainActivity : ComponentActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var customerViewModel: CustomerViewModel
    private lateinit var noticeViewModel: NoticeViewModel
    private lateinit var mdSystemsRepository: MetalDetectorSystemsRepository
    private lateinit var cwSystemsRepository: CheckweigherSystemsRepository
    private lateinit var calibrationRepository: MetalDetectorConveyorCalibrationRepository
    private lateinit var cwCalibrationRepository: CheckweigherCalibrationRepository
    private lateinit var mdSystemNotesRepository: MdSystemNotesRepository
    private lateinit var cwSystemNotesRepository: CwSystemNotesRepository

    private lateinit var appUpdateManager: AppUpdateManager

    // ActivityResultLauncher for the in-app update flow
    private val updateResultStarter = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode != RESULT_OK) {
            InAppLogger.e("Update flow failed! Result code: ${result.resultCode}")
            // The user cancelled or the update failed. 
            // In IMMEDIATE updates, we usually want to close the app or try again.
            // For now, let's just let them continue so they aren't totally locked out if the Play Store glitches.
            initState.value = AppInitState.READY_TO_LAUNCH
        }
    }

    private val initState = mutableStateOf(AppInitState.CHECKING_UPDATES)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        android.util.Log.d("MESSA DEBUG", "onCreate. savedInstanceState is null = ${savedInstanceState == null}")

        // Only disable screenshots/screen recording in non-debug builds
        if (0 == (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        val app = application as MyApplication
        val db = app.database
        val apiService = app.apiService
        val syncPrefs = SyncPreferences(this)

        lifecycleScope.launch {
            DataBackupManager.checkAndRestore(applicationContext, db)
        }

        // Repositories
        val userRepository = UserRepository(db.userDao(), apiService)
        val customerRepository = CustomerRepository(apiService, db, syncPrefs)
        val noticeRepository = NoticeRepository(apiService, db)
        mdSystemsRepository = MetalDetectorSystemsRepository(apiService, db)
        cwSystemsRepository = CheckweigherSystemsRepository(apiService, db)
        calibrationRepository = MetalDetectorConveyorCalibrationRepository(db.metalDetectorConveyorCalibrationDAO())
        cwCalibrationRepository = CheckweigherCalibrationRepository(db.checkweigherCalibrationDAO())
        mdSystemNotesRepository = MdSystemNotesRepository(apiService, db)
        cwSystemNotesRepository = CwSystemNotesRepository(apiService, db)

        // Additional repositories for background sync
        val systemTypeRepository = SystemTypeRepository(apiService, db)
        val mdModelsRepository = MetalDetectorModelsRepository(apiService, db)
        val retailerSensitivitiesRepository = RetailerSensitivitiesRepository(apiService, db)
        val measuringEquipmentRepository = MeasuringEquipmentRepository(apiService, db)

        // ViewModels
        userViewModel = ViewModelProvider(this, UserViewModelFactory(userRepository))[UserViewModel::class.java]
        customerViewModel = ViewModelProvider(this, CustomerViewModelFactory(customerRepository))[CustomerViewModel::class.java]
        noticeViewModel = ViewModelProvider(this, NoticeViewModelFactory(noticeRepository))[NoticeViewModel::class.java]

        val savedCredentials = PreferencesHelper.getCredentials(this)

        // Always sync users on launch
        userViewModel.syncUsers(this)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForAppUpdates()

        setContent {

            val currentInitState by initState
            
            if (currentInitState == AppInitState.CHECKING_UPDATES) {
                // Just a blank screen or a splash screen while checking Play Store quickly
                Box(modifier = Modifier.fillMaxSize().background(Color.White))
                return@setContent
            }

            if (currentInitState == AppInitState.SYNCING_BEFORE_UPDATE) {
                UpdateSyncScreen()
                return@setContent
            }


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

                loginStatus -> {

                    LaunchedEffect(Unit) {



                        // Boot sync (runs once)
                        customerViewModel.syncCustomers()
                        noticeViewModel.syncNotices()
                    }

                    MyApp(
                        db = db,
                        userViewModel = userViewModel,
                        customerViewModel = customerViewModel,
                        noticeViewModel = noticeViewModel,
                        mdSystemsRepository = mdSystemsRepository,
                        cwSystemsRepository = cwSystemsRepository,
                        calibrationRepository = calibrationRepository,
                        cwCalibrationRepository = cwCalibrationRepository,
                        systemTypeRepository = systemTypeRepository,
                        mdModelsRepository = mdModelsRepository,
                        retailerSensitivitiesRepository = retailerSensitivitiesRepository,
                        measuringEquipmentRepository = measuringEquipmentRepository,
                        mdSystemNotesRepository = mdSystemNotesRepository,
                        cwSystemNotesRepository = cwSystemNotesRepository,
                        syncPrefs = syncPrefs
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
                        onLoginClick = { username, password, pin ->
                            userViewModel.login(this, username, password, pin)
                        }
                    )
                }
            }
        }
    }

    private fun checkForAppUpdates() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // An update is available! Intercept and sync first.
                initState.value = AppInitState.SYNCING_BEFORE_UPDATE
                syncDataBeforeUpdate(appUpdateInfo)
            } else {
                // No update available, or update type not allowed. Proceed.
                initState.value = AppInitState.READY_TO_LAUNCH
            }
        }.addOnFailureListener { e ->
            // Offline, or Play Store error. Just proceed to the app.
            InAppLogger.e("App Update Check Failed: ${e.message}")
            initState.value = AppInitState.READY_TO_LAUNCH
        }
    }

    private fun syncDataBeforeUpdate(appUpdateInfo: AppUpdateInfo) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                InAppLogger.d("PRE-UPDATE SYNC: Starting...")
                
                val context = this@MainActivity

                // 1. Upload new machines
                mdSystemsRepository.uploadUnsyncedSystems(context)
                cwSystemsRepository.uploadUnsyncedSystems(context)
                
                // 2. Upload calibrations
                val apiService = (application as MyApplication).apiService
                calibrationRepository.uploadUnsyncedCalibrations(context, apiService)
                cwCalibrationRepository.uploadUnsyncedCalibrations(context, apiService)

                // 3. Sync Notes
                mdSystemNotesRepository.syncAllUnsyncedNotes(context)
                cwSystemNotesRepository.syncAllUnsyncedNotes(context)

                InAppLogger.d("PRE-UPDATE SYNC: Completed successfully.")
                
                withContext(Dispatchers.Main) {
                    // Trigger the actual update UI
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateResultStarter,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }

            } catch (e: Exception) {
                InAppLogger.e("PRE-UPDATE SYNC: Failed. ${e.message}")
                // Option B: If the sync fails (e.g. poor signal dropping halfway through), 
                // we bypass the update and let them into the app to continue working offline.
                withContext(Dispatchers.Main) {
                    initState.value = AppInitState.READY_TO_LAUNCH
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
fun UpdateSyncScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = SnbRed)
            Text(
                text = "Important Update Required",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Syncing your offline data safely to the server before installing the update...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun SyncUsersScreen(
    message: String?,
    onRetry: () -> Unit
) {
    // Optional: simple auto-retry countdown when there’s an error
    // Useful for “Azure is waking up” without making the user babysit it.
    var retryInSeconds by remember { mutableIntStateOf(if (message != null) { 10 } else { 0 }) }

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
            .background(Color.White),
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


data class NavigationBarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun OfflineBanner(
    isOffline: Boolean,
    isSyncing: Boolean
) {
    val showBanner = isOffline || isSyncing
    val backgroundColor by animateColorAsState(
        targetValue = if (isOffline) Color(0xFFB71C1C) else Color(0xFF2E7D32),
        label = "bannerColor"
    )

    AnimatedVisibility(
        visible = showBanner,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .background(backgroundColor)
                .padding(vertical = 6.dp)
                .heightIn(min = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text(
                    text = if (isOffline) "Offline mode" else "Back online - Syncing data...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}







@OptIn(ExperimentalMaterial3Api::class)
@Composable
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun MyApp(
    db: AppDatabase,
    userViewModel: UserViewModel,
    customerViewModel: CustomerViewModel,
    noticeViewModel: NoticeViewModel,
    mdSystemsRepository: MetalDetectorSystemsRepository,
    cwSystemsRepository: CheckweigherSystemsRepository,
    calibrationRepository: MetalDetectorConveyorCalibrationRepository,
    cwCalibrationRepository: CheckweigherCalibrationRepository,
    systemTypeRepository: SystemTypeRepository,
    mdModelsRepository: MetalDetectorModelsRepository,
    retailerSensitivitiesRepository: RetailerSensitivitiesRepository,
    measuringEquipmentRepository: MeasuringEquipmentRepository,
    mdSystemNotesRepository: MdSystemNotesRepository,
    cwSystemNotesRepository: CwSystemNotesRepository,
    syncPrefs: SyncPreferences
) {

    /*
        NavController drives the entire app navigation.
        There should be ONE of these at the root of your app.
     */
    val navController = rememberNavController()

    /*
        Chrome ViewModel controls GLOBAL UI:
        - Top bar
        - Menu button
        - Back button
        etc.

        Screens should NOT control global UI anymore.
     */
    val chromeVm: AppChromeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    /*
        Observe the TopBar state once.
        NEVER collect the same flow multiple times in Compose.
     */
    val topBarState by chromeVm.topBarState.collectAsState()

    /*
        Observe navigation changes so we can update the TopBar automatically.
        This removes the need for setTopBar() calls inside screens.
     */
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val route = navBackStackEntry?.destination?.route


    // ---------------------------------------------------------
    // GLOBAL SNACKBAR HOST
    // ---------------------------------------------------------
    /*
        One snackbar host for the entire app.

        Avoid putting snack bars inside screens — it leads to
        nested scaffolds and layout chaos.
     */
    val snackbarHostState = remember { SnackbarHostState() }



    // ---------------------------------------------------------
    // OFFLINE STATE
    // ---------------------------------------------------------
    val isOffline by rememberIsOffline()

    var isSyncingBackground by remember { mutableStateOf(false) }

    // AUTO-SYNC when coming back online
    LaunchedEffect(isOffline) {
        if (!isOffline && syncPrefs.isAutoSyncEnabled()) {
            // Wait 2 seconds for signal stability
            delay(2000)
            
            isSyncingBackground = true
            scope.launch(Dispatchers.IO) {
                try {
                    val app = context.applicationContext as MyApplication
                    val apiService = app.apiService

                    // 1. UPLOAD MACHINES FIRST (Creates cloud IDs & links calibrations)
                    InAppLogger.d("BACKGROUND SYNC: Step 1 - Uploading Machines...")
                    mdSystemsRepository.uploadUnsyncedSystems(context)
                    
                    // 2. REFRESH MACHINE DATABASE (Resolves cloud IDs for machines already on server)
                    // This now also resolving ID linking for pending calibrations.
                    InAppLogger.d("BACKGROUND SYNC: Step 2 - Pulling latest machine data...")
                    mdSystemsRepository.fetchAndStoreMdSystems()
                    cwSystemsRepository.fetchAndStoreCwSystems()
                    
                    // 3. UPLOAD CALIBRATIONS (Now they definitely have cloudSystemIds and fresh CSVs)
                    InAppLogger.d("BACKGROUND SYNC: Step 3 - Uploading Calibrations...")
                    calibrationRepository.uploadUnsyncedCalibrations(context, apiService)
                    
                    // 4. REFRESH OTHER DATA
                    InAppLogger.d("BACKGROUND SYNC: Step 4 - Refreshing other data...")
                    customerViewModel.syncCustomers()
                    noticeViewModel.syncNotices()

                    // 5. REFRESH SYSTEM TYPES, MODELS & SENSITIVITIES
                    InAppLogger.d("BACKGROUND SYNC: Step 5 - Refreshing System Types, Models & Sensitivities...")
                    systemTypeRepository.fetchAndStoreSystemTypes()
                    mdModelsRepository.fetchAndStoreMdModels()
                    cwSystemsRepository.fetchAndStoreCwModels()
                    retailerSensitivitiesRepository.fetchAndStoreConveyor()
                    retailerSensitivitiesRepository.fetchAndStoreFreefall()
                    retailerSensitivitiesRepository.fetchAndStorePipeline()
                    measuringEquipmentRepository.fetchAndStoreEquipment()

                    // 6. SYNC NOTES
                    InAppLogger.d("BACKGROUND SYNC: Step 6 - Syncing Notes...")
                    mdSystemNotesRepository.syncAllUnsyncedNotes(context)
                    mdSystemNotesRepository.fetchAndStoreAllNotes()
                    
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("✅ Background sync complete")
                    }
                } catch (e: Exception) {
                    InAppLogger.e("Background Sync Failed: ${e.message}")
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("⚠️ Background sync failed: ${e.message}")
                    }
                } finally {
                    isSyncingBackground = false
                }
            }
        }
    }


    // ---------------------------------------------------------
    // TOP BAR AUTO-UPDATES BASED ON ROUTE
    // ---------------------------------------------------------
    /*

        Navigation drives chrome.
        Screens no longer fight over it.
     */


    LaunchedEffect(route) {
        chromeVm.applyRouteChrome(
            chromeVm.topBarForRoute(route)
        )
    }



    // ---------------------------------------------------------
    // BOTTOM NAVIGATION ITEMS
    // ---------------------------------------------------------
    val items = listOf(
        NavigationBarItem("Service", Icons.Outlined.Build, Icons.Outlined.Build),
        NavigationBarItem("Notices", Icons.Outlined.Info, Icons.Outlined.Info),
        NavigationBarItem("More", Icons.Filled.MoreHoriz, Icons.Default.MoreHoriz)
    )

    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    // =========================================================
    // ROOT SCAFFOLD (THE ONLY ONE IN THE APP)
    // =========================================================
    Scaffold(



        /*
            Global snackbar lives here.
         */
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 12.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SnbDarkGrey,
                    contentColor = Color.White,
                    actionColor = SnbRed,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(12.dp)
                )
            }
        },




        // ---------------- TOP BAR ----------------
        topBar = {
            MyTopAppBar(
                navController = navController,
                title = topBarState.title,
                showBack = topBarState.showBack,
                showCall = topBarState.showCall,
                showMenu = topBarState.showMenu,
                onMenuClick = topBarState.onMenuClick
            )
        },


        // ---------------- BOTTOM BAR ----------------
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
                                contentDescription = item.title,
                                modifier = Modifier.size(30.dp)
                            )
                        },
                        label = {
                            Text(
                                item.title,
                                fontWeight = if (selectedItemIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SnbRed,
                            selectedTextColor = Color.Black,
                            indicatorColor = Color.LightGray,
                            unselectedIconColor = Color.Black,
                            unselectedTextColor = Color.Black
                        )
                    )
                }
            }
        }

    ) { innerPadding ->


        // =====================================================
        // CONTENT LAYER
        // =====================================================
        /*

            NavHost = main content

         */
        Column(
            modifier = Modifier
                .padding(innerPadding) // accounts for top & bottom bars
                .fillMaxSize()
        ) {
            OfflineBanner(isOffline = isOffline, isSyncing = isSyncingBackground)


            // ---------------- MAIN NAVIGATION ----------------

            Box(
                modifier = Modifier.weight(1f)
            ){
                AppNavGraph(
                    navController = navController,
                    db = db,
                    userViewModel = userViewModel,
                    customerViewModel = customerViewModel,
                    noticeViewModel = noticeViewModel,
                    chromeVm = chromeVm,
                    snackbarHostState = snackbarHostState, // optional but recommended
                    repositoryMdSystems = mdSystemsRepository,
                    repositoryCwSystems = cwSystemsRepository,
                    calibrationRepository = calibrationRepository,
                    cwCalibrationRepository = cwCalibrationRepository,
                    notesRepository = mdSystemNotesRepository,
                    cwNotesRepository = cwSystemNotesRepository
                )
            }

        }
    }
}
