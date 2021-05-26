//package ui.tasks
//
//import db.DatabaseHelper
//import db.TaskModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import tornadofx.TableColumnDirtyState
//
//class TasksPresenter(private val view: TasksView) {
//
//    fun loadTasksList() {
//        GlobalScope.launch(Dispatchers.IO) {
//            val tasksList = DatabaseHelper.tasksController.tasksList
//            view.onTasksListReady(tasksList)
//        }
//    }
//
//    fun addNewTask(
//        category: String,
//        name: String,
//        description: String,
//        price: Int,
//        flag: String,
//        files: String,
//        ctfName: String
//    ) {
//        GlobalScope.launch(Dispatchers.IO) {
//            DatabaseHelper.addNewTask(
//                category,
//                name,
//                description,
//                price,
//                flag,
//                files,
//                ctfName
//            )
//        }
//    }
//
//    fun deleteTask(model: TaskModel) {
//        model.item.delete()
//    }
//
//    fun updateDatabase(changes: Sequence<Map.Entry<TaskModel, TableColumnDirtyState<TaskModel>>>) {
//        DatabaseHelper.tasksController.commitChanges(changes)
//    }
//}