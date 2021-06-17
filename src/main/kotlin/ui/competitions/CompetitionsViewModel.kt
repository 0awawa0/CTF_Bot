package ui.competitions

import db.CompetitionDTO
import db.DatabaseHelper
import db.TaskDTO
import db.TaskJsonModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import utils.Logger
import java.io.File

class CompetitionsViewModel {

    private val tag = "CompetitionsViewModel"
    private val viewModelScope = CoroutineScope(Dispatchers.IO)

    var selectedCompetition: CompetitionDTO? = null
        set(value) {
            field = value
            viewModelScope.launch {
                if (value != null)
                    mTasks.emit(DatabaseHelper.getTasks(value).result ?: emptyList())
            }
        }

    val competitions = DatabaseHelper.competitions
    private val mTasks =  MutableStateFlow<List<TaskDTO>>(emptyList())
    val tasks = mTasks.asStateFlow()

    fun addCompetition(name: String) {
        viewModelScope.launch {
            val result = DatabaseHelper.addCompetition(name)
            if (result.result == null) {
                Logger.error(tag, "${result.exception?.message}\n${result.exception?.stackTraceToString()}")
            }
        }
    }

    fun addTask(
        category: String,
        name: String,
        description: String,
        price: Int,
        flag: String,
        attachment: String,
        competition: CompetitionDTO,
        dynamicScoring: Boolean,
    ) {
        viewModelScope.launch {
            val result = DatabaseHelper.addTask(
                category,
                name,
                description,
                price,
                flag,
                attachment,
                dynamicScoring = dynamicScoring,
                competition
            )
            if (result.result != null) {
                val selectedCompetition = selectedCompetition
                if (selectedCompetition != null)
                    viewModelScope.launch {
                        mTasks.emit(DatabaseHelper.getTasks(selectedCompetition).result ?: emptyList())
                    }
            } else {
                Logger.error(tag, "${result.exception?.message}\n${result.exception?.stackTraceToString()}")
            }
        }
    }

    fun delete(competition: CompetitionDTO) {
        viewModelScope.launch {
            val result = DatabaseHelper.deleteCompetition(competition)
            if (result.result != true) {
                Logger.error(tag, "${result.exception?.message}\n${result.exception?.stackTraceToString()}")
            }
        }
    }

    fun delete(task: TaskDTO) {
        viewModelScope.launch {
            val result = DatabaseHelper.deleteTask(task)
            if (result.result == true) {
                val competition = selectedCompetition ?: return@launch
                mTasks.emit(DatabaseHelper.getTasks(competition).result ?: emptyList())
            } else {
                Logger.error(tag, "${result.exception?.message}\n${result.exception?.stackTraceToString()}")
            }
        }
    }

    fun update(task: TaskDTO) {
        viewModelScope.launch {
            val result = DatabaseHelper.updateTask(task)
            if (result.result != true) {
                Logger.error(tag, "${result.exception?.message}\n${result.exception?.stackTraceToString()}")
            }
        }
    }

    fun tryAddFromJson(file: File, competition: CompetitionDTO, onErrorAction: () -> Unit) {
        viewModelScope.launch {
            try {
                val text = file.readText()
                val parse = Json.decodeFromString<Array<TaskJsonModel>>(text)
                for (task in parse) {
                    val result = DatabaseHelper.addTask(
                        task.category,
                        task.name,
                        task.description,
                        task.price,
                        task.flag,
                        task.attachment,
                        task.dynamicScoring,
                        competition
                    )
                    if (result.result == null) {
                        Logger.error(
                            tag,
                            "${result.exception?.message}\n${result.exception?.stackTraceToString()}"
                        )
                    }
                }
                mTasks.emit(DatabaseHelper.getTasks(competition).result ?: emptyList())
            } catch (ex: Exception) {
                onErrorAction()
                Logger.error(tag, "Failed to decode JSON. ${ex.message}\n${ex.stackTraceToString()}")
            }
        }
    }
}