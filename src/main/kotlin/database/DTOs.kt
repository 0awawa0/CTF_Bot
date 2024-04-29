package database

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
}

class PlayerDTO(val entity: PlayerEntity): BaseDTO() {
    override val id: Long = entity.id.value

    var name: String = entity.name

    override suspend fun updateEntity() { DbHelper.update(this) }

    suspend fun getSolves(): List<SolveDTO> {
        return DbHelper.transactionOn(DbHelper.database) { entity.solves.map { SolveDTO(it) }}
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

    suspend fun getCompetition(): CompetitionDTO {
        return DbHelper.transactionOn(DbHelper.database) { CompetitionDTO(entity.competition) }
    }

    suspend fun getSolves(): List<SolveDTO> {
        return DbHelper.transactionOn(DbHelper.database) { entity.solves.map { SolveDTO(it) }}
    }
}

class SolveDTO(val entity: SolveEntity): BaseDTO() {
    override val id: Long = entity.id.value

    val timestamp = entity.timestamp

    override suspend fun updateEntity() {}

    suspend fun getTask(): TaskDTO {
        return DbHelper.transactionOn(DbHelper.database) { TaskDTO(entity.task) }
    }

    suspend fun getPlayer(): PlayerDTO {
        return DbHelper.transactionOn(DbHelper.database) { PlayerDTO(entity.player) }
    }
}
