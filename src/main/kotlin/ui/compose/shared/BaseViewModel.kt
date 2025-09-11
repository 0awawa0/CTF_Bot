package ui.compose.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

open class BaseViewModel {

    protected val viewModelScope
        get() = _viewModelScope

    private var _viewModelScope = CoroutineScope(Dispatchers.Default)

    open fun onStart() {
        _viewModelScope = CoroutineScope(Dispatchers.Default)
    }

    open fun onDispose() {
        _viewModelScope.cancel()
    }
}