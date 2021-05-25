package db

import tornadofx.Controller
import tornadofx.TableColumnDirtyState
import tornadofx.asObservable


class CompetitionsController: Controller() {
    val data by lazy {
        DatabaseHelper.perform {
            CompetitionEntity.all().map { CompetitionDTO(it) }.asObservable()
        }
    }

    fun add(competition: CompetitionDTO) { data.add(competition) }

    fun commitChanges(changes: Sequence<Map.Entry<CompetitionDTO, TableColumnDirtyState<CompetitionDTO>>>) {
        changes.filter { it.value.isDirty }.forEach {
            it.key.commit()
            it.value.commit()
        }
    }
}

class TasksController: Controller() {
    val data by lazy {
        DatabaseHelper.perform {
            TaskEntity.all().map { TaskDTO(it) }.asObservable()
        }
    }

    fun add(task: TaskDTO) { data.add(task) }

    fun commitChanges(changes: Sequence<Map.Entry<TaskDTO, TableColumnDirtyState<TaskDTO>>>) {
        changes.filter { it.value.isDirty }.forEach {
            it.key.commit()
            it.value.commit()
        }
    }
}

class PlayersController: Controller() {
    val data by lazy {
        DatabaseHelper.perform {
            PlayerEntity.all().map { PlayerDTO(it) }.asObservable()
        }
    }

    fun add(player: PlayerDTO) { data.add(player) }

    fun commitChanges(changes: Sequence<Map.Entry<PlayerDTO, TableColumnDirtyState<PlayerDTO>>>) {
        changes.filter { it.value.isDirty }.forEach {
            it.key.commit()
            it.value.commit()
        }
    }
}

class SolvesController: Controller() {
    val list by lazy {
        DatabaseHelper.perform {
            SolveEntity.all().map {
                SolveDTO(it)
            }.asObservable()
        }
    }
}