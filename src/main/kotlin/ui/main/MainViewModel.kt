package ui.main

import database.CompetitionDTO
import database.DbHelper
import javafx.collections.ObservableList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.toObservable
import ui.BaseViewModel

class MainViewModel: BaseViewModel() {

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
}