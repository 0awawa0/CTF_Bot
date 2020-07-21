package ui.main

import bot.Bot
import db.DatabaseHelper
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import ui.Application
import ui.Application.Companion.botSession
import utils.Logger
import java.io.FileReader

class MainPresenter(private val view: MainView) {

    private val botApi = TelegramBotsApi()

    private val botCredentials = FileReader("./BotCredentials").readLines()
    private val token = botCredentials[0].split(":|:")[1].trim()
    private val botName = botCredentials[1].split(":|:")[1].trim()

    private var botStarted = false

    fun startBot(ctfName: String) {
        if (!botStarted) {
            Application.bot = Bot.Builder()
                .setToken(token)
                .setBotName(botName)
                .setCtfName(ctfName)
                .build()
        }
        view.onBotStarted()

        try {
            if (!botStarted) {
                botSession = botApi.registerBot(Application.bot)
            }
            botStarted = true
            Logger.info("Bot", "Bot started")
        } catch (e: TelegramApiRequestException) {
            Logger.error("Main", e.message.toString())
        }
    }

    fun startTestingBot(ctfName: String, password: String) {
        if (!botStarted) {
            Application.bot = Bot.Builder()
                .setTesting(true)
                .setTestingPassword(password)
                .setToken(token)
                .setBotName(botName)
                .setCtfName(ctfName)
                .build()
        }

        view.onBotStarted()
        DatabaseHelper.init()
        try {
            if (!botStarted) {
                botSession = botApi.registerBot(Application.bot)
            }
            botStarted = true
            Logger.info("Bot", "Bot started")
        } catch (e: TelegramApiRequestException) {
            Logger.error("Main", e.message.toString())
        }
    }
}