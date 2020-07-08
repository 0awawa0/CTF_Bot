package db.tables

import org.jetbrains.exposed.dao.id.LongIdTable


object PlayersTable: LongIdTable() {
    val userName = varchar("userName", 128)
    val currentScore = integer("currentScore")
    val seasonScore = integer("seasonScore")
    val solvedTasks = text("solvedTasks")
    val lastRightAnswer = long("lastRightAnswer")
}