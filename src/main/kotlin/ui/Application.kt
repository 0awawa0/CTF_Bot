package ui

import database.DbHelper
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.generics.BotSession
import tornadofx.App
import tornadofx.launch
import utils.Logger
import kotlin.system.exitProcess

class Application:  App(ui.main.MainView::class) {

    companion object {
        lateinit var instance: Application
            private set

//        var bot: Bot? = null
        var botSession: BotSession? = null
    }

    init { instance = this }

    override fun start(stage: Stage) {
        super.start(stage)

        stage.minWidth = 500.0
        stage.minHeight = 275.0
        stage.centerOnScreen()
        ApiContextInitializer.init()

        stage.setOnCloseRequest {
            exitProcess(0)
        }
    }

    override fun stop() {
        super.stop()
        Logger.unregisterAllListeners()
    }
}

fun main(args: Array<String> = emptyArray()) {
    ApiContextInitializer.init()
    CoroutineScope(Dispatchers.IO).launch { DbHelper.init() }
    launch<Application>(args)
}