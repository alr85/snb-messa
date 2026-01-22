package com.example.mecca.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mecca.AppChromeViewModel
import com.example.mecca.AppDatabase
import com.example.mecca.repositories.CustomerRepository
import com.example.mecca.repositories.MetalDetectorModelsRepository
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.repositories.RetailerSensitivitiesRepository
import com.example.mecca.repositories.SystemTypeRepository
import com.example.mecca.RetrofitClient
import com.example.mecca.UserViewModel
import com.example.mecca.screens.AboutAppScreen
import com.example.mecca.screens.AddNewMetalDetectorScreen
import com.example.mecca.screens.DatabaseSyncScreen
import com.example.mecca.screens.HomeScreen
import com.example.mecca.screens.MessagesHomeScreen
import com.example.mecca.screens.MetalDetectorConveyorSystemScreen
import com.example.mecca.screens.MyCalibrationsScreen
import com.example.mecca.screens.ServiceSelectCustomerScreen
import com.example.mecca.screens.ServiceSelectSystemScreen
import com.example.mecca.screens.SettingsScreen
import com.example.mecca.util.LogConsole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(navController: NavHostController, db: AppDatabase, userViewModel: UserViewModel, chromeVm: AppChromeViewModel) {
    val apiService = RetrofitClient.instance
    val repositoryCustomer = CustomerRepository(apiService, db)
    val repositoryMdModels = MetalDetectorModelsRepository(apiService, db)
    val repositoryMdSystems = MetalDetectorSystemsRepository(apiService, db)
    val repositorySystemTypes = SystemTypeRepository(apiService, db)
    val repositoryDetectionLevels = RetailerSensitivitiesRepository(apiService, db)


    NavHost(navController = navController, startDestination = "serviceHome") {

        composable("logsScreen") { LogConsole(chromeVm = chromeVm) }

        composable("serviceHome") { HomeScreen(navController, chromeVm = chromeVm) }

        composable("menu") { SettingsScreen(navController, userViewModel, chromeVm = chromeVm) }

        composable("databaseSync") {
            DatabaseSyncScreen(
                repositoryCustomer,
                repositoryMdModels,
                repositoryMdSystems,
                repositorySystemTypes,
                repositoryDetectionLevels,
                chromeVm = chromeVm
            )
        }
        composable("aboutApp") { AboutAppScreen(chromeVm = chromeVm) }

        composable("messagesHomeScreen") { MessagesHomeScreen(navController, chromeVm = chromeVm) }

        composable("serviceSelectCustomer") {
            ServiceSelectCustomerScreen(
                navController,
                db,
                repositoryCustomer,
                chromeVm
            )
        }
        composable("calibrationSearchSystem/{customerID}/{customerName}/{customerPostcode}") { backStackEntry ->
            val customerID =
                backStackEntry.arguments?.getString("customerID")?.toIntOrNull() ?: 0
            val customerName = backStackEntry.arguments?.getString("customerName") ?: ""
            val customerPostcode = backStackEntry.arguments?.getString("customerPostcode") ?: ""
            ServiceSelectSystemScreen(
                navController = navController,
                db = db,
                repository = repositoryMdSystems,
                customerID = customerID,
                customerName = customerName,
                customerPostcode = customerPostcode,
                chromeVm = chromeVm
            )
        }
        composable("AddNewMetalDetectorScreen/{customerId}/{customerName}") { backStackEntry ->
            val customerId =
                backStackEntry.arguments?.getString("customerId")?.toIntOrNull() ?: 0
            val customerName = backStackEntry.arguments?.getString("customerName") ?: ""
            AddNewMetalDetectorScreen(
                navController = navController,
                systemTypeRepository = repositorySystemTypes,
                mdModelsRepository = repositoryMdModels,
                customerID = customerId,
                customerName = customerName,
                mdSystemsRepository = repositoryMdSystems,
                chromeVm = chromeVm
            )
        }
        composable("MetalDetectorConveyorSystemScreen/{systemId}") { backStackEntry ->
            val systemId = backStackEntry.arguments?.getString("systemId")?.toIntOrNull() ?: 0
            val dao = db.metalDetectorConveyorCalibrationDAO()
            MetalDetectorConveyorSystemScreen(
                navController = navController,
                repositoryMD = repositoryMdSystems,
                systemId = systemId,
                dao = dao,
                repositoryModels = repositoryMdModels,
                chromeVm = chromeVm
            )
        }

        composable("myCalibrations") {
            val dao = db.metalDetectorConveyorCalibrationDAO()

            MyCalibrationsScreen(
                navController = navController,
                db = db,
                repositoryMD = repositoryMdSystems,
                dao = dao,
                apiService = apiService,
                customerRepository = repositoryCustomer,
                chromeVm = chromeVm
            )
        }

    }

}