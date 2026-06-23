package com.snb.inspect.screens.service.sov

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
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.ui.theme.FormBackground

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SovScreenWrapper(
    navController: NavHostController,
    viewModel: SensitivityOptimisationValidationViewModel,
    windowSizeClass: WindowSizeClass,
    apiService: ApiService
) {
    var currentScreen by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    val routeOrder = remember {
        listOf(
            "ValidationStart",
            "ValidationProductDetails",
            "ValidationFerrousTestAsLeft",
            "ValidationNonFerrousTestAsLeft",
            "ValidationStainlessTestAsLeft",
            "ValidationDetectionSettingsAsLeft",
            "ValidationPackValidation",
            "ValidationSummary"
        )
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val previousRoute = remember { mutableStateOf<String?>(null) }
    val goingForward = remember(currentRoute) {
        val old = routeOrder.indexOf(previousRoute.value)
        val new = routeOrder.indexOf(currentRoute)
        previousRoute.value = currentRoute
        new > old
    }

    val currentIndex = routeOrder.indexOf(currentRoute).coerceAtLeast(0)
    val progress = (currentIndex + 1).toFloat() / routeOrder.size.toFloat()
    val isFirstStep = currentIndex == 0
    val isNextEnabled by viewModel.currentScreenNextEnabled.collectAsState()

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
        SovBanner(
            progress = progress,
            viewModel = viewModel
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

                            // Swipe RIGHT → previous
                            if (!swipeHandled &&
                                accumulatedDragX > threshold &&
                                currentIndex > 0
                            ) {
                                swipeHandled = true
                                navController.navigate(routeOrder[currentIndex - 1])
                            }

                            // Swipe LEFT → next
                            if (!swipeHandled &&
                                accumulatedDragX < -threshold &&
                                currentIndex < routeOrder.lastIndex &&
                                isNextEnabled
                            ) {
                                swipeHandled = true
                                viewModel.saveSov()
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

            SovNavGraphContent(
                navController = navController,
                viewModel = viewModel,
                apiService = apiService,
                onScreenChanged = { newScreen ->
                    currentScreen = newScreen
                }
            )
        }

        SovNavigationButtons(
            viewModel = viewModel,
            isNextEnabled = isNextEnabled,
            isFirstStep = isFirstStep,
            onPreviousClick = {
                if (currentIndex > 0) {
                    navController.navigate(routeOrder[currentIndex - 1])
                }
            },
            onNextClick = {
                if (currentIndex < routeOrder.lastIndex) {
                    viewModel.saveSov()
                    navController.navigate(routeOrder[currentIndex + 1])
                }
            },
            onCancelClick = {
                // Handle cancel
            },
            onSaveAndExitClick = {
                viewModel.saveSov()
                // Handle exit
            },
            windowSizeClass = windowSizeClass
        )
    }
}
