package db

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.getValue
import tornadofx.setValue

data class CompetitionDTO(private val entity: CompetitionEntity) {
    val nameProperty = SimpleStringProperty(entity.name)
    var name by nameProperty

    fun commit() {
        DatabaseHelper.perform {
            entity.name = name
        }
    }

    fun delete() {
        DatabaseHelper.perform {
            entity.delete()
        }
    }
}

data class TaskDTO(private val entity: TaskEntity) {
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

    fun commit() {
        DatabaseHelper.perform {
            entity.category = category
            entity.name = name
            entity.description = description
            entity.price = price
            entity.flag = flag
            entity.attachment = attachment
            entity.competition = competition
        }
    }

    fun delete() {
        DatabaseHelper.perform {
            entity.delete()
        }
    }
}

data class PlayerDTO(private val entity: PlayerEntity) {

    val userNameProperty = SimpleStringProperty(entity.userName)
    var userName by userNameProperty

    val currentScoreProperty = SimpleIntegerProperty(entity.currentScore)
    var currentScore by currentScoreProperty

    val seasonScoreProperty = SimpleIntegerProperty(entity.seasonScore)
    var seasonScore by seasonScoreProperty

    fun commit() {
        DatabaseHelper.perform {
            entity.userName = userName
            entity.currentScore = currentScore
            entity.seasonScore = seasonScore
        }
    }

    fun delete() {
        DatabaseHelper.perform {
            entity.delete()
        }
    }
}

data class SolveDTO(private val entity: SolveEntity) {

    val playerProperty = SimpleObjectProperty(entity.player)
    var player by playerProperty

    val taskProperty = SimpleObjectProperty(entity.task)
    var task by taskProperty

    val timestampProperty = SimpleLongProperty(entity.timestamp)
    var timestamp by timestampProperty

    fun commit() {
        DatabaseHelper.perform {
            entity.player = player
            entity.task = task
            entity.timestamp = timestamp
        }
    }

    fun delete() {
        DatabaseHelper.perform {
            entity.delete()
        }
    }
}