package utils

import db.DATABASE_LOG_FILE
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


object Logger {

    private val logListeners = ArrayList<LogListener>()

    fun info(tag: String, msg: String) {
        val dateFormat = SimpleDateFormat("dd.MM.YYYY HH:mm:ss")
        val logMsg = "${dateFormat.format(Date())}: $tag: \\I: $msg"
        println(logMsg)
        writeToLogFile(logMsg)
        logListeners.forEach { it.onLog(logMsg) }
    }

    fun debug(tag: String, msg: String) {
        val dateFormat = SimpleDateFormat("dd.MM.YYYY HH:mm:ss")
        val logMsg = "${dateFormat.format(Date())}: $tag: \\D: $msg"
        println(logMsg)
        writeToLogFile(logMsg)
        logListeners.forEach { it.onLog(logMsg) }
    }

    fun error(tag: String, msg: String) {
        val dateFormat = SimpleDateFormat("dd.MM.YYYY HH:mm:ss")
        val logMsg = "${dateFormat.format(Date())}: $tag: \\E: $msg"
        println(logMsg)
        writeToLogFile(logMsg)
        logListeners.forEach { it.onLog(logMsg) }
    }

    private fun writeToLogFile(msg: String) {
        val file = File(DATABASE_LOG_FILE)
        val logWriter = FileWriter(file, true)
        logWriter.append("$msg\n")
        logWriter.close()
    }

    fun registerLogListener(listener: LogListener) { logListeners.add(listener) }
    fun unregisterLogListener(listener: LogListener) { logListeners.remove(listener) }
    fun unregisterAllListeners() { logListeners.clear() }
}