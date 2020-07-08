package db.models

import db.entities.TaskEntity
import tornadofx.ItemViewModel

class TaskModel: ItemViewModel<TaskEntity>() {

    val category = bind(TaskEntity::category)
    val name = bind(TaskEntity::name)
    val description = bind(TaskEntity::description)
    val price = bind(TaskEntity::price)
    val flag = bind(TaskEntity::flag)
    val filesDirectory = bind(TaskEntity::filesDirectory)
    val ctfName = bind(TaskEntity::ctfName)
}