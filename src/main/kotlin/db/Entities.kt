package db

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CompetitionEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<CompetitionEntity>(CompetitionsTable)

    var name by CompetitionsTable.name
}

class TaskEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<TaskEntity>(TasksTable)

    var category by TasksTable.category
    var name by TasksTable.name
    var description by TasksTable.description
    var price by TasksTable.price
    var flag by TasksTable.flag
    var attachment by TasksTable.attachment
    var competition by TasksTable.competition
}

class PlayerEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<PlayerEntity>(PlayersTable)

    var userName by PlayersTable.userName
    var currentScore by PlayersTable.currentScore
    var seasonScore by PlayersTable.seasonScore
}

class SolveEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<SolveEntity>(SolvesTable)

    var player by SolvesTable.player
    var task by SolvesTable.task
    var timestamp by SolvesTable.timestamp
}

