package ui.main

import database.CompetitionDTO
import database.DbHelper
import javafx.collections.ObservableList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.toObservable

class MainViewModel {

    val competitions: ObservableList<CompetitionDTO> = emptyList<CompetitionDTO>().toObservable()
    private val dbEvents = DbHelper.eventsPipe
    private var viewModelScope = CoroutineScope(Dispatchers.Default)

    fun onViewDock() {
        viewModelScope.launch {
            competitions.clear()
            competitions.addAll(DbHelper.getAllCompetitions())
            dbEvents.collect { event ->
                when (event) {
                    is DbHelper.DbEvent.Add -> {
                        if (event.dto is CompetitionDTO)
                            withContext(Dispatchers.JavaFx) {
                                competitions.add(event.dto)
                            }
                    }
                    is DbHelper.DbEvent.Update -> {
                        if (event.dto is CompetitionDTO) {
                            withContext(Dispatchers.JavaFx) {
                                if (competitions.removeIf { it.id == event.dto.id })
                                    competitions.add(event.dto)
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

    fun onViewUndock() {
        viewModelScope.cancel()
        viewModelScope = CoroutineScope(Dispatchers.Default)
    }
}