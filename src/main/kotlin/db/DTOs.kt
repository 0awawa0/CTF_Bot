package db

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.runBlocking
import tornadofx.getValue
import tornadofx.setValue

data class CompetitionDTO(val entity: CompetitionEntity) {

    val id = entity.id

    val nameProperty = SimpleStringProperty(entity.name)
    var name by nameProperty

    fun commit(): DatabaseHelper.DbOpResult<Boolean> {
        return runBlocking { DatabaseHelper.updateCompetition(this@CompetitionDTO) }
    }

    fun delete(): DatabaseHelper.DbOpResult<Boolean> {
        return runBlocking { DatabaseHelper.deleteCompetition(this@CompetitionDTO) }
    }
}

data class TaskDTO(val entity: TaskEntity) {
    val id = entity.id

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

    val competitionProperty = SimpleObjectProperty(entity.competition)
    var competition by competitionProperty

    fun commit(): DatabaseHelper.DbOpResult<Boolean> {
        return runBlocking { DatabaseHelper.updateTask(this@TaskDTO) }
    }

    fun delete(): DatabaseHelper.DbOpResult<Boolean> {
        return runBlocking { DatabaseHelper.deleteTask(this@TaskDTO) }
    }
}

data class PlayerDTO(val entity: PlayerEntity) {

    val id = entity.id

    val userNameProperty = SimpleStringProperty(entity.userName)
    var userName by userNameProperty

    val currentScoreProperty = SimpleIntegerProperty(entity.currentScore)
    var currentScore by currentScoreProperty

    val seasonScoreProperty = SimpleIntegerProperty(entity.seasonScore)
    var seasonScore by seasonScoreProperty

    fun commit(): DatabaseHelper.DbOpResult<Boolean> {
        return runBlocking { DatabaseHelper.updatePlayer(this@PlayerDTO) }
    }

    fun delete(): DatabaseHelper.DbOpResult<Boolean> {
        return runBlocking { DatabaseHelper.deletePlayer(this@PlayerDTO) }
    }
}

data class SolveDTO(val entity: SolveEntity) {
    val id = entity.id
    val playerProperty = SimpleObjectProperty(entity.player)
    var player by playerProperty

    val taskProperty = SimpleObjectProperty(entity.task)
    var task by taskProperty

    val timestampProperty = SimpleLongProperty(entity.timestamp)
    var timestamp by timestampProperty
}