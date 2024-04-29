package ui.compose.competitions

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import database.CompetitionModel
import database.DbHelper
import database.TaskDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ui.compose.shared.dto.Competition
import ui.compose.shared.dto.Score
import ui.compose.shared.dto.Task
import utils.Logger
import java.io.File

class CompetitionsViewModel {

    data class AddFromJsonDialogState(val isVisible: Boolean = false)

    data class MessageDialogState(
        val isVisible: Boolean = false,
        val message: String = "",
        val type: Type = Type.Message
    ) {
        enum class Type {
            Message,
            Error
        }
    }

    data class AcceptDialogState(
        val isVisible: Boolean = false,
        val message: String = "",
        val onAccept: () -> Unit = {},
        val onDecline: () -> Unit = {}
    )

    private val tag = "CompetitionsViewModel"

    private val _competitions = mutableStateMapOf<Long?, Competition>()
    private val selectedCompetitionId: MutableStateFlow<Long?> = MutableStateFlow(null)
    private val _addFromJsonDialogState = MutableStateFlow(AddFromJsonDialogState())
    private val _messageDialogState = MutableStateFlow(MessageDialogState())
    private val _acceptDialogState = MutableStateFlow(AcceptDialogState())
    private val _tasks = mutableStateListOf<TaskRow>()
    private val _scoreboard = mutableStateListOf<ScoreboardRow>()

    val competitions: List<Competition> get() = _competitions.values.toList().sortedBy { it.id }
    val selectedCompetition: Competition? get() = _competitions[selectedCompetitionId.value]
    val addFromJsonDialogState: StateFlow<AddFromJsonDialogState> get() = _addFromJsonDialogState
    val messageDialogState: StateFlow<MessageDialogState> get() = _messageDialogState
    val acceptDialogState: StateFlow<AcceptDialogState> get() = _acceptDialogState
    val tasks: List<TaskRow> get() = _tasks
    val scoreboard: List<ScoreboardRow> get() = _scoreboard

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    fun onSelected(competition: Competition) {
        _competitions[selectedCompetitionId.value]?.let {
            _competitions[selectedCompetitionId.value] = it.copy(selected = false)
        }
        _competitions[competition.id]?.let {
            _competitions[competition.id] = competition.copy(selected = true)
            selectedCompetitionId.value = competition.id
            viewModelScope.launch {
                updateTasksList()
                updatePlayersList()
            }
        }
    }

    fun onAddFileFromJson() {
        _addFromJsonDialogState.value = addFromJsonDialogState.value.copy(isVisible = true)
    }

    fun addCompetitionsFromJson(file: File?) {
        _addFromJsonDialogState.value = addFromJsonDialogState.value.copy(isVisible = false)
        file ?: return

        viewModelScope.launch {
            val result = kotlin.runCatching {
                val text = file.readText()
                val parse = Json.decodeFromString<Array<CompetitionModel>>(text)
                for (competition in parse) DbHelper.add(competition)
                updateCompetitionsList()
            }

            result.exceptionOrNull()?.let {
                reportError("Failed to parse competitions from file '${file.name}'.", it)
            }
        }
    }

    fun hideMessage() { _messageDialogState.value = MessageDialogState() }

    fun onCompetitionDeleteClicked() {
        val competitionName = selectedCompetition?.name
        _acceptDialogState.value = AcceptDialogState(
            isVisible = true,
            message = "This will delete competition '$competitionName' from database. This action is unrecoverable. Continue?",
            onAccept = {
                deleteCompetition()
                _acceptDialogState.value = AcceptDialogState()
            },
            onDecline = { _acceptDialogState.value = AcceptDialogState() }
        )
    }

    suspend fun updateCompetitionsList() {
        withContext(Dispatchers.Default) {
            _competitions.clear()
            _competitions.putAll(DbHelper.getAllCompetitions().map { it.id to Competition(it.id, it.name) })

            _competitions[selectedCompetitionId.value]?.let {
                _competitions[selectedCompetitionId.value] = it.copy(selected = true)
            } ?: run { selectedCompetitionId.value = null }
            updateTasksList()
            updatePlayersList()
        }
    }

    private suspend fun updateTasksList() {
        withContext(Dispatchers.Default) {
            val selectedCompetition = selectedCompetitionId.value ?: return@withContext
            val tasks = (DbHelper.getCompetition(selectedCompetition)?.getTasks() ?: emptyList()).map {
                TaskRow(
                    dto = it.toTask(),
                    changesCoroutineScope = viewModelScope,
                    onChangesSaved = ::updateTasksList
                )
            }
            _tasks.clear()
            _tasks.addAll(tasks)
        }
    }

    private suspend fun updatePlayersList() {
        withContext(Dispatchers.Default) {
            val board = selectedCompetitionId.value?.let {
                val competition = DbHelper.getCompetition(it) ?: return@let null
                DbHelper.getScoreboard(competition).map { scoreValue ->
                    ScoreboardRow(Score(scoreValue.first, scoreValue.second))
                }
            } ?: emptyList()
            _scoreboard.clear()
            _scoreboard.addAll(board)
        }
    }

    private fun showMessage(message: String) {
        _messageDialogState.value = MessageDialogState(
            isVisible = true,
            message = message,
            type = MessageDialogState.Type.Message
        )
    }

    private fun deleteCompetition() {
        viewModelScope.launch {
            val result = kotlin.runCatching {
                selectedCompetitionId.value?.let { id ->
                    DbHelper.getCompetition(id)?.let { competition ->
                        DbHelper.delete(competition)
                        showMessage("Competition '${competition.name}' deleted successfully.")
                        updateCompetitionsList()
                    }
                }
            }

            result.exceptionOrNull()?.let {
                reportError("Failed to delete competition from database.", it)
            }
        }
    }

    private suspend fun TaskDTO.toTask() = Task(
        id = id,
        category = category,
        name = name,
        description = description,
        flag = flag,
        attachment = attachment,
        solvesCount = getSolves().count()
    )

    private fun reportError(message: String, exception: Throwable? = null) {
        _messageDialogState.value = MessageDialogState(
            isVisible = true,
            message = message,
            type = MessageDialogState.Type.Error
        )

        Logger.error(
            tag = tag,
            msg = message + exception?.let { "\n${it.message}\n${it.stackTraceToString()}" }
        )
    }
}