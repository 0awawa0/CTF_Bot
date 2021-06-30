package database

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption


object CompetitionsTable: LongIdTable(name = "Competitions") {
    val name = varchar("name", 128)
}

object PlayersTable: LongIdTable(name = "Players") {
    val name = varchar("name", 16)
}

object TasksTable: LongIdTable(name = "Tasks") {
    val category = varchar("category", 128)
    val name = varchar("name", 128)
    val description = varchar("description", 2048)
    val flag = varchar("flag", 128)
    val attachment = varchar("attachment", 1024)
    val competition = reference("competition", CompetitionsTable, onDelete = ReferenceOption.CASCADE)
}

object SolvesTable: LongIdTable(name = "Solves") {
    val task = reference("task", TasksTable, onDelete = ReferenceOption.CASCADE)
    val player = reference("player", PlayersTable, onDelete = ReferenceOption.CASCADE)
    val timestamp = long("timestamp")
}

//object ScoresTable: LongIdTable(name = "Scores") {
//    val competition = reference("competition", CompetitionsTable, onDelete = ReferenceOption.CASCADE)
//    val player = reference("player", PlayersTable, onDelete = ReferenceOption.CASCADE)
//    val score = integer("score")
//}