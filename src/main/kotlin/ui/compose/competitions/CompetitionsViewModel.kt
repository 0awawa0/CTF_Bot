package ui.compose.competitions

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import database.CompetitionModel
import database.DbHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ui.compose.shared.components.BasicColumn
import ui.compose.shared.components.Column
import ui.compose.shared.components.Row
import ui.compose.shared.dto.Competition
import ui.compose.shared.dto.Score
import ui.compose.shared.dto.Task
import utils.Logger
import java.io.File

class CompetitionsViewModel {

    private val _competitions = mutableStateMapOf<Long?, Competition>()
    val competitions: List<Competition> get() = _competitions.values.toList().sortedBy { it.id }

    private val selectedCompetitionId: MutableStateFlow<Long?> = MutableStateFlow(null)
    val selectedCompetition: Competition? get() = _competitions[selectedCompetitionId.value]

    companion object {
        class TaskColumn(name: String, editable: Boolean): BasicColumn(name, editable)
        class ScoreboardColumn(name: String, editable: Boolean): BasicColumn(name, editable)

        val idColumn = TaskColumn("id", false)
        val categoryColumn = TaskColumn("Category", true)
        val nameColumn = TaskColumn("Name", true)
        val descriptionColumn = TaskColumn("Description", true)
        val attachmentColumn = TaskColumn("Attachment", true)
        val flagColumn = TaskColumn("Flag", true)
        val solvesColumn = TaskColumn("Solves count", false)
        val tasksColumns = listOf(
            idColumn,
            categoryColumn,
            nameColumn,
            descriptionColumn,
            attachmentColumn,
            flagColumn,
            solvesColumn
        )

        val playerNameColumn = ScoreboardColumn("Player", false)
        val playerScoreColumn = ScoreboardColumn("Score", false)
        val playersColumns = listOf(playerNameColumn, playerScoreColumn)
    }

    inner class ScoreboardRow(private val dto: Score): Row {
        override val columns: List<Column> = playersColumns
        override val values: MutableMap<Column, String> = mutableMapOf(
            playerNameColumn to dto.name,
            playerScoreColumn to dto.score.toString()
        )

        override fun commitChanges() = throw UnsupportedOperationException("Players editing is not allowed")
    }

    inner class TaskRow(private val dto: Task) : Row {
        override val columns = tasksColumns
        override val values: MutableMap<Column, String> = mutableMapOf(
            idColumn to dto.id.toString(),
            categoryColumn to dto.category,
            nameColumn to dto.name,
            descriptionColumn to dto.description,
            attachmentColumn to dto.attachment,
            flagColumn to dto.flag,
            solvesColumn to dto.solvesCount.toString()
        )

        override fun commitChanges() {
            GlobalScope.launch {
                DbHelper.getTask(dto.id)?.let {
                    it.category = values[categoryColumn] ?: ""
                    it.name = values[nameColumn] ?: ""
                    it.description = values[descriptionColumn] ?: ""
                    it.attachment = values[attachmentColumn] ?: ""
                    it.flag = values[flagColumn] ?: ""
                    DbHelper.update(it)
                }
                updateTasksList()
            }
        }

    }

    private val _tasks = mutableStateListOf<TaskRow>()
    val tasks: List<TaskRow> get() = _tasks

    private val _scoreboard = mutableStateListOf<ScoreboardRow>()
    val scoreboard: List<ScoreboardRow> get() = _scoreboard

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

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

    suspend fun updateTasksList() {
        withContext(Dispatchers.Default) {
            val selectedCompetition = selectedCompetitionId.value ?: return@withContext
            val tasks = DbHelper.getCompetition(selectedCompetition)?.getTasks() ?: emptyList()
            _tasks.clear()
            _tasks.addAll(tasks.map {
                TaskRow(Task(
                    id = it.id,
                    category = it.category,
                    name = it.name,
                    description = it.description,
                    flag = it.flag,
                    attachment = it.attachment,
                    solvesCount = it.getSolves().count()
                ))
            })
        }
    }

    suspend fun updatePlayersList() {
        withContext(Dispatchers.Default) {
            _scoreboard.clear()
            val board = selectedCompetitionId.value?.let {
                val competition = DbHelper.getCompetition(it) ?: return@let null
                DbHelper.getScoreboard(competition).map { scoreValue ->
                    ScoreboardRow(Score(scoreValue.first, scoreValue.second))
                }
            } ?: emptyList()
            _scoreboard.addAll(board)
        }
    }

    fun onSelected(competition: Competition) {
        _competitions[selectedCompetitionId.value]?.let {
            _competitions[selectedCompetitionId.value] = it.copy(selected = false)
        }
        _competitions[competition.id]?.let {
            _competitions[competition.id] = competition.copy(selected = true)
            selectedCompetitionId.value = competition.id
            viewModelScope.launch {
                updateTasksList()
                updateCompetitionsList()
            }
        }
    }

    fun addCompetitionsFromJson(path: String) {
        val error = kotlin.runCatching {
            viewModelScope.launch {
                val text = File(path).readText()
                val parse = Json.decodeFromString<Array<CompetitionModel>>(text)
                for (competition in parse) DbHelper.add(competition)
            }
        }

        error.exceptionOrNull()?.let {
            Logger.error(
                "CompetitionsViewModel",
                "Failed to parse competitions from JSON. ${it.message}\n${it.stackTraceToString()}"
            )
        }
    }
}