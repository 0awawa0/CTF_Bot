package ui.competitions

import database.*
import javafx.collections.ObservableList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.toObservable

class CompetitionsViewModel {

    private val tag = "CompetitionsViewModel"
    private var viewModelScope = CoroutineScope(Dispatchers.Default)

    var selectedCompetition: CompetitionDTO? = null
        set(value) {
            field = value
            tasks.clear()
            viewModelScope.launch {
                withContext(Dispatchers.JavaFx) {
                    tasks.addAll(value?.getTasks() ?: emptyList())
                }
            }
        }

    val competitions: ObservableList<CompetitionDTO> = emptyList<CompetitionDTO>().toObservable()
    val tasks: ObservableList<TaskDTO> = emptyList<TaskDTO>().toObservable()

    fun addCompetition(name: String) {
        viewModelScope.launch { DbHelper.addCompetition(CompetitionModel(name)) }
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
            val result = DbHelper.addTask(
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

    fun onViewDock() {
        viewModelScope.launch {
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

    fun onViewUndock() {
        viewModelScope.cancel()
        viewModelScope = CoroutineScope(Dispatchers.Default)
    }
//    fun tryAddFromJson(file: File, competition: CompetitionDTO, onErrorAction: () -> Unit) {
//        viewModelScope.launch {
//            try {
//                val text = file.readText()
//                val parse = Json.decodeFromString<Array<TaskJsonModel>>(text)
//                for (task in parse) {
//                    val result = DatabaseHelper.addTask(
//                        task.category,
//                        task.name,
//                        task.description,
//                        task.price,
//                        task.flag,
//                        task.attachment,
//                        task.dynamicScoring,
//                        competition
//                    )
//                    if (result.result == null) {
//                        Logger.error(
//                            tag,
//                            "${result.exception?.message}\n${result.exception?.stackTraceToString()}"
//                        )
//                    }
//                }
//                mTasks.emit(DatabaseHelper.getTasks(competition).result ?: emptyList())
//            } catch (ex: Exception) {
//                onErrorAction()
//                Logger.error(tag, "Failed to decode JSON. ${ex.message}\n${ex.stackTraceToString()}")
//            }
//        }
//    }
}