package ui.players

import bot.BotManager
import database.*
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.ReadOnlyStringWrapper
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
                    withContext(Dispatchers.JavaFx) {
                        scores.clear()
                        solves.clear()
                        solves.addAll(value?.getSolves()?.map { SolveItem(it.id, it.getTask().name, it.timestamp) }
                            ?: emptyList()
                        )
                        mPlayerName.set(value?.name ?: "")
                        mPlayerScore.set(value?.getTotalScore()?.toString() ?: "")
                    }

                    if (value == null) return@launch
                    val newScores = DbHelper.getAllCompetitions().map {
                        CompetitionItem(it, value.getCompetitionScore(it))
                    }

                    withContext(Dispatchers.JavaFx) { scores.addAll(newScores) }
                }
            }
        }

    private val mPlayerName = ReadOnlyStringWrapper("")
    val playerName: ReadOnlyStringProperty = mPlayerName.readOnlyProperty

    private val mPlayerScore = ReadOnlyStringWrapper("")
    val playerScore: ReadOnlyStringProperty = mPlayerScore.readOnlyProperty

    private val players = emptyList<PlayerItem>().toObservable()
    val scoreBoard = SortedList(players) { o1, o2 -> o2.totalScore - o1.totalScore }
    val scores = emptyList<CompetitionItem>().toObservable()
    val solves = emptyList<SolveItem>().toObservable()

    override fun onViewDock() {
        super.onViewDock()
        viewModelScope.launch {
            selectedPlayer = null
            selectedCompetition = null
            withContext(Dispatchers.JavaFx) {
                players.clear()
                players.setAll(DbHelper.getAllPlayers().map {
                    PlayerItem(it, it.getTotalScore())
                })
            }
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
                val player = dto.getPlayer()
                withContext(Dispatchers.JavaFx) {
                    if (players.removeIf { it.id == player.id }) {
                        players.add(PlayerItem(player, player.getTotalScore()))
                    }
                    if (selectedPlayer?.id == player.id) selectedPlayer = player
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
                if (selectedPlayer?.id == dto.id) selectedPlayer = null
            }
            is SolveDTO -> {
                withContext(Dispatchers.JavaFx) { solves.removeIf { it.id == dto.id }}
            }
        }
    }
}