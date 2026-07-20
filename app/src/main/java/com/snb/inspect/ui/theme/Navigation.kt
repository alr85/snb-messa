package com.snb.inspect.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.snb.inspect.calibrationViewModels.CodesOfPracticeViewModel
import com.snb.inspect.calibrationViewModels.CustomerViewModel
import com.snb.inspect.calibrationViewModels.NoticeViewModel
import com.snb.inspect.calibrationViewModels.UserManualsViewModel
import com.snb.inspect.calibrationViewModels.WeekendRotaViewModel
import com.snb.inspect.repositories.CheckweigherCalibrationRepository
import com.snb.inspect.repositories.CheckweigherSystemsRepository
import com.snb.inspect.repositories.CodesOfPracticeRepository
import com.snb.inspect.repositories.CustomerRepository
import com.snb.inspect.repositories.CwSystemNotesRepository
import com.snb.inspect.repositories.MdSystemNotesRepository
import com.snb.inspect.repositories.MeasuringEquipmentRepository
import com.snb.inspect.repositories.MetalDetectorModelsRepository
import com.snb.inspect.repositories.MetalDetectorSystemsRepository
import com.snb.inspect.repositories.MetalDetectorConveyorCalibrationRepository
import com.snb.inspect.repositories.RetailerSensitivitiesRepository
import com.snb.inspect.repositories.SystemTypeRepository
import com.snb.inspect.repositories.UserManualsRepository
import com.snb.inspect.repositories.UserRepository
import com.snb.inspect.screens.mainmenu.HomeScreen
import com.snb.inspect.screens.mainmenu.NoticesScreen
import com.snb.inspect.screens.mainmenu.ServiceSelectCustomerScreen
import com.snb.inspect.screens.mainmenu.SettingsScreen
import com.snb.inspect.screens.menu.AboutAppScreen
import com.snb.inspect.screens.menu.CheckweigherAccuracyScreen
import com.snb.inspect.screens.menu.CheckweigherSpeedCalculatorScreen
import com.snb.inspect.screens.menu.CodesOfPracticeListScreen
import com.snb.inspect.screens.menu.DatabaseSyncScreen
import com.snb.inspect.screens.menu.MDFailsafesScreen
import com.snb.inspect.screens.menu.MSSensitivitiesScreen
import com.snb.inspect.screens.menu.MyCalibrationsScreen
import com.snb.inspect.screens.menu.MyValidationsScreen
import com.snb.inspect.screens.menu.UserManualsListScreen
import com.snb.inspect.screens.menu.WeekendRotaScreen
import com.snb.inspect.screens.service.AddNewCheckweigherScreen
import com.snb.inspect.screens.service.AddNewMetalDetectorScreen
import com.snb.inspect.screens.service.CheckweigherSystemScreen
import com.snb.inspect.screens.service.ManualViewerScreen
import com.snb.inspect.screens.service.MetalDetectorConveyorSystemScreen
import com.snb.inspect.screens.service.ServiceSelectSystemScreen
import com.snb.inspect.repositories.SensitivityOptimisationValidationRepository
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
    repositoryCwSystems: CheckweigherSystemsRepository,
    calibrationRepository: MetalDetectorConveyorCalibrationRepository,
    cwCalibrationRepository: CheckweigherCalibrationRepository,
    notesRepository: MdSystemNotesRepository,
    cwNotesRepository: CwSystemNotesRepository
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

    val manualRepository = UserManualsRepository(apiService, db)

    val codesRepository = CodesOfPracticeRepository(apiService, db)

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
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return WeekendRotaViewModel(apiService, userRepository) as T
                    }
                }
            )
            WeekendRotaScreen(viewModel = rotaViewModel)
        }

        composable("userManualsList") {
            val manualsViewModel: UserManualsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return UserManualsViewModel(manualRepository) as T
                    }
                }
            )
            // Sync on entry
            LaunchedEffect(Unit) { manualsViewModel.syncManuals() }
            
            UserManualsListScreen(
                viewModel = manualsViewModel,
                navController = navController,
                chromeVm = chromeVm
            )
        }

        composable("codesOfPracticeList") {
            val codesViewModel: CodesOfPracticeViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return CodesOfPracticeViewModel(codesRepository) as T
                    }
                }
            )
            // Sync on entry
            LaunchedEffect(Unit) { codesViewModel.syncCodes() }

            CodesOfPracticeListScreen(
                viewModel = codesViewModel,
                navController = navController,
                chromeVm = chromeVm
            )
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
            val measuringEquipmentRepo = MeasuringEquipmentRepository(apiService, db)

            DatabaseSyncScreen(
                repositoryCustomer = repositoryCustomer,
                repositoryMdModels = repositoryMdModels,
                repositoryMdSystems = repositoryMdSystems,
                repositoryCwSystems = repositoryCwSystems,
                repositorySystemTypes = repositorySystemTypes,
                repositoryMdSystemNotes = notesRepository,
                detectionRepo = detectionRepo,
                measuringEquipmentRepo = measuringEquipmentRepo,
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
                cwRepository = repositoryCwSystems,
                snackbarHostState = snackbarHostState,
                chromeVm = chromeVm
            )
        }

        composable("CheckweigherSystemScreen/{systemId}") { backStackEntry ->
            val systemId = backStackEntry.arguments?.getString("systemId")?.toIntOrNull() ?: 0
            CheckweigherSystemScreen(
                navController = navController,
                repositoryCW = repositoryCwSystems,
                notesRepository = cwNotesRepository,
                systemId = systemId,
                chromeVm = chromeVm,
                snackbarHostState = snackbarHostState
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
                notesRepository = notesRepository,
                systemId = systemId,
                chromeVm = chromeVm,
                snackbarHostState = snackbarHostState
            )
        }

        composable("myCalibrations") {

            val mdDao = db.metalDetectorConveyorCalibrationDAO()
            val cwDao = db.checkweigherCalibrationDAO()

            val cwCalibrationRepository = CheckweigherCalibrationRepository(cwDao)

            MyCalibrationsScreen(
                mdDao = mdDao,
                cwDao = cwDao,
                customerRepository = repositoryCustomer,
                mdSystemsRepository = repositoryMdSystems,
                cwSystemsRepository = repositoryCwSystems,
                mdCalibrationRepository = calibrationRepository,
                cwCalibrationRepository = cwCalibrationRepository,
                apiService = apiService,
                snackbarHostState = snackbarHostState,
                db = db
            )

        }

        composable("myValidations") {
            val dao = db.sensitivityOptimisationValidationDAO()
            val mdSystemDao = db.mdSystemDAO()
            val sovRepository = SensitivityOptimisationValidationRepository(dao, mdSystemDao)

            MyValidationsScreen(
                dao = dao,
                customerRepository = repositoryCustomer,
                systemsRepository = repositoryMdSystems,
                sovRepository = sovRepository,
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

        composable("addNewCheckweigherScreen/{customerID}/{customerName}"){ backStackEntry ->
            val customerID =
                backStackEntry.arguments?.getString("customerID")?.toIntOrNull() ?: 0
            val customerName =
                backStackEntry.arguments?.getString("customerName") ?: ""

            AddNewCheckweigherScreen(
                navController = navController,
                systemTypeRepository = repositorySystemTypes,
                cwSystemsRepository = repositoryCwSystems,
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
            // DO NOT call URLDecoder.decode manually. 
            // Navigation.compose already handles the decoding of arguments.
            val manualUrl = backStackEntry.arguments?.getString("manualUrl") ?: ""

            ManualViewerScreen(
                modelName = modelName,
                manualUrl = manualUrl,
                chromeVm = chromeVm,
                onBack = { navController.popBackStack() }
            )
        }

    }
}
