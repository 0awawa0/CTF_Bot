package ui

import database.DbHelper
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tornadofx.App
import tornadofx.launch
import kotlin.system.exitProcess

class Application:  App(ui.main.MainView::class) {

    override fun start(stage: Stage) {
        super.start(stage)

        stage.isMaximized = true

        stage.setOnCloseRequest {
            exitProcess(0)
        }
    }
}

fun main(args: Array<String> = emptyArray()) {
    CoroutineScope(Dispatchers.IO).launch { DbHelper.init() }
    launch<Application>(args)
}