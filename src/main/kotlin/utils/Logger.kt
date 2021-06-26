package utils

import database.DbHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


object Logger {

    private const val LOG_FILE = "${DbHelper.DATABASE_FOLDER}/log.txt"

    data class Message(
        val tag: String,
        val message: String,
        val importance: Importance
    ) {
        enum class Importance {
            DEBUG,
            INFO,
            ERROR
        }
    }

    private val messagesPipe = MutableSharedFlow<Message>()
    val messages: SharedFlow<Message>
        get() { return messagesPipe.asSharedFlow() }

    private val loggerScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    fun info(tag: String, msg: String) {
        loggerScope.launch {
            val dateFormat = SimpleDateFormat("dd.MM.YYYY HH:mm:ss")
            val logMsg = "${dateFormat.format(Date())}: $tag: \\I: $msg"
            println(logMsg)
            writeToLogFile(logMsg)
            messagesPipe.emit(Message(tag, msg, Message.Importance.INFO))
        }
    }

    fun debug(tag: String, msg: String) {
        loggerScope.launch {
            val dateFormat = SimpleDateFormat("dd.MM.YYYY HH:mm:ss")
            val logMsg = "${dateFormat.format(Date())}: $tag: \\D: $msg"
            println(logMsg)
            writeToLogFile(logMsg)
            messagesPipe.emit(Message(tag, msg, Message.Importance.DEBUG))
        }
    }

    fun error(tag: String, msg: String) {
        loggerScope.launch {
            val dateFormat = SimpleDateFormat("dd.MM.YYYY HH:mm:ss")
            val logMsg = "${dateFormat.format(Date())}: $tag: \\E: $msg"
            println(logMsg)
            writeToLogFile(logMsg)
            messagesPipe.emit(Message(tag, msg, Message.Importance.ERROR))
        }
    }

    private suspend fun writeToLogFile(msg: String) {
        withContext(Dispatchers.IO) {
            val file = File(LOG_FILE)
            val logWriter = FileWriter(file, true)
            logWriter.append("$msg\n")
            logWriter.close()
        }
    }
}