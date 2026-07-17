package com.snb.inspect.screens.service.mdCalibration

import CalibrationNavigationButtons
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.snb.inspect.ApiService
import com.snb.inspect.CalibrationBanner
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.ui.theme.FormBackground
import com.snb.inspect.ui.theme.MetalDetectorConveyorCalibrationNavGraphContent
import com.snb.inspect.util.InAppLogger

import androidx.compose.runtime.CompositionLocalProvider
import com.snb.inspect.formModules.LocalCalibrationCurrentRoute
import com.snb.inspect.formModules.LocalCalibrationNavController
import com.snb.inspect.formModules.LocalCalibrationRouteOrder
import com.snb.inspect.formModules.LocalCalibrationViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MetalDetectorConveyorCalibrationScreenWrapper(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel,
    calibrationId: String,
    apiService: ApiService,
    windowSizeClass: WindowSizeClass
) {
    // Holds the currently visible screen composable
    var currentScreen by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    val pvRequired by viewModel.pvRequired
    val canPerformCalibration by viewModel.canPerformCalibration
    val shouldSkip = !canPerformCalibration

    // Route order for progress & forward/back detection
    val routeOrder = remember(pvRequired, shouldSkip) {
        if (shouldSkip) {
            listOf(
                "MetalDetectorConveyorCalibrationStart/{calibrationId}",
                "CalMetalDetectorConveyorSummary"
            )
        } else {
            listOfNotNull(
                "MetalDetectorConveyorCalibrationStart/{calibrationId}",
                "CalMetalDetectorConveyorConveyorDetails",
                "CalMetalDetectorConveyorSystemChecklist",
                "CalMetalDetectorConveyorIndicators",
                "CalMetalDetectorConveyorProductDetails",
                "CalMetalDetectorConveyorSensitivityRequirements",
                "CalMetalDetectorConveyorDetectionSettingsAsFound",
                "CalMetalDetectorConveyorFerrousTestAsFound",
                "CalMetalDetectorConveyorNonFerrousTestAsFound",
                "CalMetalDetectorConveyorStainlessTestAsFound",
                "CalMetalDetectorConveyorFerrousTest",
                "CalMetalDetectorConveyorNonFerrousTest",
                "CalMetalDetectorConveyorStainlessTest",
                "CalMetalDetectorConveyorDetectionSettingsAsLeft",
                "CalMetalDetectorConveyorRejectSettings",
                "CalMetalDetectorConveyorLargeMetalTest",
                "CalMetalDetectorConveyorInfeedPEC",
                "CalMetalDetectorConveyorRejectConfirmPEC",
                "CalMetalDetectorConveyorBinFullPEC",
                "CalMetalDetectorConveyorAirPressureSensor",
                "CalMetalDetectorConveyorBinDoorMonitor",
                "CalMetalDetectorConveyorBackupPEC",
                "CalMetalDetectorConveyorPackCheckSensor",
                "CalMetalDetectorConveyorSpeedSensor",
                "CalMetalDetectorConveyorDetectNotification",
                if (pvRequired) "CalMetalDetectorConveyorSmeDetails" else null,
                "CalMetalDetectorConveyorEquipmentUsed",
                "CalMetalDetectorConveyorSummary"
            )
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Safeguard: If the current screen is removed from routeOrder (e.g. Able to calibrate changed), 
    // redirect to Start to prevent getting "stuck" on a now-hidden screen.
    LaunchedEffect(currentRoute, routeOrder) {
        if (currentRoute != null) {
            val isInOrder = routeOrder.any { 
                it == currentRoute || (it.contains("{") && currentRoute.startsWith(it.substringBefore("{")))
            }
            if (!isInOrder && !currentRoute.contains("Summary")) {
                navController.navigate("MetalDetectorConveyorCalibrationStart/$calibrationId") {
                    popUpTo(0)
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalCalibrationViewModel provides viewModel,
        LocalCalibrationNavController provides navController,
        LocalCalibrationRouteOrder provides routeOrder,
        LocalCalibrationCurrentRoute provides currentRoute
    ) {
    // Determine direction
    val previousRoute = remember { mutableStateOf<String?>(null) }
    val goingForward = remember(currentRoute) {
        val old = routeOrder.indexOfFirst { 
            it == previousRoute.value || (it.contains("{") && previousRoute.value?.startsWith(it.substringBefore("{")) == true)
        }
        val new = routeOrder.indexOfFirst { 
            it == currentRoute || (it.contains("{") && currentRoute?.startsWith(it.substringBefore("{")) == true)
        }
        previousRoute.value = currentRoute
        new > old
    }

    // Progress bar calculation
    val currentIndex = remember(currentRoute, routeOrder) {
        routeOrder.indexOfFirst { 
            it == currentRoute || (it.contains("{") && currentRoute?.startsWith(it.substringBefore("{")) == true)
        }.coerceAtLeast(0)
    }
    val progress = (currentIndex + 1).toFloat() / routeOrder.size.toFloat()

    val isFirstStep = currentRoute?.startsWith("MetalDetectorConveyorCalibrationStart") == true

    val isCurrentScreenValid by viewModel.currentScreenNextEnabled.collectAsState()
    val screenValidities by viewModel.screenValidities.collectAsState()

    // NEW NAVIGATION LOGIC: 
    // - Forward navigation is allowed everywhere EXCEPT when moving to the Summary screen.
    // - Moving to Summary requires ALL screens to be valid.
    val isNextEnabled = remember(currentRoute, isCurrentScreenValid, routeOrder, screenValidities) {
        val nextIndex = currentIndex + 1
        if (nextIndex < routeOrder.size) {
            val nextRoute = routeOrder[nextIndex]
            if (nextRoute.contains("Summary")) {
                // If the next screen is Summary, we must be valid up to this point
                viewModel.isCalibrationValid(routeOrder)
            } else {
                // Allow forward navigation to any other screen
                true
            }
        } else {
            false
        }
    }


    // For swipe debouncing & accumulation
    var swipeHandled by remember { mutableStateOf(false) }
    var accumulatedDragX by remember { mutableFloatStateOf(0f) }

    // Reset swipe state when screen changes
    LaunchedEffect(currentScreen) {
        swipeHandled = false
        accumulatedDragX = 0f
    }

    val focusManager = LocalFocusManager.current

    Column(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {

        // Top banner
        CalibrationBanner(
            progress = progress,
            viewModel = viewModel,
            windowSizeClass = windowSizeClass
        )

        // MAIN CONTENT AREA
        Box(
            Modifier
                .weight(1f)
                .background(FormBackground)
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
                                navController.navigate(routeOrder[currentIndex - 1])
                            }

                            // Swipe LEFT → next
                            if (!swipeHandled &&
                                accumulatedDragX < -threshold &&
                                currentIndex < routeOrder.lastIndex &&
                                isNextEnabled
                            ) {
                                swipeHandled = true
                                viewModel.persistCurrentScreen(currentRoute ?: "")
                                navController.navigate(routeOrder[currentIndex + 1])
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

//        val isNextEnabled by viewModel.currentScreenNextEnabled.collectAsState()
//

        // Bottom navigation buttons
        CalibrationNavigationButtons(
            viewModel = viewModel,
            isNextEnabled = isNextEnabled,
            isFirstStep = isFirstStep,
            onPreviousClick = {
                viewModel.persistCurrentScreen(currentRoute ?: "")

                // Normal back navigation
                if (!isFirstStep && currentIndex > 0) {
                    navController.navigate(routeOrder[currentIndex - 1])
                    InAppLogger.d("MD Calibration, Navigation from $currentRoute to ${routeOrder[currentIndex - 1]}")
                }
            },
            onNextClick = {
                viewModel.persistCurrentScreen(currentRoute ?: "")
                if (currentIndex < routeOrder.lastIndex) {
                    navController.navigate(routeOrder[currentIndex + 1])
                    InAppLogger.d("MD Calibration, Navigation from $currentRoute to ${routeOrder[currentIndex + 1]}")
                }
            },
            onCancelClick = {
                InAppLogger.d("MD Calibration, Navigation from $currentRoute to Cancel")
                viewModel.clearCalibrationData()
            },
            onSaveAndExitClick = {
                InAppLogger.d("MD Calibration, Navigation from $currentRoute to Save and Exit")
                viewModel.persistCurrentScreen(currentRoute ?: "")
            },
            windowSizeClass = windowSizeClass
        )
    }
}
}
