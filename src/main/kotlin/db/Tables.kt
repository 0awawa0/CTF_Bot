package db

import org.jetbrains.exposed.dao.id.LongIdTable


object CompetitionsTable: LongIdTable(name = "Competitions") {
    val name = varchar("name", 128)
}

object TasksTable: LongIdTable(name = "Tasks") {
    val category = varchar("category", 50)
    val name = varchar("name", 128)
    val description = text("description")
    val price = integer("price")
    val flag = varchar("flag", 128)
    val attachment = varchar("attachment", 1024)
    val dynamicScoring = bool("dynamic")
    val solvesCount = integer("solvesCount")
    val competition = reference("competition", CompetitionsTable)
}

object SolvesTable: LongIdTable(name = "Solves") {
    val player = reference("player", PlayersTable)
    val task = reference("task", TasksTable)
    val timestamp = long("timestamp")
}

object PlayersTable: LongIdTable(name = "Players") {
    val userName = varchar("userName", 128)
    val currentScore = integer("currentScore")
    val seasonScore = integer("seasonScore")
}