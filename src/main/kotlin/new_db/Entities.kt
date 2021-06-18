package new_db

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CompetitionEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<CompetitionEntity>(CompetitionsTable)

    var name by CompetitionsTable.name

    val tasks by TaskEntity referrersOn TasksTable.competition
    val scores by ScoreEntity referrersOn ScoresTable.competition

    suspend fun updateName(newName: String) {
        DbHelper.transactionOn(DbHelper.database) { name = newName }
    }
}

class PlayerEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<PlayerEntity>(PlayersTable)

    var name by PlayersTable.name

    val scores by ScoreEntity referrersOn SolvesTable.player
    val solves by SolveEntity referrersOn SolvesTable.player
}

class TaskEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<TaskEntity>(TasksTable)

    var category by TasksTable.category
    var name by TasksTable.name
    var description by TasksTable.description
    var flag by TasksTable.flag
    var attachment by TasksTable.attachment
    val competition by CompetitionEntity referencedOn  TasksTable.competition
    val solves by SolveEntity referrersOn SolvesTable.task
}

class SolveEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<SolveEntity>(SolvesTable)

    val task by TaskEntity referencedOn SolvesTable.task
    val player by PlayerEntity referencedOn SolvesTable.player
    val timestamp by SolvesTable.timestamp
}

class ScoreEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<ScoreEntity>(ScoresTable)

    val competition by CompetitionEntity referencedOn ScoresTable.competition
    val player by PlayerEntity referencedOn ScoresTable.player
    var score by ScoresTable.score
}