
// AppChromeViewModel.kt
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

    fun setMenuClick(handler: (() -> Unit)?) {
        _topBarState.update { it.copy(onMenuClick = handler, showMenu = handler != null) }
    }

    fun topBarForRoute(route: String?): TopBarState {
        return when (route) {

            "notices" -> TopBarState(
                title = "Notices",
                showBack = false,
                showCall = true,
                showMenu = false
            )

            "serviceSelectCustomer" -> TopBarState(
                title = "Select Customer",
                showBack = false,
                showCall = true,
                showMenu = false
            )

            "serviceSelectSystem" -> TopBarState(
                title = "Select a System",
                showBack = true,
                showCall = false,
                showMenu = true
            )

            "settings" -> TopBarState(
                title = "Settings",
                showBack = false,
                showCall = true,
                showMenu = false
            )


            else -> TopBarState(
                title = "",
                showBack = false,
                showCall = false,
                showMenu = false
            )
        }
    }

}
