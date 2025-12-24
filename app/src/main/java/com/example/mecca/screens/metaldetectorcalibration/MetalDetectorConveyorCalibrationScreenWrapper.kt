package com.example.mecca.screens.metaldetectorcalibration

import CalibrationNavigationButtons
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mecca.ApiService
import com.example.mecca.CalibrationBanner
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.ui.theme.MetalDetectorConveyorCalibrationNavGraphContent

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MetalDetectorConveyorCalibrationScreenWrapper(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel,
    calibrationId: String,
    apiService: ApiService
) {
    // Holds the currently visible screen composable
    var currentScreen by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    // Route order for progress & forward/back detection
    val routeOrder = remember {
        listOf(
            "MetalDetectorConveyorCalibrationStart/{calibrationId}",
            "CalMetalDetectorConveyorConveyorDetails",
            "CalMetalDetectorConveyorSystemChecklist",
            "CalMetalDetectorConveyorIndicators",
            "CalMetalDetectorConveyorProductDetails",
            "CalMetalDetectorConveyorSensitivityRequirements",
            "CalMetalDetectorConveyorDetectionSettingsAsFound",
            "CalMetalDetectorConveyorSensitivityAsFound",
            "CalMetalDetectorConveyorFerrousTest",
            "CalMetalDetectorConveyorNonFerrousTest",
            "CalMetalDetectorConveyorStainlessTest",
            "CalMetalDetectorConveyorSmeDetails",
            "CalMetalDetectorConveyorDetectionSettingsAsLeft",
            "CalMetalDetectorConveyorRejectSettings",
            "CalMetalDetectorConveyorInfeedPEC",
            "CalMetalDetectorConveyorLargeMetalTest",
            "CalMetalDetectorConveyorRejectConfirmPEC",
            "CalMetalDetectorConveyorBinFullPEC",
            "CalMetalDetectorConveyorBackupPEC",
            "CalMetalDetectorConveyorAirPressureSensor",
            "CalMetalDetectorConveyorPackCheckSensor",
            "CalMetalDetectorConveyorSpeedSensor",
            "CalMetalDetectorConveyorBinDoorMonitor",
            "CalMetalDetectorConveyorDetectNotification",
            "CalMetalDetectorConveyorSummary"
        )
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Determine direction
    val previousRoute = remember { mutableStateOf<String?>(null) }
    val goingForward = remember(currentRoute) {
        val old = routeOrder.indexOf(previousRoute.value)
        val new = routeOrder.indexOf(currentRoute)
        previousRoute.value = currentRoute
        new > old
    }

    // Progress bar calculation
    val currentIndex = routeOrder.indexOf(currentRoute).coerceAtLeast(0)
    val progress = (currentIndex + 1).toFloat() / routeOrder.size.toFloat()

    val isFirstStep = currentRoute?.startsWith("MetalDetectorConveyorCalibrationStart") == true

    val isNextEnabled by viewModel.currentScreenNextEnabled.collectAsState()


    // For swipe debouncing & accumulation
    var swipeHandled by remember { mutableStateOf(false) }
    var accumulatedDragX by remember { mutableFloatStateOf(0f) }

    // Reset swipe state when screen changes
    LaunchedEffect(currentScreen) {
        swipeHandled = false
        accumulatedDragX = 0f
    }

    Column(Modifier.fillMaxSize()) {

        // Top banner
        CalibrationBanner(
            progress = progress,
            viewModel = viewModel
        )

        // MAIN CONTENT AREA
        Box(
            Modifier
                .weight(1f)
                .pointerInput(currentIndex) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            accumulatedDragX = 0f
                            swipeHandled = false
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val threshold = 80f
                            accumulatedDragX += dragAmount

                            // Swipe RIGHT → previous
                            if (!swipeHandled &&
                                accumulatedDragX > threshold &&
                                currentIndex > 0
                            ) {
                                swipeHandled = true
                                viewModel.persistCurrentScreen(currentRoute ?: "")
                                if (viewModel.shouldSkipToSummary()) {
                                    navController.navigate("MetalDetectorConveyorCalibrationStart/${calibrationId}")
                                } else if (currentIndex > 0) {
                                    navController.navigate(routeOrder[currentIndex - 1])
                                }
                            }

                            // Swipe LEFT → next
                            if (!swipeHandled &&
                                accumulatedDragX < -threshold &&
                                currentIndex < routeOrder.lastIndex &&
                                isNextEnabled
                            ) {
                                swipeHandled = true
                                viewModel.persistCurrentScreen(currentRoute ?: "")

                                if (viewModel.shouldSkipToSummary()) {
                                    navController.navigate("CalMetalDetectorConveyorSummary")
                                } else if (currentIndex < routeOrder.lastIndex) {
                                    navController.navigate(routeOrder[currentIndex + 1])
                                }
                            }
                        },
                        onDragEnd = {
                            // Reset if no navigation happened
                            if (!swipeHandled) {
                                accumulatedDragX = 0f
                            }
                        }
                    )
                }
        ) {

            // Animate ONLY the currently selected screen
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    if (goingForward) {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }
                }
            ) { screen ->
                screen?.invoke()
            }

            // NavGraph lives outside the animation now
            MetalDetectorConveyorCalibrationNavGraphContent(
                navController = navController,
                viewModel = viewModel,
                calibrationId = calibrationId,
                apiService = apiService,
                onScreenChanged = { newScreen ->
                    currentScreen = newScreen
                }
            )
        }

        val isNextEnabled by viewModel.currentScreenNextEnabled.collectAsState()


        // Bottom navigation buttons
        CalibrationNavigationButtons(
            navController = navController,
            viewModel = viewModel,
            isNextEnabled = isNextEnabled,
            isFirstStep = isFirstStep,
            onPreviousClick = {
                viewModel.persistCurrentScreen(currentRoute ?: "")

                // If user skipped the entire flow (Able = No), always go back to Start
                if (viewModel.shouldSkipToSummary()) {
                    navController.navigate("MetalDetectorConveyorCalibrationStart/${calibrationId}")
                    return@CalibrationNavigationButtons
                }

                // Normal back navigation
                if (!isFirstStep && currentIndex > 0) {
                    navController.navigate(routeOrder[currentIndex - 1])
                }
            },
            onNextClick = {
                viewModel.persistCurrentScreen(currentRoute ?: "")
                if (viewModel.shouldSkipToSummary()) {
                    navController.navigate("CalMetalDetectorConveyorSummary")
                } else if (currentIndex < routeOrder.lastIndex) {
                    navController.navigate(routeOrder[currentIndex + 1])
                }
            },
            onCancelClick = {
                viewModel.clearCalibrationData()
            },
            onSaveAndExitClick = {
                viewModel.persistCurrentScreen(currentRoute ?: "")
            }
        )
    }
}
