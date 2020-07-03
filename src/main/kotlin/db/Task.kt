package db


data class Task (
        val id: Long,
        val category: String,
        val name: String,
        val cost: Int,
        val flag: String
)