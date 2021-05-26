//package ui
//
//import db.DatabaseHelper
//import javafx.stage.Stage
//import org.telegram.telegrambots.ApiContextInitializer
//import org.telegram.telegrambots.meta.generics.BotSession
//import tornadofx.App
//import tornadofx.launch
//import ui.main.MainView
//import utils.Logger
//import kotlin.system.exitProcess
//
//class Application:  App(MainView::class) {
//
//    companion object {
//        lateinit var instance: Application
//            private set
//
////        var bot: Bot? = null
//        var botSession: BotSession? = null
//    }
//
//    init { instance = this }
//
//    override fun start(stage: Stage) {
//        super.start(stage)
//
//        stage.minWidth = 500.0
//        stage.minHeight = 275.0
//
//        ApiContextInitializer.init()
//
//        stage.setOnCloseRequest {
//            exitProcess(0)
//        }
//    }
//
//    override fun stop() {
//        super.stop()
//        Logger.unregisterAllListeners()
//    }
//}
//
//fun main(args: Array<String> = emptyArray()) {
//    ApiContextInitializer.init()
//    DatabaseHelper.init()
//    launch<Application>(args)
//}