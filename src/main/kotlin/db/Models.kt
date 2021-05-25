package db

import tornadofx.ItemViewModel

class CompetitionModel: ItemViewModel<CompetitionDTO>() {
    val name = bind(CompetitionDTO::nameProperty)
}

class TaskModel: ItemViewModel<TaskDTO>() {
    val category = bind(TaskDTO::categoryProperty)
    val name = bind(TaskDTO::nameProperty)
    val description = bind(TaskDTO::descriptionProperty)
    val price = bind(TaskDTO::priceProperty)
    val flag = bind(TaskDTO::flagProperty)
    val attachment = bind(TaskDTO::attachmentProperty)
    val competition = bind(TaskDTO::competitionProperty)
}

class PlayerModel: ItemViewModel<PlayerDTO>() {
    val userName = bind(PlayerDTO::userNameProperty)
    val currentScore = bind(PlayerDTO::currentScoreProperty)
    val seasonScore = bind(PlayerDTO::seasonScoreProperty)
}

class SolveModel: ItemViewModel<SolveDTO>() {
    val player = bind(SolveDTO::playerProperty)
    val task = bind(SolveDTO::taskProperty)
    val timestamp = bind(SolveDTO::timestampProperty)
}