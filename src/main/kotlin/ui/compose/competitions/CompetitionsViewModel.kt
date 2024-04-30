package ui.compose.competitions

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import database.CompetitionModel
import database.DbHelper
import database.TaskModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ui.compose.shared.BaseViewModel
import ui.compose.shared.components.dialogs.DialogModel
import ui.compose.shared.components.dialogs.FileChooseModel
import ui.compose.shared.components.dialogs.InstanceCreationModel
import ui.compose.shared.dto.Competition
import ui.compose.shared.dto.Score
import ui.compose.shared.dto.toTask
import utils.Logger
import java.io.File

class CompetitionsViewModel: BaseViewModel() {

    private val tag = "CompetitionsViewModel"

    private val _competitions = mutableStateMapOf<Long?, Competition>()
    private val selectedCompetitionId: MutableStateFlow<Long?> = MutableStateFlow(null)
    private val _tasks = mutableStateListOf<TaskRow>()
    private val _scoreboard = mutableStateListOf<ScoreboardRow>()

    private val _fileChooseDialog = MutableStateFlow<FileChooseModel?>(null)
    private val _dialog = MutableStateFlow<DialogModel?>(null)
    private val _instanceCreationDialog = MutableStateFlow<InstanceCreationModel?>(null)

    val competitions: List<Competition> get() = _competitions.values.toList().sortedBy { it.id }
    val selectedCompetition: Competition? get() = _competitions[selectedCompetitionId.value]
    val tasks: List<TaskRow> get() = _tasks
    val scoreboard: List<ScoreboardRow> get() = _scoreboard

    val fileChooseDialog: StateFlow<FileChooseModel?> get() = _fileChooseDialog
    val dialog: StateFlow<DialogModel?> get() = _dialog
    val instanceCreationModel: StateFlow<InstanceCreationModel?> get() = _instanceCreationDialog

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
        _fileChooseDialog.value = FileChooseModel(
            title = "Choose file",
            mode = FileChooseModel.Mode.OPEN_FILE,
            onResult = ::addCompetitionsFromJson
        )
    }

    fun addCompetition() {
        val competitionName = mutableStateOf("")
        _instanceCreationDialog.value = InstanceCreationModel(
            fields = listOf(
                InstanceCreationModel.InstanceField(
                    name = "Competition name",
                    value = competitionName,
                    onValueChanged = { competitionName.value = it }
                )
            ),
            onSave = {
                viewModelScope.launch {
                    DbHelper.add(CompetitionModel(competitionName.value))?.let {
                        addCompetitionFinished(true)
                    } ?: addCompetitionFinished(false)
                }
            },
            onCancel = {
                addCompetitionFinished(false)
            }
        )
    }

    fun onCompetitionDeleteClicked() {
        val competitionName = selectedCompetition?.name
        _dialog.value = DialogModel(
            title = "Competition deletion",
            message = "This will delete competition '$competitionName' from database. This action is unrecoverable. Continue?",
            options = listOf(
                DialogModel.createAcceptOption("OK") {
                    deleteCompetition()
                    _dialog.value = null
                },
                DialogModel.createDenyOption("Cancel") { _dialog.value = null }
            ),
            close = { _dialog.value = null }
        )
    }

    fun exportCompetition() {
        _fileChooseDialog.value = FileChooseModel(
            title = "Choose directory",
            mode = FileChooseModel.Mode.OPEN_DIR,
            onResult = ::onDirectorySelectedForExport
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

    private fun hideMessage() { _dialog.value = null }

    private fun addCompetitionsFromJson(file: File?) {
        _fileChooseDialog.value = null
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

    private fun addCompetitionFinished(isSuccessful: Boolean) {
        _instanceCreationDialog.value = null
        if (isSuccessful) {
            viewModelScope.launch {
                updateCompetitionsList()
                showMessage("New competition created")
            }
        }
    }

    private fun onDirectorySelectedForExport(directory: File?) {
        _fileChooseDialog.value = null
        directory ?: return

        val competitionId = selectedCompetitionId.value ?: return

        viewModelScope.launch {
            try {
                DbHelper.getCompetition(competitionId)?.let { dbo ->
                    val text = Json.encodeToString(arrayOf(CompetitionModel(
                        name = dbo.name,
                        tasks = dbo.getTasks().map {
                            TaskModel(
                                category = it.category,
                                name = it.name,
                                description = it.description,
                                flag = it.flag,
                                attachment = it.attachment
                            )
                        }
                    )))
                    File(directory, "${dbo.name}.json").writeText(text)
                    showMessage("Competition exported successfully")
                }
            } catch (ex: Exception) {
                reportError("Failed to export competition")
                Logger.error(tag, "Failed to export competition to JSON: ${ex.message}\n${ex.stackTraceToString()}")
            }
        }
    }

    private fun showMessage(message: String) {
        _dialog.value = DialogModel(
            message = message,
            title = "Info",
            options = listOf(DialogModel.createInfoOption("OK", ::hideMessage)),
            close = ::hideMessage
        )
    }

    private fun deleteCompetition() {
        viewModelScope.launch {
            val result = kotlin.runCatching {
                selectedCompetitionId.value?.let { id ->
                    DbHelper.getCompetition(id)?.let { competition ->
                        DbHelper.delete(competition)
                        selectedCompetitionId.value = null
                        updateCompetitionsList()
                        showMessage("Competition '${competition.name}' deleted successfully.")
                    }
                }
            }

            result.exceptionOrNull()?.let {
                reportError("Failed to delete competition from database.", it)
            }
        }
    }

    private fun reportError(message: String, exception: Throwable? = null) {
        _dialog.value = DialogModel(
            message = message,
            title = "Error",
            options = listOf(DialogModel.createDenyOption("OK", ::hideMessage)),
            close = ::hideMessage
        )

        Logger.error(
            tag = tag,
            msg = message + exception?.let { "\n${it.message}\n${it.stackTraceToString()}" }
        )
    }

    private suspend fun updateTasksList() {
        withContext(Dispatchers.Default) {
            val selectedCompetition = selectedCompetitionId.value ?: return@withContext
            val tasks = (DbHelper.getCompetition(selectedCompetition)?.getTasks() ?: emptyList()).map { taskDbo ->
                TaskRow(
                    dto = taskDbo.toTask(),
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
}