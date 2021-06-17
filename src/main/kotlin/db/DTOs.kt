package db

import javafx.beans.property.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.id.EntityID
import tornadofx.getValue
import tornadofx.setValue

abstract class BaseDTO {
    abstract val id: EntityID<Long>
    abstract fun commit(): DatabaseHelper.DbOpResult<Boolean>
    abstract fun delete(): DatabaseHelper.DbOpResult<Boolean>
}

data class CompetitionDTO(val entity: CompetitionEntity): BaseDTO() {

    override val id = entity.id

    val nameProperty = SimpleStringProperty(entity.name)
    var name by nameProperty

    override fun commit(): DatabaseHelper.DbOpResult<Boolean> {
        return runBlocking { DatabaseHelper.updateCompetition(this@CompetitionDTO) }
    }

    override fun delete(): DatabaseHelper.DbOpResult<Boolean> {
        return runBlocking { DatabaseHelper.deleteCompetition(this@CompetitionDTO) }
    }
}

data class TaskDTO(val entity: TaskEntity): BaseDTO() {
    override val id = entity.id

    val categoryProperty = SimpleStringProperty(entity.category)
    var category by categoryProperty

    val nameProperty = SimpleStringProperty(entity.name)
    var name by nameProperty

    val descriptionProperty = SimpleStringProperty(entity.description)
    var description by descriptionProperty

    val priceProperty = SimpleIntegerProperty(entity.price)
    var price by priceProperty

    val flagProperty = SimpleStringProperty(entity.flag)
    var flag by flagProperty

    val attachmentProperty = SimpleStringProperty(entity.attachment)
    var attachment by attachmentProperty

    val dynamicScoringProperty = SimpleBooleanProperty(entity.dynamicScoring)
    var dynamicScoring by dynamicScoringProperty

    val solvesCountProperty = SimpleIntegerProperty(entity.solvesCount)
    var solvesCount by solvesCountProperty

    val competitionProperty = SimpleObjectProperty(entity.competition)
    var competition by competitionProperty

    override fun commit(): DatabaseHelper.DbOpResult<Boolean> {
        return runBlocking { DatabaseHelper.updateTask(this@TaskDTO) }
    }

    override fun delete(): DatabaseHelper.DbOpResult<Boolean> {
        return runBlocking { DatabaseHelper.deleteTask(this@TaskDTO) }
    }
}

data class PlayerDTO(val entity: PlayerEntity): BaseDTO() {

    override val id = entity.id

    val userNameProperty = SimpleStringProperty(entity.userName)
    var userName by userNameProperty

    val overallScoreProperty = SimpleIntegerProperty(entity.overallScore)
    var overallScore by overallScoreProperty

    override fun commit(): DatabaseHelper.DbOpResult<Boolean> {
        return runBlocking { DatabaseHelper.updatePlayer(this@PlayerDTO) }
    }

    override fun delete(): DatabaseHelper.DbOpResult<Boolean> {
        return runBlocking { DatabaseHelper.deletePlayer(this@PlayerDTO) }
    }
}

data class SolveDTO(val entity: SolveEntity): BaseDTO() {
    override val id = entity.id

    val playerProperty = SimpleObjectProperty(entity.player)
    var player by playerProperty

    val taskProperty = SimpleObjectProperty(entity.task)
    var task by taskProperty

    val timestampProperty = SimpleLongProperty(entity.timestamp)
    var timestamp by timestampProperty

    override fun commit(): DatabaseHelper.DbOpResult<Boolean> { return DatabaseHelper.DbOpResult(false) }
    override fun delete(): DatabaseHelper.DbOpResult<Boolean> { return DatabaseHelper.DbOpResult(false) }
}