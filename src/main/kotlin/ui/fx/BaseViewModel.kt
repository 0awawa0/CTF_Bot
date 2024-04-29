package ui.fx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

open class BaseViewModel(private val coroutineContext: CoroutineContext = Dispatchers.Default) {

    protected var viewModelScope: CoroutineScope = CoroutineScope(coroutineContext)
        private set

    open fun onViewDock() { viewModelScope = CoroutineScope(coroutineContext) }
    open fun onViewUndock() { viewModelScope.cancel() }
}