package ui

import bot.Bot
import db.DatabaseHelper
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import org.telegram.telegrambots.meta.generics.BotSession
import tornadofx.App
import tornadofx.action
import tornadofx.find
import tornadofx.launch
import ui.main.MainView
import ui.players.PlayersView
import java.io.FileReader
import kotlin.system.exitProcess

class Application:  App(MainView::class) {

    companion object {
        lateinit var instance: Application
            private set
    }

    init { instance = this }

    override fun start(stage: Stage) {
        super.start(stage)

        stage.minWidth = 500.0
        stage.minHeight = 275.0

        ApiContextInitializer.init()

        stage.setOnCloseRequest {
            exitProcess(0)
        }
    }
}

fun main(args: Array<String> = emptyArray()) {
    ApiContextInitializer.init()
    launch<Application>(args)
}