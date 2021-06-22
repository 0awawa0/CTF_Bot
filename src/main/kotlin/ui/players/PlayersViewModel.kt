package ui.players

import database.*
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import tornadofx.toObservable
import ui.BaseViewModel

class PlayersViewModel: BaseViewModel() {

    private val players: ObservableList<PlayerDTO> = emptyList<PlayerDTO>().toObservable()
    val scoreBoard = SortedList(
        players
    ) { o1, o2 ->
        val x = o2.getTotalScoreSynchronous() - o1.getTotalScoreSynchronous()
        return@SortedList (x % Int.MAX_VALUE).toInt()
    }

    private var solvesLoadingMutex = Mutex()
    var selectedScore: ScoreDTO? = null
        set(value) {
            viewModelScope.launch {
                solvesLoadingMutex.withLock {
                    field = value
                    if (value == null) {
                        withContext(Dispatchers.JavaFx) {
                            solves.clear()
                            solves.addAll(selectedPlayer?.getSolves() ?: emptyList())
                        }
                        return@launch
                    }

                    val playerSolves = selectedPlayer?.getSolves() ?: emptyList()
                    val competitionTasks = value.getCompetition().getTasks().map { it.id }
                    val filteredSolves = playerSolves.filter { it.getTask().id in competitionTasks }

                    withContext(Dispatchers.JavaFx) {
                        solves.clear()
                        solves.addAll(filteredSolves)
                    }
                }
            }
        }

    private val scoresLoadingMutex = Mutex()
    var selectedPlayer: PlayerDTO? = null
        set(value) {
            viewModelScope.launch {
                scoresLoadingMutex.withLock {
                    withContext(Dispatchers.JavaFx) {
                        scores.clear()
                        scores.addAll(value?.getScores() ?: emptyList())
                        selectedScore = null
                        field = value
                    }
                }
            }
        }

    val scores: ObservableList<ScoreDTO> = emptyList<ScoreDTO>().toObservable()
    val solves: ObservableList<SolveDTO> = emptyList<SolveDTO>().toObservable()

    override fun onViewDock() {
        super.onViewDock()
        viewModelScope.launch {
            withContext(Dispatchers.JavaFx) {
                players.clear()
                players.setAll(DbHelper.getScoreBoard())
            }

            selectedPlayer = null
            DbHelper.eventsPipe.collect { event ->
                when (event) {
                    is DbHelper.DbEvent.Add -> onAddEvent(event.dto)
                    is DbHelper.DbEvent.Update -> onUpdateEvent(event.dto)
                    is DbHelper.DbEvent.Delete -> onDeleteEvent(event.dto)
                }
            }
        }
    }

    private suspend fun onAddEvent(dto: BaseDTO) {
        when (dto) {
            is PlayerDTO -> withContext(Dispatchers.JavaFx) { players.add(dto) }
            is ScoreDTO -> {
                val isForSelectedPlayer = dto.getPlayer().id == selectedPlayer?.id
                if (isForSelectedPlayer) withContext(Dispatchers.JavaFx) { scores.add(dto) }

            }
            is SolveDTO -> {
                val isForSelectedPlayer = dto.getPlayer().id == selectedPlayer?.id
                if (!isForSelectedPlayer) return

                val isForSelectedCompetition = dto.getTask().getCompetition().id == selectedScore?.getCompetition()?.id
                if (isForSelectedCompetition) withContext(Dispatchers.JavaFx) { solves.add(dto) }
            }
        }
    }

    private suspend fun onUpdateEvent(dto: BaseDTO) {
        when (dto) {
            is PlayerDTO -> {
                withContext(Dispatchers.JavaFx) {
                    if (players.removeIf { it.id == dto.id }) players.add(dto)
                }
            }
            is ScoreDTO -> {
                withContext(Dispatchers.JavaFx) {
                    if (scores.removeIf { it.id == dto.id }) scores.add(dto)
                }
            }
            is SolveDTO -> {
                withContext(Dispatchers.JavaFx) {
                    if (solves.removeIf { it.id == dto.id }) solves.add(dto)
                }
            }
        }
    }

    private suspend fun onDeleteEvent(dto: BaseDTO) {
        when (dto) {
            is PlayerDTO -> {
                withContext(Dispatchers.JavaFx) { players.removeIf { it.id == dto.id } }
            }
            is ScoreDTO -> {
                withContext(Dispatchers.JavaFx) { scores.removeIf { it.id == dto.id }}
            }
            is SolveDTO -> {
                withContext(Dispatchers.JavaFx) { solves.removeIf { it.id == dto.id }}
            }
        }
    }
}