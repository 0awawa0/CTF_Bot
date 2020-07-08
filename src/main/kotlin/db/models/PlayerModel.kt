package db.models

import db.entities.PlayerEntity
import tornadofx.ItemViewModel

class PlayerModel: ItemViewModel<PlayerEntity>() {

    val userName = bind(PlayerEntity::userName)
    val currentScore = bind(PlayerEntity::currentScore)
    val seasonScore = bind(PlayerEntity::seasonScore)
    val solvedTasks = bind(PlayerEntity::solvedTasks)
    val lastRightAnswer = bind(PlayerEntity::lastRightAnswer)
}