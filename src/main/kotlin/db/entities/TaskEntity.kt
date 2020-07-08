package db.entities

import db.tables.TasksTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TaskEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<TaskEntity>(TasksTable)
    var category by TasksTable.category
    var name by TasksTable.name
    var description by TasksTable.description
    var price by TasksTable.price
    var flag by TasksTable.flag
    var filesDirectory by TasksTable.filesDirectory
    var ctfName by TasksTable.ctfName
}