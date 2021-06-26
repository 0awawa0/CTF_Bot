package ui.players

import database.*
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

    data class SolveItem(
        val id: Long,
        val taskName: String,
        val timestamp: Long
    )

    inner class PlayerItem(private val dto: PlayerDTO, var totalScore: Int = 0) {
        val id by dto::id
        var name by dto::name

        fun onSelected() { selectedPlayer = dto }

        fun pushChanges() { viewModelScope.launch { dto.updateEntity() } }

        fun delete() { viewModelScope.launch { DbHelper.delete(dto) } }
    }

    inner class ScoreItem(private val dto: ScoreDTO, competitionName: String = "") {
        val id by dto::id
        var score by dto::score
        var competitionName: String
            private set

        init {
            this.competitionName = competitionName
        }

        fun onSelected() { selectedScore = dto }
        fun pushChanges() { viewModelScope.launch { dto.updateEntity() } }
    }

    private var solvesLoadingMutex = Mutex()
    private var selectedScore: ScoreDTO? = null
        set(value) {
            viewModelScope.launch {
                solvesLoadingMutex.withLock {
                    field = value
                    if (value == null) {
                        withContext(Dispatchers.JavaFx) {
                            solves.clear()
                            solves.addAll(
                                selectedPlayer?.getSolves()?.map {
                                    SolveItem(it.id, it.getTask().name, it.timestamp)
                                } ?: emptyList()
                            )
                        }
                        return@launch
                    }

                    val playerSolves = selectedPlayer?.getSolves() ?: emptyList()
                    val competitionTasks = value.getCompetition().getTasks().map { it.id }
                    val filteredSolves = playerSolves
                        .filter { it.getTask().id in competitionTasks }
                        .map { SolveItem(it.id, it.getTask().name, it.timestamp) }

                    withContext(Dispatchers.JavaFx) {
                        solves.clear()
                        solves.addAll(filteredSolves)
                    }
                }
            }
        }

    private val scoresLoadingMutex = Mutex()
    private var selectedPlayer: PlayerDTO? = null
        set(value) {
            viewModelScope.launch {
                scoresLoadingMutex.withLock {
                    withContext(Dispatchers.JavaFx) {
                        scores.clear()
                        scores.addAll(
                            value?.getScores()?.map {
                                ScoreItem(it, it.getCompetition().name)
                            } ?: emptyList()
                        )
                        selectedScore = null
                        field = value
                    }
                }
            }
        }

    private val players = emptyList<PlayerItem>().toObservable()
    val scoreBoard = SortedList(players) { o1, o2 -> o1.totalScore - o2.totalScore }
    val scores = emptyList<ScoreItem>().toObservable()
    val solves = emptyList<SolveItem>().toObservable()

    override fun onViewDock() {
        super.onViewDock()
        viewModelScope.launch {
            withContext(Dispatchers.JavaFx) {
                players.clear()
                players.setAll(DbHelper.getAllPlayers().map {
                    PlayerItem(it, it.getTotalScore())
                })
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
            is PlayerDTO -> withContext(Dispatchers.JavaFx) { players.add(PlayerItem(dto, dto.getTotalScore())) }
            is ScoreDTO -> {
                val isForSelectedPlayer = dto.getPlayer().id == selectedPlayer?.id
                if (isForSelectedPlayer) withContext(Dispatchers.JavaFx) {
                    scores.add(ScoreItem(dto, dto.getCompetition().name))
                }

            }
            is SolveDTO -> {
                val isForSelectedPlayer = dto.getPlayer().id == selectedPlayer?.id
                if (!isForSelectedPlayer) return

                val isForSelectedCompetition = dto.getTask().getCompetition().id == selectedScore?.getCompetition()?.id
                if (isForSelectedCompetition) withContext(Dispatchers.JavaFx) {
                    solves.add(SolveItem(dto.id, dto.getTask().name, dto.timestamp))
                }
            }
        }
    }

    private suspend fun onUpdateEvent(dto: BaseDTO) {
        when (dto) {
            is PlayerDTO -> {
                withContext(Dispatchers.JavaFx) {
                    if (players.removeIf { it.id == dto.id }) players.add(PlayerItem(dto, dto.getTotalScore()))
                }
            }
            is ScoreDTO -> {
                withContext(Dispatchers.JavaFx) {
                    if (scores.removeIf { it.id == dto.id }) scores.add(ScoreItem(dto, dto.getCompetition().name))
                }
            }
            is SolveDTO -> {
                withContext(Dispatchers.JavaFx) {
                    if (solves.removeIf { it.id == dto.id }) {
                        solves.add(SolveItem(dto.id, dto.getTask().name, dto.timestamp))
                    }
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