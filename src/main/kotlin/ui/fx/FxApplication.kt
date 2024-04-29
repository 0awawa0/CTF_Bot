package ui.fx

import database.DbHelper
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tornadofx.App
import tornadofx.launch
import ui.fx.main.MainView
import kotlin.system.exitProcess

object FxApplication {
    class Application : App(MainView::class) {

        override fun start(stage: Stage) {
            super.start(stage)

            stage.isMaximized = true

            stage.setOnCloseRequest {
                exitProcess(0)
            }
        }
    }

    fun main(args: Array<String>) {
        CoroutineScope(Dispatchers.IO).launch { DbHelper.init() }
        launch<Application>(args)
    }
}