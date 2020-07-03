package db


data class Player(
        val userId: Long,
        val userName: String,
        var score: Int,
        val solvedTasks: ArrayList<Long>
)