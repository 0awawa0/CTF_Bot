package db

import kotlinx.serialization.Serializable


@Serializable
data class TaskJsonModel(
    val category: String,
    val name: String,
    val description: String = "",
    val price: Int,
    val flag: String,
    val attachment: String = "",
    val dynamicScoring: Boolean = false
)