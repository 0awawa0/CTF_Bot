package ui.compose.competitions.add

import database.CompetitionModel
import database.DbHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ui.compose.shared.BaseViewModel

class AddCompetitionViewModel(private val onFinished: (Boolean) -> Unit): BaseViewModel() {

    private val _competitionName = MutableStateFlow("")
    val competitionName: StateFlow<String> get() = _competitionName

    fun onCompetitionNameChanged(newName: String) {
        _competitionName.value = newName
    }

    fun onSave() {
        viewModelScope.launch {
            DbHelper.add(CompetitionModel(competitionName.value))?.let {
                onFinished(true)
            } ?: onFinished(false)
        }
    }

    fun onCancel() = onFinished(false)
}