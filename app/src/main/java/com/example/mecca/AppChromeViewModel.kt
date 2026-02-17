package com.example.mecca

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class TopBarState(
    val title: String? = null,
    val showBack: Boolean = false,
    val showCall: Boolean = true,
    val showMenu: Boolean = false,
    val onMenuClick: (() -> Unit)? = null
)

class AppChromeViewModel : ViewModel() {

    private val _topBarState = MutableStateFlow(TopBarState())
    val topBarState: StateFlow<TopBarState> = _topBarState

    /** ROUTE owns chrome (title/back/menu visibility/call) */
    fun applyRouteChrome(state: TopBarState) {
        // Keep the current handler when route changes
        _topBarState.update { current ->
            state.copy(onMenuClick = current.onMenuClick)
        }
    }

    /** SCREEN owns ONLY what happens when menu is clicked */
    fun setMenuAction(handler: (() -> Unit)?) {
        _topBarState.update { it.copy(onMenuClick = handler) }
    }

    fun topBarForRoute(route: String?): TopBarState {
        route ?: return TopBarState()

        return when {
            route.startsWith("MetalDetectorConveyorSystemScreen") -> TopBarState(
                title = "System Details",
                showBack = true,
                showCall = false,
                showMenu = true
            )

            route.startsWith("calibrationSearchSystem") -> TopBarState(
                title = "Select a System",
                showBack = true,
                showCall = false,
                showMenu = true
            )

            route.startsWith("addNewMetalDetectorScreen") -> TopBarState(
                title = "New Metal Detector",
                showBack = true,
                showCall = false,
                showMenu = false
            )

            route == "serviceSelectCustomer" -> TopBarState(
                title = "Service",
                showBack = false,
                showCall = true,
                showMenu = false
            )

            route == "notices" -> TopBarState(
                title = "Notices",
                showBack = false,
                showCall = true,
                showMenu = false
            )

            route == "menu" -> TopBarState(
                title = "Menu",
                showBack = false,
                showCall = true,
                showMenu = false
            )

            route == "databaseSync" -> TopBarState(
                title = "Database Sync",
                showBack = true,
                showCall = false,
                showMenu = false
            )

            route == "logsScreen" -> TopBarState(
                title = "Logs",
                showBack = true,
                showCall = false,
                showMenu = false
            )

            route == "aboutApp" -> TopBarState(
                title = "About",
                showBack = true,
                showCall = false,
                showMenu = false
            )

            route == "myCalibrations" -> TopBarState(
                title = "My Calibrations",
                showBack = true,
                showCall = false,
                showMenu = false
            )



            else -> TopBarState()
        }
    }
}
