package database

import kotlinx.serialization.Serializable


@kotlinx.serialization.Serializable
data class CompetitionModel(
    val name: String,
    val tasks: List<TaskModel> = emptyList()
)

@kotlinx.serialization.Serializable
data class TaskModel(
    val category: String,
    val name: String,
    val description: String = "",
    val flag: String,
    val attachment: String = ""
)

@kotlinx.serialization.Serializable
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
