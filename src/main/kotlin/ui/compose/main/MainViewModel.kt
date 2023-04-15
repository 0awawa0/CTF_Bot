package ui.compose.main

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import bot.BotManager
import database.DbHelper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
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

    private val selectedCompetitionId: MutableStateFlow<Long?> = MutableStateFlow(null)

    val canStart = BotManager.botRunning.combine(selectedCompetitionId) { running, selected ->
        !running && (selected != null)
    }
    val started = BotManager.botRunning

    private val _competitions = mutableStateListOf<Competition>()
    val competitions: List<Competition> get() = _competitions

    private val _log = mutableStateListOf<LogMessage>()
    val log: List<LogMessage> get() = _log

    suspend fun loadCompetitions() {
        _competitions.clear()
        _competitions.addAll(DbHelper.getAllCompetitions().map { Competition(it.id, it.name) })
        Logger.messages.collect { _log.add(it.mapToLogMessage()) }
    }

    fun onSelected(competition: Competition) {
        _competitions.indexOfFirst { it.selected }.let { idx ->
            if (idx >= 0) _competitions[idx] = _competitions[idx].copy(selected = false)
        }

        _competitions.indexOfFirst { it.id == competition.id }.let { idx ->
            if (idx >= 0) {
                _competitions[idx] = _competitions[idx].copy(selected = true)
                selectedCompetitionId.value = competition.id
            }
        }
    }

    private fun Logger.Message.mapToLogMessage(): LogMessage {
        val text = StringBuilder()
        when (importance) {
            Logger.Message.Importance.DEBUG -> text.append("\\D: ")
            Logger.Message.Importance.INFO -> text.append("\\I: ")
            Logger.Message.Importance.ERROR -> text.append("\\E: ")
        }
        text.append(message)
        return LogMessage(text.toString(), when(importance) {
            Logger.Message.Importance.DEBUG -> Color.Green
            Logger.Message.Importance.INFO -> Color.Blue
            Logger.Message.Importance.ERROR -> Color.Red
        })
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startBot() {
        GlobalScope.launch {
            val id = selectedCompetitionId.value ?: return@launch
            val competition = DbHelper.getCompetition(id) ?: return@launch
            BotManager.startBot(competition)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startTesting(password: String) {
        GlobalScope.launch {
            val id = selectedCompetitionId.value ?: return@launch
            val competition = DbHelper.getCompetition(id) ?: return@launch
            BotManager.startForTesting(competition, password)
        }
    }
    fun stopBot() { BotManager.stopBot() }
}