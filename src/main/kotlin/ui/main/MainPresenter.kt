package ui.main

import bot.Bot
import db.DatabaseHelper
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import org.telegram.telegrambots.meta.generics.BotSession
import utils.Logger
import java.io.FileReader

class MainPresenter(private val view: MainView) {

    private var bot: Bot? = null
    private var botSession: BotSession? = null
    private val botApi = TelegramBotsApi()

    private val botCredentials = FileReader("./BotCredentials").readLines()
    private val token = botCredentials[0].split(":|:")[1].trim()
    private val botName = botCredentials[1].split(":|:")[1].trim()


    fun startBot() {
        bot = Bot(token = token, botName = botName)
        try {
            botSession = botApi.registerBot(bot)
        } catch (e: TelegramApiRequestException) {
            Logger.error("Main", e.message.toString())
        }
    }

    fun startTestingBot(password: String) {
        bot = Bot(
            true,
            password,
            token = token,
            botName = botName
        )
        DatabaseHelper.init()
        try {
            botSession = botApi.registerBot(bot)
        } catch (e: TelegramApiRequestException) {
            Logger.error("Main", e.message.toString())
        }
    }
}