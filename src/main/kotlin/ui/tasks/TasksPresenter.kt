package ui.tasks

import db.DatabaseHelper
import db.models.TaskModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.TableColumnDirtyState

class TasksPresenter(private val view: TasksView) {

    fun loadTasksList() {
        GlobalScope.launch {
            val tasksList = DatabaseHelper.tasksController.tasksList
            view.onTasksListReady(tasksList)
        }
    }

    fun addNewTask(
        category: String,
        name: String,
        description: String,
        price: Int,
        flag: String,
        files: String,
        ctfName: String
    ) {
        GlobalScope.launch {
            DatabaseHelper.addNewTask(
                category,
                name,
                description,
                price,
                flag,
                files,
                ctfName
            )
        }
    }

    fun deleteTask(model: TaskModel) {
        GlobalScope.launch {
            DatabaseHelper.deleteTask(model)
        }
    }

    fun updateDatabase(changes: Sequence<Map.Entry<TaskModel, TableColumnDirtyState<TaskModel>>>) {
        DatabaseHelper.tasksController.commitChanges(changes)
    }
}