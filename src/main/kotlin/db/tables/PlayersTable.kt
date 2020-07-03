package db.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table


object PlayersTable: LongIdTable() {
    val userName = varchar("userName", 128)
    val score = integer("score")
    val solvedTasks = text("solvedTasks")
}