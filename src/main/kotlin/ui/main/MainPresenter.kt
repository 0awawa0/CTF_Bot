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


    fun startBot(ctfName: String) {
        bot = Bot.Builder()
            .setToken(token)
            .setBotName(botName)
            .setCtfName(ctfName)
            .build()

        try {
            botSession = botApi.registerBot(bot)
        } catch (e: TelegramApiRequestException) {
            Logger.error("Main", e.message.toString())
        }
    }

    fun startTestingBot(ctfName: String, password: String) {
        bot = Bot.Builder()
            .setTesting(true)
            .setTestingPassword(password)
            .setToken(token)
            .setBotName(botName)
            .setCtfName(ctfName)
            .build()

        DatabaseHelper.init()
        try {
            botSession = botApi.registerBot(bot)
        } catch (e: TelegramApiRequestException) {
            Logger.error("Main", e.message.toString())
        }
    }
}