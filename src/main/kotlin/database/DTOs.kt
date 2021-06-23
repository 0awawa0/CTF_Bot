package database

import kotlinx.coroutines.runBlocking

abstract class BaseDTO {
    abstract val id: Long

    abstract suspend fun updateEntity()
}

class CompetitionDTO(val entity: CompetitionEntity): BaseDTO() {
    override val id: Long = entity.id.value

    var name: String = entity.name

    override suspend fun updateEntity() { DbHelper.update(this) }

    suspend fun getTasks(): List<TaskDTO> {
        return DbHelper.transactionOn(DbHelper.database) { entity.tasks.map { TaskDTO(it) }}
    }

    suspend fun getScores(): List<ScoreDTO> {
        return DbHelper.transactionOn(DbHelper.database) { entity.scores.map { ScoreDTO(it) }}
    }

    suspend fun getScoreBoard(): List<Pair<String, Long>> {
        val scores = getScores()
        val result = ArrayList<Pair<String, Long>>()
        for (score in scores) {
            val player = score.getPlayer()
            result.add(Pair(player.name, score.score))
        }
        return result
    }
}

class PlayerDTO(val entity: PlayerEntity): BaseDTO() {
    override val id: Long = entity.id.value

    var name: String = entity.name

    override suspend fun updateEntity() {
        DbHelper.update(this)
    }

    fun getTotalScoreSynchronous(): Long {
        return runBlocking { getScores().sumOf { it.score } }
    }

    suspend fun getScores(): List<ScoreDTO> {
        return DbHelper.transactionOn(DbHelper.database) { entity.scores.map { ScoreDTO(it) }}
    }

    suspend fun getSolves(): List<SolveDTO> {
        return DbHelper.transactionOn(DbHelper.database) { entity.solves.map { SolveDTO(it) }}
    }

    suspend fun getCompetitionScore(competitionDTO: CompetitionDTO): ScoreDTO? {
        return getScores().find {
            it.getCompetition().id == competitionDTO.id
        }
    }

    suspend fun getTotalScore(): Long {
        return getScores().sumOf { it.score }
    }

    suspend fun getSolvedTasks(competitionDTO: CompetitionDTO): List<TaskDTO> {
        return getSolves().map {
            it.getTask()
        }.filter {
            it.getCompetition().id == competitionDTO.id
        }
    }
}

class TaskDTO(val entity: TaskEntity): BaseDTO() {
    override val id: Long = entity.id.value

    var category = entity.category
    var name = entity.name
    var description = entity.description
    var flag = entity.flag
    var attachment = entity.attachment

    override suspend fun updateEntity() { DbHelper.update(this) }

    fun getSolvesCountSynchronous(): Int {
        return runBlocking { getSolves().count() }
    }

    suspend fun getCompetition(): CompetitionDTO {
        return DbHelper.transactionOn(DbHelper.database) { CompetitionDTO(entity.competition) }
    }

    suspend fun getSolves(): List<SolveDTO> {
        return DbHelper.transactionOn(DbHelper.database) { entity.solves.map { SolveDTO(it) }}
    }

    suspend fun getSolvedPlayers(): List<PlayerDTO> {
        return DbHelper.transactionOn(DbHelper.database) { entity.solves.map { PlayerDTO(it.player) } }
    }

    suspend fun getTaskPrice(): Int {
        return DbHelper.getNewTaskPrice(getSolves().count())
    }
}

class SolveDTO(val entity: SolveEntity): BaseDTO() {
    override val id: Long = entity.id.value

    val timestamp = entity.timestamp

    override suspend fun updateEntity() {}

    suspend fun getTask(): TaskDTO {
        return DbHelper.transactionOn(DbHelper.database) { TaskDTO(entity.task) }
    }

    fun getTaskSynchronous(): TaskDTO {
        return runBlocking { getTask() }
    }

    suspend fun getPlayer(): PlayerDTO {
        return DbHelper.transactionOn(DbHelper.database) { PlayerDTO(entity.player) }
    }
}

data class ScoreDTO(val entity: ScoreEntity): BaseDTO() {
    override val id: Long = entity.id.value

    var score = entity.score

    override suspend fun updateEntity() { DbHelper.update(this) }

    suspend fun getCompetition(): CompetitionDTO {
        return DbHelper.transactionOn(DbHelper.database) { CompetitionDTO(entity.competition) }
    }

    fun getCompetitionSynchronous(): CompetitionDTO {
        return runBlocking { getCompetition() }
    }

    suspend fun getPlayer(): PlayerDTO {
        return DbHelper.transactionOn(DbHelper.database) { PlayerDTO(entity.player) }
    }
}