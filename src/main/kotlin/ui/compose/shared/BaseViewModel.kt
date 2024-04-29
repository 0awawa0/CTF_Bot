package ui.compose.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

open class BaseViewModel {

    protected val viewModelScope = CoroutineScope(Dispatchers.Default)
}