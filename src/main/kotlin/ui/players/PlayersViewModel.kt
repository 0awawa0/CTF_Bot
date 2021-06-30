package ui.players

import bot.BotManager
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

        fun sendMessage(text: String) { BotManager.sendMessageToPlayer(dto.id, text) }

        fun pushChanges() { viewModelScope.launch { dto.updateEntity() } }

        fun delete() { viewModelScope.launch { DbHelper.delete(dto) } }
    }

    inner class CompetitionItem(private val dto: CompetitionDTO, val score: Int) {
        val id by dto::id
        val name by dto::name

        fun onSelected() { selectedCompetition = dto }
    }

    private var solvesLoadingMutex = Mutex()
    private var selectedCompetition: CompetitionDTO? = null
        set(value) {
            viewModelScope.launch {
                solvesLoadingMutex.withLock {
                    field = value
                    withContext(Dispatchers.JavaFx) { solves.clear() }

                    if (value == null) return@launch
                    val newSolves = selectedPlayer?.getSolves(value)?.map {
                        SolveItem(it.id, it.getTask().name, it.timestamp)
                    } ?: emptyList()

                    withContext(Dispatchers.JavaFx) { solves.addAll(newSolves) }
                }
            }
        }

    private val scoresLoadingMutex = Mutex()
    private var selectedPlayer: PlayerDTO? = null
        set(value) {
            viewModelScope.launch {
                scoresLoadingMutex.withLock {
                    field = value
                    selectedCompetition = null
                    withContext(Dispatchers.JavaFx) { scores.clear() }

                    if (value == null) return@launch
                    val newScores = DbHelper.getAllCompetitions().map {
                        CompetitionItem(it, value.getCompetitionScore(it))
                    }

                    withContext(Dispatchers.JavaFx) { scores.addAll(newScores) }
                }
            }
        }

    private val players = emptyList<PlayerItem>().toObservable()
    val scoreBoard = SortedList(players) { o1, o2 -> o2.totalScore - o1.totalScore }
    val scores = emptyList<CompetitionItem>().toObservable()
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

    fun broadcastMessage(text: String) { BotManager.broadcastMessage(text) }

    private suspend fun onAddEvent(dto: BaseDTO) {
        when (dto) {
            is PlayerDTO -> withContext(Dispatchers.JavaFx) { players.add(PlayerItem(dto, dto.getTotalScore())) }
            is SolveDTO -> {
                val isForSelectedPlayer = dto.getPlayer().id == selectedPlayer?.id
                if (!isForSelectedPlayer) return

                val isForSelectedCompetition = dto.getTask().getCompetition().id == selectedCompetition?.id
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
//            is ScoreDTO -> {
//                withContext(Dispatchers.JavaFx) {
//                    if (scores.removeIf { it.id == dto.id }) scores.add(ScoreItem(dto, dto.getCompetition().name))
//                }
//            }
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
//            is ScoreDTO -> {
//                withContext(Dispatchers.JavaFx) { scores.removeIf { it.id == dto.id }}
//            }
            is SolveDTO -> {
                withContext(Dispatchers.JavaFx) { solves.removeIf { it.id == dto.id }}
            }
        }
    }
}