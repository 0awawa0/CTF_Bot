package ui.compose.main

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import bot.BotManager
import database.DbHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.compose.shared.LogDebugColor
import ui.compose.shared.LogErrorColor
import ui.compose.shared.LogInfoColor
import ui.compose.shared.dto.Competition
import utils.Logger

class MainViewModel {

    data class LogMessage(
        val message: String,
        val color: Color
    )

    private val selectedCompetitionId: MutableStateFlow<Long?> = MutableStateFlow(null)

    val canStart = BotManager.botRunning.combine(selectedCompetitionId) { running, selected ->
        !running && (selected != null)
    }
    val started = BotManager.botRunning

    private val _competitions = mutableStateMapOf<Long?, Competition>()
    val competitions: List<Competition> get() = _competitions.values.toList().sortedBy { it.id }

    private val _log = mutableStateListOf<LogMessage>()
    val log: List<LogMessage> get() = _log

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    init {
        viewModelScope.launch { Logger.messages.collect { _log.add(it.mapToLogMessage()) }  }
    }

    suspend fun updateCompetitionsList() {
        withContext(Dispatchers.Default) {
            _competitions.clear()
            _competitions.putAll(DbHelper.getAllCompetitions().map { it.id to Competition(it.id, it.name) })

            _competitions[selectedCompetitionId.value]?.let {
                _competitions[selectedCompetitionId.value] = it.copy(selected = true)
            } ?: run { selectedCompetitionId.value = null }
        }
    }

    fun onSelected(competition: Competition) {
        _competitions[selectedCompetitionId.value]?.let {
            _competitions[selectedCompetitionId.value] = it.copy(selected = false)
        }
        _competitions[competition.id]?.let {
            _competitions[competition.id] = competition.copy(selected = true)
            selectedCompetitionId.value = competition.id
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
            Logger.Message.Importance.DEBUG -> LogDebugColor
            Logger.Message.Importance.INFO -> LogInfoColor
            Logger.Message.Importance.ERROR -> LogErrorColor
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