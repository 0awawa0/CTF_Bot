package bot

import database.CompetitionDTO
import database.DbHelper
import kotlinx.coroutines.*
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import utils.Logger
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

class Bot(
    private val credentials: BotCredentials,
    val competition: CompetitionDTO,
    private val testing: Boolean = false,
    private val testingPassword: String = ""
): TelegramLongPollingBot() {

    companion object {
        const val MSG_START = "/start"
        const val MSG_FLAG = "/flag"
        const val MSG_TESTING_PASSWORD = "/testingPassword"
        const val MSG_CONVERT = "/convert"
        const val MSG_TO_HEX = "/toHex"
        const val MSG_TO_DEC = "/toDec"
        const val MSG_TO_BIN = "/toBin"
        const val MSG_TO_STRING = "/toString"
        const val MSG_ROT = "/rot"
        const val MSG_ROT_BRUTE = "/rotBruteForce"
        const val MSG_CHECK_MAGIC = "/checkMagic"
        const val MSG_CHANGE_NAME = "/changeName"
        const val MSG_DELETE = "/delete"
        const val MSG_COMMANDS_HELP = "/commandsHelp"

        const val DATA_MENU = "/menu"
        const val DATA_CURRENT_SCOREBOARD = "/current_scoreboard"
        const val DATA_GLOBAL_SCOREBOARD = "/global_scoreboard"
        const val DATA_TASKS = "/tasks"
        const val DATA_TASK = "/task"
        const val DATA_FILE = "/file"
        const val DATA_COMMANDS = "/commands"

        const val DATA_JPEG_SIGNATURE = "/jpegSignature"
        const val DATA_JPEG_TAIL = "/jpegTail"
        const val DATA_PNG_SIGNATURE = "/pngSignature"
        const val DATA_PNG_HEADER = "/pngHeader"
        const val DATA_PNG_DATA = "/pngData"
        const val DATA_PNG_TAIL = "/pngTail"
        const val DATA_ZIP_SIGNATURE = "/zipSignature"
        const val DATA_RAR_SIGNATURE = "/rarSignature"
        const val DATA_ELF_SIGNATURE = "/elfSignature"
        const val DATA_CLASS_SIGNATURE = "/classSignature"
        const val DATA_PDF_SIGNATURE = "/pdfSignature"
        const val DATA_PDF_TAIL = "/pdfTail"
        const val DATA_PSD_SIGNATURE = "/psdSignature"
        const val DATA_RIFF_SIGNATURE = "/wavAviSignature"
        const val DATA_WAVE_TAG = "/waveTag"
        const val DATA_AVI_TAG = "/aviTag"
        const val DATA_BMP_SIGNATURE = "/bmpSignature"
        const val DATA_DOC_SIGNATURE = "/docSignature"
        const val DATA_VMDK_SIGNATURE = "/vmdkSignature"
        const val DATA_TAR_SIGNATURE = "/tarSignature"
        const val DATA_7ZIP_SIGNATURE = "/7zSignature"
        const val DATA_GZ_SIGNATURE = "/gzSignature"
    }

    private val tag = "Bot"
    private val authorizedForTesting = HashSet<Long>()

    private val threadPool = Executors.newCachedThreadPool()
    private var botScope = CoroutineScope(threadPool.asCoroutineDispatcher())

    private val messageMaker = MessageMaker(WeakReference(this))
    override fun getBotToken(): String { return credentials.token }

    override fun getBotUsername(): String { return credentials.name }

    override fun onUpdateReceived(update: Update?) {
        if (update == null) return

        botScope.launch {
            if (update.hasMessage()) {
                if (update.message.hasText()) {
                    val start = System.nanoTime()
                    answerMessage(update.message)
                    val end = System.nanoTime()
                    Logger.debug(tag, "Message processed in ${(end - start) / 1000000} ms")
                }
                if (update.message.hasSticker()) Logger.info(tag, "Received sticker: ${update.message.sticker.fileId}")
            }
            if (update.hasCallbackQuery()) {
                val start = System.nanoTime()
                answerCallback(update.callbackQuery)
                val end = System.nanoTime()
                Logger.debug(tag, "Callback processed in ${(end - start) / 1000000} ms")
            }
        }
    }

    override fun onClosing() {
        super.onClosing()
        botScope.cancel()
        threadPool.shutdownNow()
    }

    override fun onRegister() {
        super.onRegister()
        botScope = CoroutineScope(threadPool.asCoroutineDispatcher())
    }

    private suspend fun answerMessage(message: Message) {
        val msgText = message.text

        Logger.info(
            tag,
            "Received message from chat id: ${message.chatId} " +
                    "Player name: ${message.from.userName ?: message.from.firstName}. " +
                    "Message $msgText"
        )

        if (!msgText.startsWith("/")) {
            // answer wrong message
            return
        }

        val command = msgText.split(" ").find { it.startsWith("/") }!!
        val content = msgText.replace(command, "").trim()

        if (testing && message.chatId !in authorizedForTesting) {

            try {
                if (command == MSG_TESTING_PASSWORD) {
                    if (content == testingPassword) {
                        authorizedForTesting.add(message.chatId)
                        execute(messageMaker.getMenuMessage(message))
                    } else {
                        execute(messageMaker.getPasswordWrongMessage(message))
                    }
                } else {
                    execute(messageMaker.getPasswordRequestMessage(message.chatId))
                }
            } catch (ex: TelegramApiException) {
                Logger.error(tag, "${ex.message}\n${ex.stackTraceToString()}")
            }
            return
        }

        if (!DbHelper.checkPlayerExists(message.chatId)) {
            execute(messageMaker.getStartMessage(message))
            return
        }

        try {
            when (command) {
                MSG_START -> execute(messageMaker.getMenuMessage(message))
                MSG_FLAG -> execute(messageMaker.getFlagSticker(message, content))
                MSG_CONVERT -> execute(messageMaker.getConvertMessage(message ,content))
                MSG_TO_HEX -> execute(messageMaker.getToHexMessage(message, content))
                MSG_TO_BIN -> execute(messageMaker.getToBinMessage(message, content))
                MSG_TO_DEC -> execute(messageMaker.getToDecMessage(message, content))
                MSG_TO_STRING -> execute(messageMaker.getToStringMessage(message, content))
                MSG_ROT -> execute(messageMaker.getRotMessage(message, content))
                MSG_ROT_BRUTE -> execute(messageMaker.getRotBruteMessage(message, content))
                MSG_CHECK_MAGIC -> execute(messageMaker.getCheckMagicMessage(message, content))
                MSG_CHANGE_NAME -> execute(messageMaker.getChangeNameMessage(message, content))
                MSG_DELETE -> execute(messageMaker.getDeleteMessage(message, content))
                MSG_COMMANDS_HELP -> execute(messageMaker.getCommandsHelpMessage(message))
                else -> execute(messageMaker.getUnknownMessage(message))
            }
        } catch (ex: TelegramApiException) {
            Logger.error(tag, "${ex.message}\n${ex.stackTraceToString()}")
        }
    }

    private suspend fun answerCallback(callback: CallbackQuery) {
        try {
            val answerToCallback = AnswerCallbackQuery()
            answerToCallback.callbackQueryId = callback.id
            execute(answerToCallback)

            Logger.info(
                tag,
                "Received callback from chat id: ${callback.message.chatId} " +
                        "Player name: ${callback.from.userName ?: callback.from.firstName}. " +
                        "Data: ${callback.data}"
            )
            if (testing && callback.message.chatId !in authorizedForTesting) {
                execute(messageMaker.getPasswordRequestMessage(callback.message.chatId))
                return
            }

            if (!DbHelper.checkPlayerExists(callback.message.chatId)) {
                execute(messageMaker.getStartMessage(callback))
                return
            }

            if (!callback.data.startsWith("/")) {
                execute(messageMaker.getErrorMessage(callback.message.chatId))
                return
            }

            val command = callback.data.split(" ").find { it.startsWith("/") }!!
            val content = callback.data.replace(command, "").trim()

            when (command) {
                DATA_FILE -> execute(messageMaker.getFileMessage(callback, content.toLong()))
                DATA_CURRENT_SCOREBOARD -> execute(messageMaker.getCurrentScoreboard(callback))
                DATA_GLOBAL_SCOREBOARD -> execute(messageMaker.getGlobalScoreboard(callback))
                DATA_TASKS -> execute(messageMaker.getTasksMessage(callback))
                DATA_TASK -> execute(messageMaker.getTaskMessage(callback, content.toLong()))
                DATA_MENU -> execute(messageMaker.getMenuMessage(callback))
                DATA_COMMANDS -> execute(messageMaker.getCommandsHelpMessage(callback))

                DATA_JPEG_SIGNATURE, DATA_JPEG_TAIL,
                DATA_PNG_SIGNATURE, DATA_PNG_HEADER,
                DATA_PNG_DATA, DATA_PNG_TAIL,
                DATA_ZIP_SIGNATURE, DATA_RAR_SIGNATURE,
                DATA_ELF_SIGNATURE, DATA_CLASS_SIGNATURE,
                DATA_PDF_SIGNATURE, DATA_PDF_TAIL,
                DATA_PSD_SIGNATURE, DATA_RIFF_SIGNATURE,
                DATA_WAVE_TAG, DATA_AVI_TAG,
                DATA_BMP_SIGNATURE, DATA_DOC_SIGNATURE,
                DATA_VMDK_SIGNATURE, DATA_TAR_SIGNATURE,
                DATA_7ZIP_SIGNATURE, DATA_GZ_SIGNATURE -> messageMaker.getMagicData(callback, callback.data)
                else -> messageMaker.getMenuMessage(callback)
            }
        } catch (ex: TelegramApiException) {
            Logger.error(tag, "${ex.message}\n${ex.stackTraceToString()}")
        }
    }

    fun sendMessageToPlayer(id: Long, text: String) {
        botScope.launch {
            execute(messageMaker.getMessageToPlayer(id, text))
        }
    }

    fun broadcastMessage(text: String) {
        botScope.launch {
            for (player in DbHelper.getAllPlayers()) {
                sendMessageToPlayer(player.id, text)
                delay(100)
            }
        }
    }
}