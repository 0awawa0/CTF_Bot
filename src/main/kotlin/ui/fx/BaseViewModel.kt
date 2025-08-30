package ui.fx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

open class BaseViewModel(private val coroutineContext: CoroutineContext = Dispatchers.Default) {

    protected val viewModelScope: CoroutineScope by lazy {
        CoroutineScope(coroutineContext)
    }

    open fun onViewDock() { }
    open fun onViewUndock() { viewModelScope.cancel() }
}