package bot


import db.DatabaseHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import utils.Logger


const val MSG_START = "/start"
const val MSG_FLAG = "/flag"
const val MSG_TESTING_PASSWORD = "/testing_password"
const val MSG_CONVERT = "/convert"
const val MSG_TO_HEX = "/toHex"
const val MSG_TO_DEC = "/toDec"
const val MSG_TO_BIN = "/toBin"
const val MSG_TO_STRING = "/toString"
const val MSG_ROT = "/rot"

const val DATA_MENU = "/menu"
const val DATA_SCOREBOARD = "/scoreboard"
const val DATA_TASKS = "/tasks"
const val DATA_TASK = "/task"
const val DATA_FILE = "/file"
const val DATA_COMMANDS = "/commands"

class Bot private constructor(
    private val testing: Boolean = false,
    private val testingPassword: String = "",
    private val token: String,
    private val botName: String,
    private val ctfName: String
): TelegramLongPollingBot() {


    class Builder {
        private var testing = false
        private var testingPassword = ""
        private var token = ""
        private var botName = ""
        private var ctfName = ""

        fun setTesting(testing: Boolean): Builder {
            this.testing = testing
            return this
        }

        fun setTestingPassword(testingPassword: String): Builder {
            this.testingPassword = testingPassword
            return this
        }

        fun setToken(token: String): Builder {
            this.token = token
            return this
        }

        fun setBotName(botName: String): Builder {
            this.botName = botName
            return this
        }

        fun setCtfName(ctfName: String): Builder {
            this.ctfName = ctfName
            return this
        }

        fun build(): Bot {
            return Bot(
                testing,
                testingPassword,
                token,
                botName,
                ctfName
            )
        }
    }

    init { MessageMaker.ctfName = ctfName }

    private val authorizedForTesting = ArrayList<Long>()
    private val tag = "Bot"

    override fun getBotUsername(): String { return botName }

    override fun getBotToken(): String { return token }

    override fun onUpdateReceived(update: Update?) {

        if (update == null) return
        GlobalScope.launch {
            if (update.hasMessage()) {
                if (update.message.hasText()) {
                    answerMessage(update.message)
                }
            }

            if (update.hasCallbackQuery()) {
                answerCallback(update.callbackQuery)
            }
        }
    }


    private fun answerMessage(message: Message) {

        Logger.info(tag, "Received message from Chat id: ${message.chatId} User: ${message.chat.firstName}. Message: ${message.text}")

        if (testing && message.chatId !in authorizedForTesting) {
            val command = message.text.split(" ").find { it.startsWith("/") }
            val content = message.text.replace(command ?: "", "").trim()

            try {
                if (command == MSG_TESTING_PASSWORD) {
                    if (content == testingPassword) {
                        authorizedForTesting.add(message.chatId)
                        execute(MessageMaker.getMenuMessage(message.chat.firstName, message.chatId, message.chat.userName))
                    } else {
                        execute(MessageMaker.getPasswordWrongMessage(message.chatId))
                    }
                    return
                } else {
                    execute(MessageMaker.getPasswordRequestMessage(message.chatId))
                }
            } catch (e: TelegramApiException) {
                Logger.error(tag, e.toString())
            }
            return
        }

        if (!message.text.startsWith("/")) {
            execute(MessageMaker.getUnknownMessage(message.chatId))
            return
        }
        val command = message.text.split(" ").find { it.startsWith("/") }!!
        val content = message.text.replace(command, "").trim()

        try {
            execute(
                when (command) {
                    MSG_START -> MessageMaker.getMenuMessage(message.chat.firstName, message.chatId, message.chat.userName)
                    MSG_FLAG -> MessageMaker.getFlagMessage(message.chatId, content)
                    MSG_CONVERT -> MessageMaker.getConvertMessage(message.chatId, content)
                    MSG_TO_HEX -> MessageMaker.getToHexMessage(message.chatId, content)
                    MSG_TO_BIN -> MessageMaker.getToBinMessage(message.chatId, content)
                    MSG_TO_DEC -> MessageMaker.getToDecMessage(message.chatId, content)
                    MSG_TO_STRING -> MessageMaker.getToStringMessage(message.chatId, content)
                    MSG_ROT -> MessageMaker.getRotMessage(message.chatId, content)
                    else -> MessageMaker.getUnknownMessage(message.chatId)
                }
            )
        } catch (e: TelegramApiException) {
            Logger.error(tag, e.toString())
        }
    }


    private fun answerCallback(callback: CallbackQuery) {
        try {
            val answerToCallback = AnswerCallbackQuery()
            answerToCallback.callbackQueryId = callback.id
            execute(answerToCallback)

            if (testing && callback.message.chatId !in authorizedForTesting) {
                execute(MessageMaker.getPasswordRequestMessage(callback.message.chatId))
                return
            }

            if (!callback.data.startsWith("/")) {
                execute(MessageMaker.getErrorMessage(callback.message.chatId))
                return
            }

            val command = callback.data.split(" ").find { it.startsWith("/") }!!
            val content = callback.data.replace(command, "").trim()

            Logger.info(tag, "Received message from Chat id: ${callback.message.chatId} User: ${callback.message.chat.firstName}. Callback data: ${callback.data}")
            if (command == DATA_FILE) {
                val splat = content.split(" ")
                val taskId = splat[0].toLong()
                val fileName = splat.subList(1, splat.size).joinToString(separator = " ")
                execute(MessageMaker.getFileMessage(callback.message.chatId, taskId, fileName))
                return
            }

            execute(
                when (command) {

                    DATA_SCOREBOARD -> MessageMaker.getScoreboardMessage(callback.message.chatId)
                    DATA_TASKS -> MessageMaker.getTasksMessage(callback.message.chatId)
                    DATA_TASK -> MessageMaker.getTaskMessage(callback.message.chatId, content.toLong())
                    DATA_MENU -> MessageMaker.getMenuMessage(callback.message.chat.firstName, callback.message.chatId, callback.message.chat.userName)
                    DATA_COMMANDS -> MessageMaker.getCommandsHelpMessage(callback.message.chatId)
                    else -> MessageMaker.getMenuMessage(callback.message.chat.firstName, callback.message.chatId, callback.message.chat.userName)
                }
            )

        } catch (e: TelegramApiException) {
            Logger.error(tag, e.toString())
        }
    }

    suspend fun sendMessageToPlayer(id: Long, text: String) {
        execute(MessageMaker.getMessageToPlayer(id, text))
    }

    suspend fun sendMessageToAll(text: String) {
        for (player in DatabaseHelper.getAllPlayers()) {
            sendMessageToPlayer(player.id.value, text)
            delay(200)
        }
    }
}