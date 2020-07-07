package db.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table


object TasksTable: LongIdTable() {
    val category = varchar("category", 50)
    val name = varchar("name", 128)
    val description = text("description")
    val price = integer("price")
    val flag = varchar("flag", 128)
    val filesDirectory = varchar("files", 1024)
}