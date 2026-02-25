package com.example.mecca.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mecca.AppChromeViewModel
import com.example.mecca.AppDatabase
import com.example.mecca.RetrofitClient
import com.example.mecca.UserViewModel
import com.example.mecca.calibrationViewModels.CustomerViewModel
import com.example.mecca.calibrationViewModels.NoticeViewModel
import com.example.mecca.repositories.CustomerRepository
import com.example.mecca.repositories.MetalDetectorModelsRepository
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.repositories.RetailerSensitivitiesRepository
import com.example.mecca.repositories.SystemTypeRepository
import com.example.mecca.screens.mainmenu.HomeScreen
import com.example.mecca.screens.mainmenu.NoticesScreen
import com.example.mecca.screens.mainmenu.ServiceSelectCustomerScreen
import com.example.mecca.screens.mainmenu.SettingsScreen
import com.example.mecca.screens.menu.AboutAppScreen
import com.example.mecca.screens.menu.DatabaseSyncScreen
import com.example.mecca.screens.menu.MyCalibrationsScreen
import com.example.mecca.screens.service.AddNewMetalDetectorScreen
import com.example.mecca.screens.service.MetalDetectorConveyorSystemScreen
import com.example.mecca.screens.service.ServiceSelectSystemScreen
import com.example.mecca.util.LogConsole
import com.example.mecca.util.SyncPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    userViewModel: UserViewModel,
    customerViewModel: CustomerViewModel,
    noticeViewModel: NoticeViewModel,
    db: AppDatabase,
    chromeVm: AppChromeViewModel,
    snackbarHostState: SnackbarHostState
)
 {

    val apiService = RetrofitClient.instance

    val repositoryMdSystems =
        MetalDetectorSystemsRepository(apiService, db)

    val repositorySystemTypes =
        SystemTypeRepository(apiService, db)

    val repositoryMdModels =
        MetalDetectorModelsRepository(apiService, db)

    val context = LocalContext.current
    val syncPrefs = remember { SyncPreferences(context) }

    val repositoryCustomer =
        CustomerRepository(apiService, db, syncPrefs)


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

    }
}
