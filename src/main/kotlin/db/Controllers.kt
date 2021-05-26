package db

import javafx.collections.ObservableList
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import tornadofx.Controller
import tornadofx.TableColumnDirtyState
import tornadofx.asObservable
import tornadofx.toObservable


class CompetitionsController: Controller() {
    private var data: ObservableList<CompetitionDTO> = emptyList<CompetitionDTO>().toObservable()

    fun setData(list: List<CompetitionDTO>) { data = list.toObservable() }

    fun add(competition: CompetitionDTO) { data.add(competition) }

    fun commitChanges(changes: Sequence<Map.Entry<CompetitionDTO, TableColumnDirtyState<CompetitionDTO>>>) {
        changes.filter { it.value.isDirty }.forEach {
            it.key.commit()
            it.value.commit()
        }
    }
}

class TasksController: Controller() {
    private var data: ObservableList<TaskDTO> = emptyList<TaskDTO>().toObservable()

    fun setData(list: List<TaskDTO>) { data = list.toObservable()}
    fun add(task: TaskDTO) { data.add(task) }

    fun commitChanges(changes: Sequence<Map.Entry<TaskDTO, TableColumnDirtyState<TaskDTO>>>) {
        changes.filter { it.value.isDirty }.forEach {
            it.key.commit()
            it.value.commit()
        }
    }
}

class PlayersController: Controller() {
    private var data: ObservableList<PlayerDTO> = emptyList<PlayerDTO>().toObservable()

    fun setData(list: List<PlayerDTO>) { data = list.toObservable() }
    fun add(player: PlayerDTO) { data.add(player) }

    fun commitChanges(changes: Sequence<Map.Entry<PlayerDTO, TableColumnDirtyState<PlayerDTO>>>) {
        changes.filter { it.value.isDirty }.forEach {
            it.key.commit()
            it.value.commit()
        }
    }
}

class SolvesController: Controller() {
    private var data: ObservableList<SolveDTO> = emptyList<SolveDTO>().toObservable()

    fun setData(list: List<SolveDTO>) { data = list.toObservable() }
    fun add(solve: SolveDTO) { data.add(solve) }
}