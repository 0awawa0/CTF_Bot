package ui.competitions

import db.CompetitionDTO
import db.DatabaseHelper
import db.TaskDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CompetitionsViewModel {

    private val viewModelScope = CoroutineScope(Dispatchers.IO)

    var selectedCompetition: CompetitionDTO? = null

    val competitions = DatabaseHelper.competitions
    val tasks = DatabaseHelper.tasks.map { list ->
        list.filter { it.competition.value == selectedCompetition?.id ?: 0L }
    }

    fun delete(competition: CompetitionDTO) {
        viewModelScope.launch {
            DatabaseHelper.deleteCompetition(competition)
        }
    }

    fun delete(task: TaskDTO) {
        viewModelScope.launch {
            DatabaseHelper.deleteTask(task)
        }
    }
}