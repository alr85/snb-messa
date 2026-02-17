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

    fun setTopBar(state: TopBarState) {
        _topBarState.value = state
    }

    fun resetTopBar() {
        _topBarState.value = TopBarState()
    }

    /**
     * Sets the handler for the top-right menu button.
     *
     * IMPORTANT:
     * - The route decides whether a menu icon is shown (showMenu = true in topBarForRoute)
     * - The screen provides what happens when it is clicked (handler)
     *
     * This function only wires the click action and toggles the icon visibility based on handler.
     * If you want the icon always visible via route, you can remove the showMenu change below.
     */
    fun setMenuAction(handler: (() -> Unit)?) {
        _topBarState.update {
            it.copy(
                onMenuClick = handler
            )
        }
    }

    /**
     * One place to decide chrome based on route.
     *
     * NOTE:
     * Routes with arguments must match NavHost patterns exactly.
     */
    fun topBarForRoute(route: String?): TopBarState {
        return when (route) {

            // ----- Bottom nav roots -----
            "serviceSelectCustomer" -> TopBarState(
                title = "Service",
                showBack = false,
                showCall = true,
                showMenu = false
            )

            "notices" -> TopBarState(
                title = "Notices",
                showBack = false,
                showCall = true,
                showMenu = false
            )

            "menu" -> TopBarState(
                title = "Menu",
                showBack = false,
                showCall = true,
                showMenu = false
            )

            // ----- Other screens -----
            "databaseSync" -> TopBarState(
                title = "Database Sync",
                showBack = true,
                showCall = false,
                showMenu = false
            )

            "logsScreen" -> TopBarState(
                title = "Logs",
                showBack = true,
                showCall = false,
                showMenu = false
            )

            "aboutApp" -> TopBarState(
                title = "About",
                showBack = true,
                showCall = false,
                showMenu = false
            )

            "myCalibrations" -> TopBarState(
                title = "My Calibrations",
                showBack = true,
                showCall = false,
                showMenu = false
            )

            // ----- Route WITH arguments (must match NavHost pattern) -----
            "calibrationSearchSystem/{customerID}/{customerName}/{customerPostcode}" -> TopBarState(
                title = "Select a System",
                showBack = true,
                showCall = false,
                showMenu = true
            )

            "addNewMetalDetectorScreen/{customerID}/{customerName}" -> TopBarState(
                title = "Add New Metal Detector",
                showBack = true,
                showCall = false,
                showMenu = false
            )

            // Fallback
            else -> TopBarState(
                title = null,
                showBack = false,
                showCall = false,
                showMenu = false
            )
        }
    }
}
