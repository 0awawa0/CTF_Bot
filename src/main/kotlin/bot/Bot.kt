package bot


import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import utils.info


const val MSG_START = "/start"
const val MSG_FLAG = "/flag"
const val MSG_TESTING_PASSWORD = "/testing_password"
const val DATA_MENU = "/menu"
const val DATA_SCOREBOARD = "/scoreboard"
const val DATA_TASKS = "/tasks"
const val DATA_TASK = "/task"
const val DATA_FILE = "/file"
class Bot(private val testing: Boolean = false, private val testingPassword: String = "", private val token: String, private val botName: String): TelegramLongPollingBot() {

    private val authorizedForTesting = ArrayList<Long>()
    private val tag = "Bot"

    override fun getBotUsername(): String { return botName }

    override fun getBotToken(): String { return token }

    override fun onUpdateReceived(update: Update?) {

        if (update == null) return;
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

        info(tag, "Received message from Chat id: ${message.chatId} User: ${message.chat.firstName}. Message: ${message.text}")

        if (testing && message.chatId !in authorizedForTesting) {
            val command = message.text.split(" ").find { it.startsWith("/") }!!
            val content = message.text.replace(command, "").trim()

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
                utils.error(tag, e.toString())
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
                        else -> MessageMaker.getUnknownMessage(message.chatId)
                    }
            )
        } catch (e: TelegramApiException) {
            utils.error(tag, e.toString())
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

            info(tag, "Received message from Chat id: ${callback.message.chatId} User: ${callback.message.chat.firstName}. Callback data: ${callback.data}")
            if (command == DATA_FILE) {
                execute(MessageMaker.getFileMessage(callback.message.chatId, content.toLong()))
                return
            }

            execute(
                    when (command) {

                        DATA_SCOREBOARD -> MessageMaker.getScoreboardMessage(callback.message.chatId)
                        DATA_TASKS -> MessageMaker.getTasksMessage(callback.message.chatId)
                        DATA_TASK -> MessageMaker.getTaskMessage(callback.message.chatId, content.toLong())
                        DATA_MENU -> MessageMaker.getMenuMessage(callback.message.chat.firstName, callback.message.chatId, callback.message.chat.userName)
                        else -> MessageMaker.getMenuMessage(callback.message.chat.firstName, callback.message.chatId, callback.message.chat.userName)
                    }
            )

        } catch (e: TelegramApiException) {
            utils.error(tag, e.toString())
        }
    }
}