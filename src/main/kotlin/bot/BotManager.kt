package bot

import database.CompetitionDTO
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.generics.BotSession
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import utils.Logger
import java.io.File
import java.io.FileNotFoundException

object BotManager {
    const val CREDENTIALS_FILE = "BotCredentials.json"

    private val tag = "BotManager"

    private var bot: Bot? = null
    private var session: BotSession? = null

    fun startBot(competitionDTO: CompetitionDTO) {
        stopBot()
        val file = File(CREDENTIALS_FILE)
        if (!file.exists()) throw FileNotFoundException(CREDENTIALS_FILE)
        val credentials = Json.decodeFromString<BotCredentials>(file.readText())
        bot = Bot(credentials, competitionDTO)
        val api = TelegramBotsApi(DefaultBotSession::class.java)
        session = api.registerBot(bot)
        Logger.info(tag, "Bot registered")
    }

    fun startForTesting(competitionDTO: CompetitionDTO, password: String) {
        stopBot()
        val file = File(CREDENTIALS_FILE)
        if (!file.exists()) throw FileNotFoundException(CREDENTIALS_FILE)
        val credentials = Json.decodeFromString<BotCredentials>(file.readText())
        bot = Bot(credentials, competitionDTO, testing = true, testingPassword = password)
        val api = TelegramBotsApi(DefaultBotSession::class.java)
        session = api.registerBot(bot)
        Logger.info(tag, "Bot registered")
    }

    fun stopBot() {
        session?.stop()
        session = null
        Logger.info(tag, "Bot stopped")
    }

    fun sendMessageToPlayer(id: Long, text: String) { bot?.sendMessageToPlayer(id, text) }

    fun broadcastMessage(text: String) { bot?.broadcastMessage(text) }
}