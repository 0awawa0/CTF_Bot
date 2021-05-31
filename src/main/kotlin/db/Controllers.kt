package db

import javafx.collections.ObservableList
import tornadofx.Controller
import tornadofx.TableColumnDirtyState
import tornadofx.toObservable

fun <T: BaseDTO> List<T>.asDbController(filter: (T) -> Boolean): DbController<T> {
    return DbController(this.toObservable(), filter)
}

class DbController<T: BaseDTO>(
    private var data: ObservableList<T> = emptyList<T>().toObservable(),
    private val filter: (T) -> Boolean
): Controller(), DatabaseHelper.ChangeListener<T> {

    fun setData(list: List<T>) { data = list.filter(filter).toObservable() }

    fun commitChanges(changes: Sequence<Map.Entry<T, TableColumnDirtyState<T>>>) {
        changes.filter { it.value.isDirty }.forEach {
            it.key.commit()
            it.value.commit()
        }
    }

    override fun onAdd(value: T) {
        if (filter(value)) {
            data.add(value)
        }
    }

    override fun onUpdate(value: T) {
        val index = data.indexOfFirst { it.id == value.id }
        if (index != -1) {
            if (filter(value)) {
                data[index] = value
            } else {
                data.removeAt(index)
            }
        }
    }

    override fun onDelete(value: T) {
        data.removeAll { it.id == value.id }
    }
}