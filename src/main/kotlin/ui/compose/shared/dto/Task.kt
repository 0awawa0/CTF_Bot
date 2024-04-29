package ui.compose.shared.dto

import database.TaskDTO


data class Task(
    val id: Long,
    val category: String,
    val name: String,
    val description: String,
    val flag: String,
    val attachment: String,
    val solvesCount: Int
)

suspend fun TaskDTO.toTask() = Task(
    id = id,
    category = category,
    name = name,
    description = description,
    flag = flag,
    attachment = attachment,
    solvesCount = getSolves().count()
)