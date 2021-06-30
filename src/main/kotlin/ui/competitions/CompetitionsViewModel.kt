package ui.competitions

import database.*
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tornadofx.toObservable
import ui.BaseViewModel
import utils.Logger
import java.io.File

class CompetitionsViewModel: BaseViewModel() {

    data class ScoreboardItem(
        val playerName: String,
        val playerScore: Int
    )

    inner class CompetitionItem(
        private val dto: CompetitionDTO
    ) {
        val id by dto::id
        var name by dto::name

        fun onSelected() { selectedCompetition = dto }

        fun addTask(category: String, name: String, description: String, flag: String, attachment: String) {
            viewModelScope.launch {
                DbHelper.add(
                    dto,
                    TaskModel(
                        category,
                        name,
                        description,
                        flag,
                        attachment
                    )
                )
            }
        }

        fun delete() { viewModelScope.launch { DbHelper.delete(dto) } }

        fun pushChanges() { viewModelScope.launch { dto.updateEntity() } }

        fun addTasksFromJson(file: File, onErrorAction: () -> Unit) {
            viewModelScope.launch {
                try {
                    val text = file.readText()
                    val parse = Json.decodeFromString<Array<TaskModel>>(text)
                    for (task in parse) {
                        DbHelper.add(dto, task)
                    }
                } catch (ex: Exception) {
                    onErrorAction()
                    Logger.error(tag, "Failed to decode JSON. ${ex.message}\n${ex.stackTraceToString()}")
                }
            }
        }
    }

    inner class TaskItem(private val dto: TaskDTO) {
        val id by dto::id
        var category by dto::category
        var name by dto::name
        var description by dto::description
        var flag by dto::flag
        var attachment by dto::attachment
        var solvesCount: Int = 0
            private set

        init { viewModelScope.launch { solvesCount = dto.getSolves().count() } }

        fun delete() { viewModelScope.launch { DbHelper.delete(dto) } }

        fun pushChanges() { viewModelScope.launch { dto.updateEntity() } }
    }

    private val tag = "CompetitionsViewModel"

    private var tasksLoadingMutex = Mutex()
    private var selectedCompetition: CompetitionDTO? = null
        set(value) {
            viewModelScope.launch {
                tasksLoadingMutex.withLock {
                    field = value
                    val newTasks = value?.getTasks()?.map { TaskItem(it) } ?: emptyList()
                    val newScoreBoard = value?.getScoreBoard()?.map { ScoreboardItem(it.first, it.second) }
                        ?: emptyList()
                    withContext(Dispatchers.JavaFx) {
                        tasks.clear()
                        tasks.addAll(newTasks)
                        scoreboard.clear()
                        scoreboard.addAll(newScoreBoard)
                        mCompetitionName.set(value?.name ?: "")
                    }
                }
            }
        }

    private val mCompetitionName = ReadOnlyStringWrapper("")
    val competitionName = mCompetitionName.readOnlyProperty

    val competitions: ObservableList<CompetitionItem> = emptyList<CompetitionItem>().toObservable()
    val tasks: ObservableList<TaskItem> = emptyList<TaskItem>().toObservable()

    val scoreboard = emptyList<ScoreboardItem>().toObservable()

    fun addCompetition(name: String) {
        viewModelScope.launch { DbHelper.add(CompetitionModel(name)) }
    }

    override fun onViewDock() {
        super.onViewDock()

        viewModelScope.launch {
            withContext(Dispatchers.JavaFx) {
                competitions.clear()
                competitions.addAll(DbHelper.getAllCompetitions().map { CompetitionItem(it) })
            }

            selectedCompetition = null
            DbHelper.eventsPipe.collect { event ->
                when (event) {
                    is DbHelper.DbEvent.Add -> {
                        when (event.dto) {
                            is CompetitionDTO -> withContext(Dispatchers.JavaFx) {
                                competitions.add(CompetitionItem(event.dto))
                            }
                            is TaskDTO -> {
                                if (event.dto.getCompetition().id == selectedCompetition?.id)
                                    withContext(Dispatchers.JavaFx) {
                                        tasks.add(TaskItem(event.dto))
                                    }
                            }}
                    }
                    is DbHelper.DbEvent.Update -> {
                        when (event.dto) {
                            is CompetitionDTO -> withContext(Dispatchers.JavaFx) {
                                if (competitions.removeIf { it.id == event.dto.id }) {
                                    competitions.add(CompetitionItem(event.dto))
                                }
                                if (selectedCompetition?.id == event.dto.id) selectedCompetition = event.dto
                            }
                            is TaskDTO -> withContext(Dispatchers.JavaFx) {
                                if (tasks.removeIf { it.id == event.dto.id })
                                    tasks.add(TaskItem(event.dto))
                            }
                        }
                    }
                    is DbHelper.DbEvent.Delete -> {
                        when (event.dto) {
                            is CompetitionDTO -> withContext(Dispatchers.JavaFx) {
                                if (event.dto.id == selectedCompetition?.id) selectedCompetition = null
                                competitions.removeIf { it.id == event.dto.id }
                            }
                            is TaskDTO -> withContext(Dispatchers.JavaFx) {
                                tasks.removeIf { it.id == event.dto.id }
                            }
                        }
                    }
                }
            }
        }
    }
}