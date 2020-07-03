package db.entities

import db.tables.PlayersTable
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

class PlayerEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<PlayerEntity>(PlayersTable)
    var userName by PlayersTable.userName
    var score by PlayersTable.score
    var solvedTasks by PlayersTable.solvedTasks
}