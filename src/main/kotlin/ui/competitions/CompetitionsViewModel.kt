package ui.competitions

import database.*
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

    private val tag = "CompetitionsViewModel"

    private var tasksLoadingMutex = Mutex()
    var selectedCompetition: CompetitionDTO? = null
        set(value) {
            viewModelScope.launch {
                tasksLoadingMutex.withLock {
                    withContext(Dispatchers.JavaFx) {
                        tasks.clear()
                        tasks.addAll(value?.getTasks() ?: emptyList())
                        field = value
                    }
                }
            }
        }

    val competitions: ObservableList<CompetitionDTO> = emptyList<CompetitionDTO>().toObservable()
    val tasks: ObservableList<TaskDTO> = emptyList<TaskDTO>().toObservable()

    fun addCompetition(name: String) {
        viewModelScope.launch { DbHelper.add(CompetitionModel(name)) }
    }

    fun addTask(
        category: String,
        name: String,
        description: String,
        flag: String,
        attachment: String,
        competition: CompetitionDTO,
    ) {
        viewModelScope.launch {
            DbHelper.add(
                competition,
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

    fun delete(competition: CompetitionDTO) {
        viewModelScope.launch { DbHelper.delete(competition) }
    }

    fun delete(task: TaskDTO) {
        viewModelScope.launch { DbHelper.delete(task) }
    }

    fun update(task: TaskDTO) {
        viewModelScope.launch { task.updateEntity() }
    }

    override fun onViewDock() {
        viewModelScope.launch {
            withContext(Dispatchers.JavaFx) {
                competitions.clear()
                competitions.addAll(DbHelper.getAllCompetitions())
            }

            selectedCompetition = null
            DbHelper.eventsPipe.collect { event ->
                when (event) {
                    is DbHelper.DbEvent.Add -> {
                        when (event.dto) {
                            is CompetitionDTO -> withContext(Dispatchers.JavaFx) {
                                competitions.add(event.dto)
                            }
                            is TaskDTO -> {
                                if (event.dto.getCompetition().id == selectedCompetition?.id)
                                    withContext(Dispatchers.JavaFx) {
                                        tasks.add(event.dto)
                                    }
                            }}
                    }
                    is DbHelper.DbEvent.Update -> {
                        when (event.dto) {
                            is CompetitionDTO -> withContext(Dispatchers.JavaFx) {
                                if (competitions.removeIf { it.id == event.dto.id })
                                    competitions.add(event.dto)
                            }
                            is TaskDTO -> withContext(Dispatchers.JavaFx) {
                                if (tasks.removeIf { it.id == event.dto.id })
                                    tasks.add(event.dto)
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

    fun tryAddFromJson(file: File, competition: CompetitionDTO, onErrorAction: () -> Unit) {
        viewModelScope.launch {
            try {
                val text = file.readText()
                val parse = Json.decodeFromString<Array<TaskModel>>(text)
                for (task in parse) {
                    DbHelper.add(competition, task)
                }
            } catch (ex: Exception) {
                onErrorAction()
                Logger.error(tag, "Failed to decode JSON. ${ex.message}\n${ex.stackTraceToString()}")
            }
        }
    }
}