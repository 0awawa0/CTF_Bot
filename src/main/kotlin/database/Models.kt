package database

import kotlinx.serialization.Serializable


@Serializable
data class CompetitionModel(
    val name: String,
    val tasks: List<TaskModel> = emptyList()
)

@Serializable
data class TaskModel(
    val category: String,
    val name: String,
    val description: String = "",
    val flag: String,
    val attachment: String = ""
)

@Serializable
data class PlayerModel(
    val id: Long,
    val name: String
)

@Serializable
data class SolveModel(
    val player: PlayerModel,
    val task: TaskModel,
    val timestamp: Long
)

@Serializable
data class ScoreModel(
    val competition: CompetitionModel,
    val player: PlayerModel,
    val score: Long
)