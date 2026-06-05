package com.snb.inspect.screens.service.sov

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.ui.theme.FormBackground

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SovScreenWrapper(
    navController: NavHostController,
    viewModel: SensitivityOptimisationValidationViewModel,
    windowSizeClass: WindowSizeClass
) {
    var currentScreen by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    val routeOrder = remember {
        listOf(
            "SovStart",
            "SovProductDetails",
            "SovAsFound",
            "SovOptimisation",
            "SovValidation",
            "SovAsLeft",
            "SovComments",
            "SovSummary"
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

    Column(Modifier.fillMaxSize()) {
        SovBanner(
            progress = progress,
            viewModel = viewModel
        )

        Box(
            Modifier
                .weight(1f)
                .background(FormBackground)
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
