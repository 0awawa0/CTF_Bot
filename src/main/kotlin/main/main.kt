package main

import bot.Bot
import db.DatabaseHelper
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import org.telegram.telegrambots.meta.generics.BotSession
import ui.Application
import ui.MainWindow
import java.awt.Dimension
import java.io.FileReader
import javax.swing.JFrame
import javax.swing.text.DefaultCaret


lateinit var bot: Bot
lateinit var botSession: BotSession
lateinit var mW: MainWindow

fun main(args: Array<String> = emptyArray()) {

    val botCredentials = FileReader("./BotCredentials").readLines()
    val token = botCredentials[0].split(":|:")[1].trim()
    val botName = botCredentials[1].split(":|:")[1].trim()

    val botApi = TelegramBotsApi()
    ApiContextInitializer.init()

    tornadofx.launch<Application>(args)

    mW = MainWindow()
    val frame = JFrame("DonNU CTF Bot")
    frame.contentPane = mW.root
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.size = Dimension(1000, 800)

    main.mW.startBotButton.isEnabled = true
    main.mW.startTestingBotButton.isEnabled = true

    main.mW.startBotButton.addActionListener {
        main.mW.startBotButton.isEnabled = false
        main.mW.startTestingBotButton.isEnabled = false

        main.bot = Bot(token = token, botName = botName)
        DatabaseHelper.init()
        try {
            main.botSession = botApi.registerBot(main.bot)
        } catch (e: TelegramApiRequestException) {
            utils.error("Main", e.message.toString())
        }
    }

    main.mW.startTestingBotButton.addActionListener {
        main.mW.startBotButton.isEnabled = false
        main.mW.startTestingBotButton.isEnabled = false
        main.bot = Bot(true, main.mW.testingPassword.text.toString(), token = token, botName = botName)
        DatabaseHelper.init()
        try {
            main.botSession = botApi.registerBot(main.bot)
        } catch (e: TelegramApiRequestException) {
            utils.error("Main", e.message.toString())
        }
    }

    (main.mW.log.caret as DefaultCaret).updatePolicy = DefaultCaret.ALWAYS_UPDATE

    frame.pack()
    frame.isVisible = true
}