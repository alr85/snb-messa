
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
}
