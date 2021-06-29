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
import java.util.concurrent.atomic.AtomicBoolean

object BotManager {
    const val CREDENTIALS_FILE = "BotCredentials.json"

    private val tag = "BotManager"

    private var bot: Bot? = null
    private var session: BotSession? = null
    private var operationPending = AtomicBoolean(false)

    fun startBot(competitionDTO: CompetitionDTO): Boolean {
        if (!operationPending.compareAndSet(false, true)) return false

        val file = File(CREDENTIALS_FILE)
        if (!file.exists()) throw FileNotFoundException(CREDENTIALS_FILE)
        val credentials = Json.decodeFromString<BotCredentials>(file.readText())
        Logger.info(tag, "Registering bot...")
        bot = Bot(credentials, competitionDTO)
        val api = TelegramBotsApi(DefaultBotSession::class.java)
        session = api.registerBot(bot)
        Logger.info(tag, "Bot registered")
        return true
    }

    fun startForTesting(competitionDTO: CompetitionDTO, password: String): Boolean {
        if (!operationPending.compareAndSet(false, true)) return false

        val file = File(CREDENTIALS_FILE)
        if (!file.exists()) throw FileNotFoundException(CREDENTIALS_FILE)
        val credentials = Json.decodeFromString<BotCredentials>(file.readText())
        Logger.info(tag, "Registering bot...")
        bot = Bot(credentials, competitionDTO, testing = true, testingPassword = password)
        val api = TelegramBotsApi(DefaultBotSession::class.java)
        session = api.registerBot(bot)
        Logger.info(tag, "Bot registered")
        return true
    }

    fun stopBot(): Boolean {
        if (!operationPending.compareAndSet(false, true)) return false

        Logger.info(tag, "Stopping bot...")
        session?.stop()
        session = null
        Logger.info(tag, "Bot stopped")
        return true
    }

    fun sendMessageToPlayer(id: Long, text: String) { bot?.sendMessageToPlayer(id, text) }

    fun broadcastMessage(text: String) { bot?.broadcastMessage(text) }
}