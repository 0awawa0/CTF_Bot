package db.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table


object TasksTable: LongIdTable() {
    val category = varchar("category", 50)
    val name = varchar("name", 128)
    val cost = integer("cost")
    val flag = varchar("flag", 128)
}