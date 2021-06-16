//package bot
//
//
//import db.DatabaseHelper
//import kotlinx.coroutines.*
//import org.telegram.telegrambots.bots.TelegramLongPollingBot
//import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
//import org.telegram.telegrambots.meta.api.objects.*
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException
//import utils.Logger
//import java.util.concurrent.Executors
//
//
//const val MSG_START = "/start"
//const val MSG_FLAG = "/flag"
//const val MSG_TESTING_PASSWORD = "/testing_password"
//const val MSG_CONVERT = "/convert"
//const val MSG_TO_HEX = "/toHex"
//const val MSG_TO_DEC = "/toDec"
//const val MSG_TO_BIN = "/toBin"
//const val MSG_TO_STRING = "/toString"
//const val MSG_ROT = "/rot"
//const val MSG_ROT_BRUTE = "/rotBruteForce"
//const val MSG_CHECK_MAGIC = "/checkMagic"
//
//const val DATA_MENU = "/menu"
//const val DATA_SCOREBOARD = "/scoreboard"
//const val DATA_TASKS = "/tasks"
//const val DATA_TASK = "/task"
//const val DATA_FILE = "/file"
//const val DATA_COMMANDS = "/commands"
//
//const val DATA_JPEG_SIGNATURE = "/jpegSignature"
//const val DATA_JPEG_TAIL = "/jpegTail"
//const val DATA_PNG_SIGNATURE = "/pngSignature"
//const val DATA_PNG_HEADER = "/pngHeader"
//const val DATA_PNG_DATA = "/pngData"
//const val DATA_PNG_TAIL = "/pngTail"
//const val DATA_ZIP_SIGNATURE = "/zipSignature"
//const val DATA_RAR_SIGNATURE = "/rarSignature"
//const val DATA_ELF_SIGNATURE = "/elfSignature"
//const val DATA_CLASS_SIGNATURE = "/classSignature"
//const val DATA_PDF_SIGNATURE = "/pdfSignature"
//const val DATA_PDF_TAIL = "/pdfTail"
//const val DATA_PSD_SIGNATURE = "/psdSignature"
//const val DATA_RIFF_SIGNATURE = "/wavAviSignature"
//const val DATA_WAVE_TAG = "/waveTag"
//const val DATA_AVI_TAG = "/aviTag"
//const val DATA_BMP_SIGNATURE = "/bmpSignature"
//const val DATA_DOC_SIGNATURE = "/docSignature"
//const val DATA_VMDK_SIGNATURE = "/vmdkSignature"
//const val DATA_TAR_SIGNATURE = "/tarSignature"
//const val DATA_7ZIP_SIGNATURE = "/7zSignature"
//const val DATA_GZ_SIGNATURE = "/gzSignature"
//
//class Bot private constructor(
//    private val testing: Boolean = false,
//    private val testingPassword: String = "",
//    private val token: String,
//    private val botName: String,
//    private val ctfName: String
//): TelegramLongPollingBot() {
//
//    class Builder(private val botName: String, private val token: String) {
//        private var testing = false
//        private var testingPassword = ""
//        private var ctfName = ""
//
//        fun setTesting(testing: Boolean, password: String = ""): Builder {
//            this.testing = testing
//            this.testingPassword = password
//            return this
//        }
//
//        fun setCtfName(ctfName: String): Builder {
//            this.ctfName = ctfName
//            return this
//        }
//
//        fun build(): Bot {
//            return Bot(
//                testing,
//                testingPassword,
//                token,
//                botName,
//                ctfName
//            )
//        }
//    }
//
//    private val authorizedForTesting = ArrayList<Long>()
//    private val tag = "Bot"
//    private val threadPool = Executors.newCachedThreadPool()
//    private val botScope = CoroutineScope(threadPool.asCoroutineDispatcher())
//
//    override fun getBotUsername(): String { return botName }
//
//    override fun getBotToken(): String { return token }
//
//    override fun onUpdateReceived(update: Update?) {
//
//        if (update == null) return
//        botScope.launch {
//            if (update.hasMessage()) {
//                if (update.message.hasText()) {
//                    answerMessage(update.message)
//                }
//            }
//
//            if (update.hasCallbackQuery()) answerCallback(update.callbackQuery)
//        }
//    }
//
//
//    private suspend fun answerMessage(message: Message) {
//
//        Logger.info(tag, "Received message from Chat id: ${message.chatId} User: ${message.chat.firstName}. Message: ${message.text}")
//
//        // Check if bot is in testing mode and user is authorized for testing it.
//        if (testing && message.chatId !in authorizedForTesting) {
//
//            // If user is not authorized, check the command.
//            // If its a /testing_password, so check the password.
//            // Else, tell user that he/she is not authorized.
//            val command = message.text.split(" ").find { it.startsWith("/") }
//            val content = message.text.replace(command ?: "", "").trim()
//
//            try {
//                if (command == MSG_TESTING_PASSWORD) {
//                    if (content == testingPassword) {
//                        authorizedForTesting.add(message.chatId)
//                        execute(MessageMaker.getMenuMessage(message.chat.firstName, message.chatId, message.chat.userName))
//                    } else {
//                        execute(MessageMaker.getPasswordWrongMessage(message.chatId))
//                    }
//                    return
//                } else {
//                    execute(MessageMaker.getPasswordRequestMessage(message.chatId))
//                }
//            } catch (e: TelegramApiException) {
//                Logger.error(tag, e.toString())
//            }
//            return
//        }
//
//        // Every message must start with /, because they all are commands.
//        if (!message.text.startsWith("/")) {
//            execute(MessageMaker.getUnknownMessage(message.chatId))
//            return
//        }
//
//        // Split command and it's arguments
//        val command = message.text.split(" ").find { it.startsWith("/") }!!
//        val content = message.text.replace(command, "").trim()
//
//        if (command == MSG_FLAG) {
//            execute(MessageMaker.getFlagSticker(message.chatId, content))
//            return
//        }
//
//        try {
//            // Build the answer for command and answer to user
//            execute(
//                when (command) {
//                    MSG_START -> MessageMaker.getMenuMessage(message.chat.firstName, message.chatId, message.chat.userName)
////                    MSG_FLAG -> MessageMaker.getFlagMessage(message.chatId, content)
//                    MSG_CONVERT -> MessageMaker.getConvertMessage(message.chatId, content)
//                    MSG_TO_HEX -> MessageMaker.getToHexMessage(message.chatId, content)
//                    MSG_TO_BIN -> MessageMaker.getToBinMessage(message.chatId, content)
//                    MSG_TO_DEC -> MessageMaker.getToDecMessage(message.chatId, content)
//                    MSG_TO_STRING -> MessageMaker.getToStringMessage(message.chatId, content)
//                    MSG_ROT -> MessageMaker.getRotMessage(message.chatId, content)
//                    MSG_ROT_BRUTE -> MessageMaker.getRotBruteMessage(message.chatId, content)
//                    MSG_CHECK_MAGIC -> MessageMaker.getCheckMagicMessage(message.chatId, content)
//                    else -> MessageMaker.getUnknownMessage(message.chatId)
//                }
//            )
//        } catch (e: TelegramApiException) {
//            Logger.error(tag, e.toString())
//        }
//    }
//
//
//    private fun answerCallback(callback: CallbackQuery) {
//        try {
//            val answerToCallback = AnswerCallbackQuery()
//            answerToCallback.callbackQueryId = callback.id
//            execute(answerToCallback)
//
//            // Check if the bot is in testing mode and user is authorized for testing
//            if (testing && callback.message.chatId !in authorizedForTesting) {
//                execute(MessageMaker.getPasswordRequestMessage(callback.message.chatId))
//                return
//            }
//
//            // Every callback data must start with / as it's a command
//            if (!callback.data.startsWith("/")) {
//                execute(MessageMaker.getErrorMessage(callback.message.chatId))
//                return
//            }
//
//            // Split the command and data
//            val command = callback.data.split(" ").find { it.startsWith("/") }!!
//            val content = callback.data.replace(command, "").trim()
//
//            Logger.info(tag, "Received message from Chat id: ${callback.message.chatId} User: ${callback.message.chat.firstName}. Callback data: ${callback.data}")
//
//            // Build answer corresponding to the command and answer to user
//            if (command == DATA_FILE) {
//                val splat = content.split(" ")
//                val taskId = splat[0].toLong()
//                val fileName = splat.subList(1, splat.size).joinToString(separator = " ")
//                execute(MessageMaker.getFileMessage(callback.message.chatId, taskId, fileName))
//                return
//            }
//
//            execute(
//                when (command) {
//
//                    DATA_SCOREBOARD -> MessageMaker.getScoreboardMessage(callback.message.chatId)
//                    DATA_TASKS -> MessageMaker.getTasksMessage(callback.message.chatId)
//                    DATA_TASK -> MessageMaker.getTaskMessage(callback.message.chatId, content.toLong())
//                    DATA_MENU -> MessageMaker.getMenuMessage(callback.message.chat.firstName, callback.message.chatId, callback.message.chat.userName)
//                    DATA_COMMANDS -> MessageMaker.getCommandsHelpMessage(callback.message.chatId)
//
//                    DATA_JPEG_SIGNATURE, DATA_JPEG_TAIL,
//                    DATA_PNG_SIGNATURE, DATA_PNG_HEADER,
//                    DATA_PNG_DATA, DATA_PNG_TAIL,
//                    DATA_ZIP_SIGNATURE, DATA_RAR_SIGNATURE,
//                    DATA_ELF_SIGNATURE, DATA_CLASS_SIGNATURE,
//                    DATA_PDF_SIGNATURE, DATA_PDF_TAIL,
//                    DATA_PSD_SIGNATURE, DATA_RIFF_SIGNATURE,
//                    DATA_WAVE_TAG, DATA_AVI_TAG,
//                    DATA_BMP_SIGNATURE, DATA_DOC_SIGNATURE,
//                    DATA_VMDK_SIGNATURE, DATA_TAR_SIGNATURE,
//                    DATA_7ZIP_SIGNATURE, DATA_GZ_SIGNATURE -> MessageMaker.getMagicData(callback.message.chatId, callback.data)
//                    else -> MessageMaker.getMenuMessage(callback.message.chat.firstName, callback.message.chatId, callback.message.chat.userName)
//                }
//            )
//
//        } catch (e: TelegramApiException) {
//            Logger.error(tag, e.toString())
//        }
//    }
//
//    suspend fun sendMessageToPlayer(id: Long, text: String) {
//        execute(MessageMaker.getMessageToPlayer(id, text))
//    }
//
////    suspend fun sendMessageToAll(text: String) {
////        for (player in DatabaseHelper.getAllPlayers()) {
////            sendMessageToPlayer(player.id.value, text)
////            delay(200)
////        }
////    }
//}