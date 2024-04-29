package ui.compose.shared.dto


data class Task(
    val id: Long,
    val category: String,
    val name: String,
    val description: String,
    val flag: String,
    val attachment: String,
    val solvesCount: Int
)