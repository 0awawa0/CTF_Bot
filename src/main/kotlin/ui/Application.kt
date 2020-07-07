package ui

import bot.Bot
import db.DatabaseHelper
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
import java.io.FileReader
import kotlin.system.exitProcess

class Application:  App(MainView::class) {

    private val mainView: MainView by lazy { find(MainView::class) }

    private var bot: Bot? = null
    private var botSession: BotSession? = null

    companion object {
        lateinit var instance: Application
            private set
    }

    init { instance = this }
    override fun start(stage: Stage) {
        super.start(stage)

        stage.minWidth = 350.0
        stage.minHeight = 275.0

        val botCredentials = FileReader("./BotCredentials").readLines()
        val token = botCredentials[0].split(":|:")[1].trim()
        val botName = botCredentials[1].split(":|:")[1].trim()

        val botApi = TelegramBotsApi()
        ApiContextInitializer.init()

        mainView.startBotButton.action {
            bot = Bot(token = token, botName = botName)
            DatabaseHelper.init()
            try {
                botSession = botApi.registerBot(bot)
            } catch (e: TelegramApiRequestException) {
                utils.error("Main", e.message.toString())
            }
        }

        mainView.startTestingButton.action {
            bot = Bot(
                true,
                mainView.tfTestingPassword.text,
                token = token,
                botName = botName
            )
            DatabaseHelper.init()
            try {
                botSession = botApi.registerBot(bot)
            } catch (e: TelegramApiRequestException) {
                utils.error("Main", e.message.toString())
            }
        }

        stage.setOnCloseRequest {
            exitProcess(0)
        }
    }

    fun logMessage(message: String) {
        mainView.taLog.appendText("$message\n")
    }
}

fun main(args: Array<String> = emptyArray()) {
    ApiContextInitializer.init()
    launch<Application>(args)
}