package ui.compose.competitions

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import database.CompetitionDTO
import database.CompetitionModel
import database.DbHelper
import database.PlayerDTO
import database.SolveDTO
import database.TaskDTO
import database.TaskModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ui.compose.shared.BaseViewModel
import ui.compose.shared.components.Column
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
    private val _selectedTaskId: MutableStateFlow<Long?> = MutableStateFlow(null)
    private val _tasks = mutableStateListOf<TaskRow>()
    private val _scoreboard = mutableStateListOf<ScoreboardRow>()
    private val _competitionControlsState = mutableStateOf(CompetitionControlButtonsState())
    private val _taskControlsState = mutableStateOf(TaskControlButtonsState())
    private val _fileChooseDialog = MutableStateFlow<FileChooseModel?>(null)
    private val _dialog = MutableStateFlow<DialogModel?>(null)
    private val _instanceCreationDialog = MutableStateFlow<InstanceCreationModel?>(null)

    val competitionControlButtonsState: State<CompetitionControlButtonsState> = _competitionControlsState
    val taskControlButtonsState: State<TaskControlButtonsState> = _taskControlsState

    val competitions: List<Competition> get() = _competitions.values.toList().sortedBy { it.id }
    val selectedCompetition: Competition? get() = _competitions[selectedCompetitionId.value]
    val tasks: List<TaskRow> get() = _tasks
    val scoreboard: List<ScoreboardRow> get() = _scoreboard

    val fileChooseDialog: StateFlow<FileChooseModel?> get() = _fileChooseDialog
    val dialog: StateFlow<DialogModel?> get() = _dialog
    val instanceCreationModel: StateFlow<InstanceCreationModel?> get() = _instanceCreationDialog

    private var editingColumn: Column? = null

    override fun onStart() {
        super.onStart()

        viewModelScope.launch {
            DbHelper.eventsPipe.collect {
                it.handle()
            }
        }

        viewModelScope.launch {
            updateCompetitionsList()
        }

        _competitionControlsState.value = CompetitionControlButtonsState(
            add = ButtonState(
                isEnabled = true,
                onClick = ::addCompetition
            ),
            addFromJson = ButtonState(
                isEnabled = true,
                onClick = ::onAddCompetitionFromJson
            )
        )
    }

    fun onSelectCompetition(competition: Competition) {
        if (competition.id == selectedCompetitionId.value) return

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

        _competitionControlsState.value = _competitionControlsState.value.copy(
            delete = ButtonState(
                isEnabled = true,
                onClick = ::onCompetitionDeleteClicked
            ),
            exportToJson = ButtonState(
                isEnabled = true,
                onClick = ::exportCompetition
            )
        )

        _taskControlsState.value = _taskControlsState.value.copy(
            add = ButtonState(
                isEnabled = true,
                onClick = ::addTask
            ),
            addFromJson = ButtonState(
                isEnabled = true,
                onClick = ::addTaskFromJson
            ),
            delete = ButtonState(),
            exportToJson = ButtonState()
        )
    }

    fun onSelectTask(task: TaskRow) {
        _taskControlsState.value = taskControlButtonsState
            .value
            .copy(
                delete = ButtonState(
                    isEnabled = true,
                    onClick = {
                        viewModelScope.launch {
                            DbHelper.deleteTask(task.id)
                        }
                    }
                ),
                exportToJson = ButtonState(
                    isEnabled = true,
                    onClick = ::exportTaskToJson
                )
            )
        _selectedTaskId.value = task.id
        editingColumn = null
        viewModelScope.launch { updateTasksList() }
    }

    fun onEditField(column: Column) {
        editingColumn = column
        viewModelScope.launch { updateTasksList() }
    }

    private fun addTask() {
        val category = mutableStateOf("")
        val name = mutableStateOf("")
        val description = mutableStateOf("")
        val flag = mutableStateOf("")
        val attachment = mutableStateOf("")

        _instanceCreationDialog.value = InstanceCreationModel(
            title = "Add task",
            fields = listOf(
                InstanceCreationModel.InstanceField.Text(
                    name = "Category",
                    value = category,
                    onValueChanged = { category.value = it }
                ),
                InstanceCreationModel.InstanceField.Text(
                    name = "Name",
                    value = name,
                    onValueChanged = { name.value = it }
                ),
                InstanceCreationModel.InstanceField.Text(
                    name = "Description",
                    value = description,
                    onValueChanged = { description.value = it }
                ),
                InstanceCreationModel.InstanceField.Text(
                    name = "Flag",
                    value = flag,
                    onValueChanged = { flag.value = it }
                ),
                InstanceCreationModel.InstanceField.File(
                    name = "Attachment",
                    selectedPath = attachment,
                    onOpenFileSelection = {
                        _fileChooseDialog.value = FileChooseModel(
                            title = "Choose file",
                            mode = FileChooseModel.Mode.OPEN_FILE,
                            onResult = {
                                _fileChooseDialog.value = null
                                attachment.value = it?.absolutePath ?: ""
                            }
                        )
                    }
                )
            ),
            onSave = {
                viewModelScope.launch {
                    selectedCompetitionId.value?.let {
                        DbHelper.add(
                            TaskModel(
                                category = category.value,
                                name = name.value,
                                description = description.value,
                                flag = flag.value,
                                attachment = attachment.value
                            ),
                            competitionId = it
                        )
                    }
                }
                _instanceCreationDialog.value = null
            },
            onCancel = {
                _instanceCreationDialog.value = null
            }
        )
    }

    private fun addTaskFromJson() {
        _fileChooseDialog.value = FileChooseModel(
            title = "Choose file",
            mode = FileChooseModel.Mode.OPEN_FILE,
            onResult = { file ->
                _fileChooseDialog.value = null
                viewModelScope.launch {
                    file?.let { file ->
                        try {
                            val parsedTasks = Json.decodeFromString<List<TaskModel>>(file.readText())
                            selectedCompetitionId.value?.let { DbHelper.add(parsedTasks, it) }
                        } catch (ex: Exception) {
                            reportError("Failed to parse tasks from file", ex)
                        }
                    }
                }
            }
        )
    }

    private fun exportTaskToJson() {

    }

    private suspend fun updateCompetitionsList() {
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

    private fun onAddCompetitionFromJson() {
        _fileChooseDialog.value = FileChooseModel(
            title = "Choose file",
            mode = FileChooseModel.Mode.OPEN_FILE,
            onResult = ::addCompetitionsFromJson
        )
    }

    private fun addCompetition() {
        val competitionName = mutableStateOf("")
        _instanceCreationDialog.value = InstanceCreationModel(
            title = "Add competition",
            fields = listOf(
                InstanceCreationModel.InstanceField.Text(
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

    private fun onCompetitionDeleteClicked() {
        val selectedCompetition = selectedCompetition ?: return
        _dialog.value = DialogModel(
            title = "Competition deletion",
            message = "This will delete competition '${selectedCompetition.name}' from database. This action is unrecoverable. Continue?",
            options = listOf(
                DialogModel.createAcceptOption("OK") {
                    deleteCompetition(name = selectedCompetition.name, selectedCompetition.id)
                    _dialog.value = null
                },
                DialogModel.createDenyOption("Cancel") { _dialog.value = null }
            ),
            close = { _dialog.value = null }
        )
    }

    private fun exportCompetition() {
        _fileChooseDialog.value = FileChooseModel(
            title = "Choose directory",
            mode = FileChooseModel.Mode.OPEN_DIR,
            onResult = ::onDirectorySelectedForExport
        )
    }

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

    private fun deleteCompetition(name: String, id: Long) {
        viewModelScope.launch {
            val result = kotlin.runCatching {
                DbHelper.deleteCompetition(id)
                selectedCompetitionId.value = null
                _competitionControlsState.value = _competitionControlsState.value.copy(
                    delete = ButtonState(),
                    exportToJson = ButtonState()
                )
                updateCompetitionsList()
                showMessage("Competition '${name}' deleted successfully.")
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
                    onChangesSaved = {},
                    isSelected = _selectedTaskId.value == taskDbo.id,
                    editingColumn = if (_selectedTaskId.value == taskDbo.id) {
                        editingColumn
                    } else {
                        null
                    }
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

    private suspend fun DbHelper.DbEvent.handle() {
        when (dto) {
            is CompetitionDTO -> updateCompetitionsList()
            is TaskDTO -> updateTasksList()
            is SolveDTO -> updateTasksList()
            is PlayerDTO -> updateTasksList()
        }
    }

    data class ButtonState(
        val isEnabled: Boolean = false,
        val onClick: () -> Unit = {}
    )

    data class CompetitionControlButtonsState(
        val add: ButtonState = ButtonState(),
        val addFromJson: ButtonState = ButtonState(),
        val exportToJson: ButtonState = ButtonState(),
        val delete: ButtonState = ButtonState()
    )

    data class TaskControlButtonsState(
        val add: ButtonState = ButtonState(),
        val addFromJson: ButtonState = ButtonState(),
        val exportToJson: ButtonState = ButtonState(),
        val delete: ButtonState = ButtonState()
    )
}