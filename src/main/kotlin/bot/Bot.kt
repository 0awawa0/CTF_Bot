package bot

import database.CompetitionDTO
import database.DbHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import utils.Logger
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

class Bot(
    private val token: String,
    val name: String,
    val competition: CompetitionDTO,
    private val testing: Boolean = false,
    private val testingPassword: String = ""
): TelegramLongPollingBot() {

    companion object {
        const val MSG_START = "/start"
        const val MSG_FLAG = "/flag"
        const val MSG_TESTING_PASSWORD = "/testing_password"
        const val MSG_CONVERT = "/convert"
        const val MSG_TO_HEX = "/toHex"
        const val MSG_TO_DEC = "/toDec"
        const val MSG_TO_BIN = "/toBin"
        const val MSG_TO_STRING = "/toString"
        const val MSG_ROT = "/rot"
        const val MSG_ROT_BRUTE = "/rotBruteForce"
        const val MSG_CHECK_MAGIC = "/checkMagic"

        const val DATA_MENU = "/menu"
        const val DATA_SCOREBOARD = "/scoreboard"
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
    private val botScope = CoroutineScope(threadPool.asCoroutineDispatcher())

    private val messageMaker = MessageMaker(WeakReference(this))
    override fun getBotToken(): String { return token }

    override fun getBotUsername(): String { return name }

    override fun onUpdateReceived(update: Update?) {
        if (update == null) return

        botScope.launch {
            if (update.hasMessage() && update.message.hasText()) { answerMessage(update.message) }
            if (update.hasCallbackQuery()) answerCallback(update.callbackQuery)
        }
    }

    private suspend fun answerMessage(message: Message) {
        val user = message.from
        val msgText = message.text

        if (!DbHelper.checkPlayerExists(user.id)) {
            return
        }

        Logger.info(
            tag,
            "Received message from chat id: ${user.id} " +
                    "Username: ${user.firstName}. " +
                    "Message $msgText"
        )

        if (!msgText.startsWith("/")) {
            // answer wrong message
            return
        }

        val command = msgText.split(" ").find { it.startsWith("/") }!!
        val content = msgText.replace(command, "").trim()

        if (testing && user.id !in authorizedForTesting) {

            try {
                if (command == MSG_TESTING_PASSWORD) {
                    if (content == testingPassword) {
                        authorizedForTesting.add(user.id)
                        // answer menu
                    }
                } else {
                    // answer wrong password
                }
            } catch (ex: TelegramApiException) {
                Logger.error(tag, "${ex.message}\n${ex.stackTraceToString()}")
            }
            return
        }

        try {
            when (command) {
                MSG_START -> {
                }
                MSG_FLAG -> {
                }
                MSG_CONVERT -> {
                }
                MSG_TO_HEX -> {
                }
                MSG_TO_BIN -> {
                }
                MSG_TO_DEC -> {
                }
                MSG_TO_STRING -> {
                }
                MSG_ROT -> {
                }
                MSG_ROT_BRUTE -> {
                }
                MSG_CHECK_MAGIC -> {
                }
                else -> {
                }
            }
        } catch (ex: TelegramApiException) {
            Logger.error(tag, "${ex.message}\n${ex.stackTraceToString()}")
        }
    }

    private suspend fun answerCallback(callback: CallbackQuery) {

    }
}