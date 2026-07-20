package com.snb.inspect.screens.service.cwCalibration

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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.snb.inspect.ApiService
import com.snb.inspect.CalibrationBanner
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.ui.theme.FormBackground
import com.snb.inspect.ui.theme.CheckweigherCalibrationNavGraphContent
import com.snb.inspect.util.InAppLogger
import com.snb.inspect.formModules.LocalCalibrationCurrentRoute
import com.snb.inspect.formModules.LocalCalibrationNavController
import com.snb.inspect.formModules.LocalCalibrationRouteOrder
import com.snb.inspect.formModules.LocalCalibrationViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CheckweigherCalibrationScreenWrapper(
    navController: NavHostController,
    viewModel: CalibrationCheckweigherViewModel,
    calibrationId: String,
    apiService: ApiService,
    windowSizeClass: WindowSizeClass
) {
    var currentScreen by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    val canPerformCalibration by viewModel.canPerformCalibration
    val shouldSkip = !canPerformCalibration

    val routeOrder = remember(shouldSkip) {
        if (shouldSkip) {
            listOf(
                "CheckweigherCalibrationStart/{calibrationId}",
                "CwSummary"
            )
        } else {
            listOf(
                "CheckweigherCalibrationStart/{calibrationId}",
                "CwScaleDetails",
                "CwSystemDetails",
                "CwSystemChecklist",
                "CwInfeedSensor",
                "CwRejectConfirmSensor",
                "CwBinFullSensor",
                "CwAirPressureSensor",
                "CwBinDoorMonitor",
                "CwTestProductDetails",
                "CwStaticScaleReference",
                "CwEngineerTestWeight",
                "CwDynamicTestAsFound",
                "CwStaticTestAsFound",
                "CwAdjustmentsMade",
                "CwDynamicTestAsLeft",
                "CwStaticTestAsLeft",
                "CwSummary"
            )
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Safeguard redirect
    LaunchedEffect(currentRoute, routeOrder) {
        if (currentRoute != null) {
            val isInOrder = routeOrder.any { 
                it == currentRoute || (it.contains("{") && currentRoute.startsWith(it.substringBefore("{")))
            }
            if (!isInOrder && !currentRoute.contains("Summary")) {
                navController.navigate("CheckweigherCalibrationStart/$calibrationId") {
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

        val currentIndex = remember(currentRoute, routeOrder) {
            routeOrder.indexOfFirst { 
                it == currentRoute || (it.contains("{") && currentRoute?.startsWith(it.substringBefore("{")) == true)
            }.coerceAtLeast(0)
        }
        val progress = (currentIndex + 1).toFloat() / routeOrder.size.toFloat()
        val isFirstStep = currentRoute?.startsWith("CheckweigherCalibrationStart") == true
        val isCurrentScreenValid by viewModel.currentScreenNextEnabled.collectAsState()

        val isNextEnabled = remember(currentRoute, isCurrentScreenValid) {
            val nextIndex = currentIndex + 1
            if (nextIndex < routeOrder.size) {
                val nextRoute = routeOrder[nextIndex]
                if (nextRoute.contains("Summary")) {
                    viewModel.isCalibrationValid(routeOrder)
                } else {
                    true
                }
            } else {
                false
            }
        }

        var swipeHandled by remember { mutableStateOf(false) }
        var accumulatedDragX by remember { mutableStateOf(0f) }

        LaunchedEffect(currentScreen) {
            swipeHandled = false
            accumulatedDragX = 0f
        }

        val focusManager = LocalFocusManager.current

        Column(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            // Need a compatible banner or reuse existing
            CalibrationBanner(
                progress = progress,
                viewModel = viewModel,
                windowSizeClass = windowSizeClass
            )

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
                                if (!swipeHandled && accumulatedDragX > threshold && currentIndex > 0) {
                                    swipeHandled = true
                                    viewModel.persistCurrentScreen(currentRoute ?: "")
                                    navController.navigate(routeOrder[currentIndex - 1])
                                }
                                if (!swipeHandled && accumulatedDragX < -threshold && currentIndex < routeOrder.lastIndex && isNextEnabled) {
                                    swipeHandled = true
                                    viewModel.persistCurrentScreen(currentRoute ?: "")
                                    navController.navigate(routeOrder[currentIndex + 1])
                                }
                            }
                        )
                    }
            ) {
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

                CheckweigherCalibrationNavGraphContent(
                    navController = navController,
                    viewModel = viewModel,
                    calibrationId = calibrationId,
                    apiService = apiService,
                    onScreenChanged = { newScreen: @Composable () -> Unit -> currentScreen = newScreen }
                )
            }

            CalibrationNavigationButtons(
                viewModel = viewModel,
                isNextEnabled = isNextEnabled,
                isFirstStep = isFirstStep,
                onPreviousClick = {
                    viewModel.persistCurrentScreen(currentRoute ?: "")
                    if (!isFirstStep && currentIndex > 0) {
                        navController.navigate(routeOrder[currentIndex - 1])
                    }
                },
                onNextClick = {
                    viewModel.persistCurrentScreen(currentRoute ?: "")
                    if (currentIndex < routeOrder.lastIndex) {
                        navController.navigate(routeOrder[currentIndex + 1])
                    }
                },
                onCancelClick = {
                    viewModel.clearCalibrationData()
                },
                onSaveAndExitClick = {
                    viewModel.persistCurrentScreen(currentRoute ?: "")
                },
                windowSizeClass = windowSizeClass
            )
        }
    }
}
