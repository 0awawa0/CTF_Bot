package db.entities

import db.tables.PlayersTable
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID

class PlayerEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<PlayerEntity>(PlayersTable)
    var userName by PlayersTable.userName
    var currentScore by PlayersTable.currentScore
    var seasonScore by PlayersTable.seasonScore
    var solvedTasks by PlayersTable.solvedTasks
}