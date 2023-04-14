package ui.compose.main

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import database.DbHelper
import org.glassfish.grizzly.localization.LogMessages
import utils.Logger

class MainViewModel {

    data class Competition(
        val id: Long,
        val name: String,
        val selected: Boolean = false
    )

    data class LogMessage(
        val message: String,
        val color: Color
    )

    private val _competitions = mutableStateListOf<Competition>()
    val competitions: List<Competition> get() = _competitions

    private val _log = mutableStateListOf<LogMessage>()
    val log: List<LogMessage> get() = _log

    suspend fun loadCompetitions() {
        _competitions.clear()
        _competitions.addAll(DbHelper.getAllCompetitions().map { Competition(it.id, it.name) })
        Logger.messages.collect {
            _log.add(mapLogMessage(it))
        }
    }

    fun onSelected(id: Long) {
        _competitions.indexOfFirst { it.selected }.let { idx ->
            if (idx >= 0) _competitions[idx] = _competitions[idx].copy(selected = false)
        }

        _competitions.indexOfFirst { it.id == id }.let { idx ->
            if (idx >= 0) _competitions[idx] = _competitions[idx].copy(selected = true)
        }
    }

    fun addToLog() { Logger.info("MainView", "Test message")}

    fun mapLogMessage(message: Logger.Message): LogMessage {
        val text = StringBuilder()
        when (message.importance) {
            Logger.Message.Importance.DEBUG -> text.append("\\D: ")
            Logger.Message.Importance.INFO -> text.append("\\I: ")
            Logger.Message.Importance.ERROR -> text.append("\\E: ")
        }
        text.append(message.message)
        return LogMessage(text.toString(), when(message.importance) {
            Logger.Message.Importance.DEBUG -> Color.Green
            Logger.Message.Importance.INFO -> Color.Blue
            Logger.Message.Importance.ERROR -> Color.Red
        })
    }
}