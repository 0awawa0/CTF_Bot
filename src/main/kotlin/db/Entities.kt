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
    var dynamicScoring by TasksTable.dynamicScoring
    var solvesCount by TasksTable.solvesCount
    var competition by TasksTable.competition
}

class PlayerEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<PlayerEntity>(PlayersTable)

    var userName by PlayersTable.userName
    var overallScore by PlayersTable.seasonScore
}

class SolveEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<SolveEntity>(SolvesTable)

    var player by SolvesTable.player
    var task by SolvesTable.task
    var timestamp by SolvesTable.timestamp
}

class ScoreEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<ScoreEntity>(ScoresTable)

    var competition by ScoresTable.competition
    var player by ScoresTable.player
    var score by ScoresTable.score
}

