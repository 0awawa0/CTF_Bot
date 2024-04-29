package ui.fx.main

import bot.BotManager
import database.CompetitionDTO
import database.DbHelper
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.collections.ObservableList
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.toObservable
import ui.fx.BaseViewModel
import utils.Logger

class MainViewModel: BaseViewModel() {

    private val tag = "MainViewModel"

    data class CompetitionItem(
        val id: Long,
        val name: String
    )

    private val mIsRunning = ReadOnlyBooleanWrapper(false)
    val isRunning: ReadOnlyBooleanProperty = mIsRunning.readOnlyProperty

    val competitions: ObservableList<CompetitionItem> = emptyList<CompetitionItem>().toObservable()
    private val dbEvents = DbHelper.eventsPipe

    override fun onViewDock() {
        super.onViewDock()
        viewModelScope.launch {
            competitions.clear()
            competitions.addAll(DbHelper.getAllCompetitions().map { CompetitionItem(it.id, it.name) })
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
                if (BotManager.startBot(competition)) mIsRunning.set(true)
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
                if (BotManager.startForTesting(competition, password)) mIsRunning.set(true)
            } catch (ex: Exception) {
                Logger.error(tag, "Failed to start bot: ${ex.message}\n${ex.stackTraceToString()}")
            }
        }
    }

    fun stopBot() {
        viewModelScope.launch {
            if (BotManager.stopBot()) mIsRunning.set(false)
        }
    }
}