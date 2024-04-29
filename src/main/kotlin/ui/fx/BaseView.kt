package ui.fx

import tornadofx.View

abstract class BaseView<T: BaseViewModel>(protected val viewModel: T, title: String = ""): View(title) {

    abstract fun refresh()
    override fun onDock() {
        super.onDock()
        viewModel.onViewDock()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.onViewUndock()
    }
}