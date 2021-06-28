package ui.main

import bot.BotManager
import database.CompetitionDTO
import database.DbHelper
import javafx.collections.ObservableList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.toObservable
import ui.BaseViewModel
import utils.Logger

class MainViewModel: BaseViewModel() {

    private val tag = "MainViewModel"

    data class CompetitionItem(
        val id: Long,
        val name: String
    )

    val competitions: ObservableList<CompetitionItem> = emptyList<CompetitionItem>().toObservable()
    private val dbEvents = DbHelper.eventsPipe

    override fun onViewDock() {
        super.onViewDock()
        viewModelScope.launch {
            competitions.clear()
            competitions.addAll(DbHelper.getAllCompetitions().map { CompetitionItem(it.id, it.name) })
            Logger.messages.onEach {  }
            dbEvents.collect { event ->
                when (event) {
                    is DbHelper.DbEvent.Add -> {
                        if (event.dto is CompetitionDTO)
                            withContext(Dispatchers.JavaFx) {
                                competitions.add(CompetitionItem(event.dto.id, event.dto.name))
                            }
                    }
                    is DbHelper.DbEvent.Update -> {
                        if (event.dto is CompetitionDTO) {
                            withContext(Dispatchers.JavaFx) {
                                if (competitions.removeIf { it.id == event.dto.id })
                                    competitions.add(CompetitionItem(event.dto.id, event.dto.name))
                            }
                        }
                    }
                    is DbHelper.DbEvent.Delete -> {
                        if (event.dto is CompetitionDTO) {
                            withContext(Dispatchers.JavaFx) {
                                competitions.removeIf { it.id == event.dto.id }
                            }
                        }
                    }
                }
            }
        }
    }

    fun startBot(competitionItem: CompetitionItem) {
        viewModelScope.launch {
            try {
                val competition = DbHelper.getCompetition(competitionItem.id)
                if (competition == null) {
                    Logger.error(tag, "Failed to start bot, competition not found")
                    return@launch
                }
                BotManager.startBot(competition)
            } catch (ex: Exception) {
                Logger.error(tag, "Failed to start bot: ${ex.message}\n${ex.stackTraceToString()}")
            }
        }
    }

    fun startBotForTesting(competitionItem: CompetitionItem, password: String) {
        viewModelScope.launch {
            try {
                val competition = DbHelper.getCompetition(competitionItem.id)
                if (competition == null) {
                    Logger.error(tag, "Failed to start bot, competition not found")
                    return@launch
                }
                BotManager.startForTesting(competition, password)
            } catch (ex: Exception) {
                Logger.error(tag, "Failed to start bot: ${ex.message}\n${ex.stackTraceToString()}")
            }
        }
    }

    fun stopBot() { BotManager.stopBot() }
}