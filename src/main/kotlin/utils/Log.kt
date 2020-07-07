package utils

import db.DATABASE_LOG_FILE
import tornadofx.find
import ui.Application
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

fun info(tag: String, msg: String) {
    val dateFormat = SimpleDateFormat("dd.MM.YYYY HH:mm:ss")
    val logMsg = "${dateFormat.format(Date())}: $tag: \\I: $msg"
    println(logMsg)
    writeToLogFile(logMsg)
    Application.instance.logMessage(logMsg)
}

fun debug(tag: String, msg: String) {
    val dateFormat = SimpleDateFormat("dd.MM.YYYY HH:mm:ss")
    val logMsg = "${dateFormat.format(Date())}: $tag: \\D: $msg"
    println(logMsg)
    writeToLogFile(logMsg)
    Application.instance.logMessage(logMsg)
}

fun error(tag: String, msg: String) {
    val dateFormat = SimpleDateFormat("dd.MM.YYYY HH:mm:ss")
    val logMsg = "${dateFormat.format(Date())}: $tag: \\E: $msg"
    println(logMsg)
    writeToLogFile(logMsg)
    Application.instance.logMessage(logMsg)
}

fun writeToLogFile(msg: String) {
    val file = File(DATABASE_LOG_FILE)
    val logWriter = FileWriter(file, true)
    logWriter.append("$msg\n")
    logWriter.close()
}