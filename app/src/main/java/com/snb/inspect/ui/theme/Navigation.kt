package com.snb.inspect.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.snb.inspect.AppChromeViewModel
import com.snb.inspect.AppDatabase
import com.snb.inspect.RetrofitClient
import com.snb.inspect.UserViewModel
import com.snb.inspect.calibrationViewModels.CustomerViewModel
import com.snb.inspect.calibrationViewModels.NoticeViewModel
import com.snb.inspect.calibrationViewModels.WeekendRotaViewModel
import com.snb.inspect.repositories.CustomerRepository
import com.snb.inspect.repositories.MetalDetectorModelsRepository
import com.snb.inspect.repositories.MetalDetectorSystemsRepository
import com.snb.inspect.repositories.MetalDetectorConveyorCalibrationRepository
import com.snb.inspect.repositories.RetailerSensitivitiesRepository
import com.snb.inspect.repositories.SystemTypeRepository
import com.snb.inspect.repositories.UserRepository
import com.snb.inspect.screens.mainmenu.HomeScreen
import com.snb.inspect.screens.mainmenu.NoticesScreen
import com.snb.inspect.screens.mainmenu.ServiceSelectCustomerScreen
import com.snb.inspect.screens.mainmenu.SettingsScreen
import com.snb.inspect.screens.menu.AboutAppScreen
import com.snb.inspect.screens.menu.CheckweigherAccuracyScreen
import com.snb.inspect.screens.menu.CheckweigherSpeedCalculatorScreen
import com.snb.inspect.screens.menu.DatabaseSyncScreen
import com.snb.inspect.screens.menu.MDFailsafesScreen
import com.snb.inspect.screens.menu.MSSensitivitiesScreen
import com.snb.inspect.screens.menu.MyCalibrationsScreen
import com.snb.inspect.screens.menu.WeekendRotaScreen
import com.snb.inspect.screens.service.AddNewMetalDetectorScreen
import com.snb.inspect.screens.service.ManualViewerScreen
import com.snb.inspect.screens.service.MetalDetectorConveyorSystemScreen
import com.snb.inspect.screens.service.ServiceSelectSystemScreen
import com.snb.inspect.util.LogConsole
import com.snb.inspect.util.SyncPreferences
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    userViewModel: UserViewModel,
    customerViewModel: CustomerViewModel,
    noticeViewModel: NoticeViewModel,
    db: AppDatabase,
    chromeVm: AppChromeViewModel,
    snackbarHostState: SnackbarHostState,
    repositoryMdSystems: MetalDetectorSystemsRepository,
    calibrationRepository: MetalDetectorConveyorCalibrationRepository
) {
    val apiService = RetrofitClient.instance

    val repositorySystemTypes =
        SystemTypeRepository(apiService, db)

    val repositoryMdModels =
        MetalDetectorModelsRepository(apiService, db)

    val context = LocalContext.current
    val syncPrefs = remember { SyncPreferences(context) }

    val repositoryCustomer =
        CustomerRepository(apiService, db, syncPrefs)

    val userRepository = UserRepository(db.userDao(), apiService)


    NavHost(
        navController = navController,
        startDestination = "serviceSelectCustomer"
    ) {

        composable("logsScreen") {
            LogConsole(chromeVm = chromeVm)
        }

        composable("serviceHome") {
            HomeScreen(navController, chromeVm = chromeVm)
        }

        composable("menu") {
            SettingsScreen(navController, userViewModel)
        }

        composable("aboutApp") {
            AboutAppScreen(chromeVm = chromeVm)
        }

        composable("checkweigherAccuracy") {
            CheckweigherAccuracyScreen()
        }

        composable("checkweigherSpeedCalculator") {
            CheckweigherSpeedCalculatorScreen()
        }

        composable("msSensitivities") {
            MSSensitivitiesScreen()
        }

        composable("mdFailsafes") {
            MDFailsafesScreen()
        }

        composable("weekendRota") {
            val rotaViewModel: WeekendRotaViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return WeekendRotaViewModel(apiService, userRepository) as T
                    }
                }
            )
            WeekendRotaScreen(viewModel = rotaViewModel)
        }

        composable("notices") {
            NoticesScreen(
                navController = navController,
                chromeVm = chromeVm,
                noticeViewModel = noticeViewModel,
                snackbarHostState = snackbarHostState
            )
        }

        composable("databaseSync") {
            val detectionRepo = RetailerSensitivitiesRepository(apiService, db)

            DatabaseSyncScreen(
                repositoryCustomer = repositoryCustomer,
                repositoryMdModels = repositoryMdModels,
                repositoryMdSystems = repositoryMdSystems,
                repositorySystemTypes = repositorySystemTypes,
                detectionRepo = detectionRepo,
                snackbarHostState = snackbarHostState
            )
        }

        composable("serviceSelectCustomer") {

            ServiceSelectCustomerScreen(
                navController = navController,
                chromeVm = chromeVm,
                customerViewModel = customerViewModel,
                snackbarHostState = snackbarHostState
            )
        }

        composable("calibrationSearchSystem/{customerID}/{customerName}/{customerPostcode}/{customerAddress}") { backStackEntry ->

            val customerID =
                backStackEntry.arguments?.getString("customerID")?.toIntOrNull() ?: 0

            val customerName =
                backStackEntry.arguments?.getString("customerName") ?: ""

            val customerPostcode =
                backStackEntry.arguments?.getString("customerPostcode") ?: ""

            val customerAddress =
                backStackEntry.arguments?.getString("customerAddress") ?: ""


            ServiceSelectSystemScreen(
                navController = navController,
                customerID = customerID,
                customerName = customerName,
                customerPostcode = customerPostcode,
                customerAddress = customerAddress,
                repository = repositoryMdSystems,
                snackbarHostState = snackbarHostState,
                chromeVm = chromeVm
            )
        }

        composable("MetalDetectorConveyorSystemScreen/{systemId}") { backStackEntry ->

            val systemId =
                backStackEntry.arguments?.getString("systemId")?.toIntOrNull() ?: 0

            val dao = db.metalDetectorConveyorCalibrationDAO()

            MetalDetectorConveyorSystemScreen(
                navController = navController,
                repositoryMD = repositoryMdSystems,
                dao = dao,
                repositoryModels = repositoryMdModels,
                systemId = systemId,
                chromeVm = chromeVm,
                snackbarHostState = snackbarHostState
            )
        }

        composable("myCalibrations") {

            val dao = db.metalDetectorConveyorCalibrationDAO()

            MyCalibrationsScreen(
                dao = dao,
                customerRepository = repositoryCustomer,
                systemsRepository = repositoryMdSystems,
                calibrationRepository = calibrationRepository,
                apiService = apiService,
                snackbarHostState = snackbarHostState
            )

        }

        composable("addNewMetalDetectorScreen/{customerID}/{customerName}"){ backStackEntry ->
            val customerID =
                backStackEntry.arguments?.getString("customerID")?.toIntOrNull() ?: 0
            val customerName =
            backStackEntry.arguments?.getString("customerName") ?: ""

            AddNewMetalDetectorScreen(
                navController = navController,
                systemTypeRepository = repositorySystemTypes,
                mdModelsRepository = repositoryMdModels,
                mdSystemsRepository = repositoryMdSystems,
                customerID = customerID,
                customerName = customerName,
                snackbarHostState = snackbarHostState
            )

        }

        composable(
            route = "manualViewer/{modelName}/{manualUrl}",
            arguments = listOf(
                navArgument("modelName") { type = NavType.StringType },
                navArgument("manualUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val modelName = backStackEntry.arguments?.getString("modelName") ?: ""
            val encodedUrl = backStackEntry.arguments?.getString("manualUrl") ?: ""
            val manualUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())

            ManualViewerScreen(
                modelName = modelName,
                manualUrl = manualUrl,
                chromeVm = chromeVm,
                onBack = { navController.popBackStack() }
            )
        }

    }
}
