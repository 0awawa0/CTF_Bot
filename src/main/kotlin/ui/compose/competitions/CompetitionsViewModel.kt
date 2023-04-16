package ui.compose.competitions

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import database.DbHelper
import database.TaskDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.compose.shared.components.Column
import ui.compose.shared.components.Row
import ui.compose.shared.dto.Competition
import ui.compose.shared.dto.Task

class CompetitionsViewModel {

    private val _competitions = mutableStateMapOf<Long?, Competition>()
    val competitions: List<Competition> get() = _competitions.values.toList().sortedBy { it.id }

    private val selectedCompetitionId: MutableStateFlow<Long?> = MutableStateFlow(null)
    val selectedCompetition: Competition? get() = _competitions[selectedCompetitionId.value]

    companion object {
        val idColumn = Column("id", false)
        val categoryColumn = Column("Category", true)
        val nameColumn = Column("Name", true)
        val descriptionColumn = Column("Description", true)
        val attachmentColumn = Column("Attachment", true)
        val flagColumn = Column("Flag", true)
        val solvesColumn = Column("Solves count", false)
        val tasksColumns = listOf(
            idColumn,
            categoryColumn,
            nameColumn,
            descriptionColumn,
            attachmentColumn,
            flagColumn,
            solvesColumn
        )
    }

    inner class TaskRow(
        private val dto: Task
    ) : Row {
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

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    suspend fun updateCompetitionsList() {
        withContext(Dispatchers.Default) {
            _competitions.clear()
            _competitions.putAll(DbHelper.getAllCompetitions().map { it.id to Competition(it.id, it.name) })

            _competitions[selectedCompetitionId.value]?.let {
                _competitions[selectedCompetitionId.value] = it.copy(selected = true)
            } ?: run { selectedCompetitionId.value = null }
            updateTasksList()
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

    fun onSelected(competition: Competition) {
        _competitions[selectedCompetitionId.value]?.let {
            _competitions[selectedCompetitionId.value] = it.copy(selected = false)
        }
        _competitions[competition.id]?.let {
            _competitions[competition.id] = competition.copy(selected = true)
            selectedCompetitionId.value = competition.id
            viewModelScope.launch { updateTasksList() }
        }
    }
}