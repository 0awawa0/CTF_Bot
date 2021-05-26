//package ui.tasks
//
//import javafx.collections.ObservableList
//import javafx.geometry.Insets
//import javafx.geometry.Pos
//import javafx.scene.control.TableView
//import javafx.scene.text.Font
//import javafx.stage.DirectoryChooser
//import tornadofx.*
//import java.io.File
//import java.nio.file.Paths
//
//
//class TasksView: View("Tasks") {
//
//    private var lastPickedDirectory = ""
//    private var tasksTable: TableViewEditModel<TaskModel> by singleAssign()
//    private val presenter = TasksPresenter(this)
//
//    private val tasks = tableview<TaskModel>{
//        tasksTable = editModel
//        columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
//
//        column("Category", TaskModel::category).makeEditable()
//        column("Name", TaskModel::name).makeEditable()
//        column("Description", TaskModel::description).makeEditable()
//        column("Price", TaskModel::price).makeEditable()
//        column("Flag", TaskModel::flag).makeEditable()
//        column("Files directory", TaskModel::filesDirectory).makeEditable()
//        column("CTF name", TaskModel::ctfName).makeEditable()
//
//        enableCellEditing()
//        enableDirtyTracking()
//
//        onEditCommit {
//            enableButtons()
//        }
//    }
//
//    private val lblCtfName = label {
//        text = "CTF name"
//        font = Font(16.0)
//        maxWidth = 350.0
//    }
//    private val tfCtfName = textfield {
//        font = Font(13.0)
//    }
//
//    private val lblCategory = label {
//        text = "Category"
//        font = Font(16.0)
//        maxWidth = 350.0
//    }
//    private val tfCategory = textfield {
//        font = Font(13.0)
//    }
//
//    private val lblName = label {
//        text = "Name"
//        font = Font(16.0)
//        maxWidth = 350.0
//    }
//    private val tfName = textfield {
//        font = Font(13.0)
//    }
//
//    private val lblDescription = label {
//        text = "Description"
//        font = Font(16.0)
//        maxWidth = 350.0
//    }
//    private val taDescription = textarea {
//        font = Font(13.0)
//    }
//
//    private val lblPrice = label {
//        text = "Price"
//        font = Font(16.0)
//        maxWidth = 350.0
//    }
//    private val tfPrice = textfield {
//        font = Font(13.0)
//    }
//
//    private val lblFlag = label {
//        text = "Flag"
//        font = Font(16.0)
//        maxWidth = 350.0
//    }
//    private val tfFlag = textfield {
//        font = Font(13.0)
//    }
//
//    private val lblFiles = label {
//        font = Font(16.0)
//    }
//    private val btFiles = button {
//        text = "Add files directory"
//        font = Font(13.0)
//        maxWidth = 350.0
//
//        action {
//            val chooser = DirectoryChooser()
//            val initialDirectory = File(lastPickedDirectory)
//            chooser.initialDirectory = if (initialDirectory.exists())
//                initialDirectory
//            else
//                File(Paths.get("").toAbsolutePath().toString())
//
//            val selectedPath = chooser.showDialog(currentStage)?.absolutePath
//            if (selectedPath != null) { lastPickedDirectory = selectedPath }
//
//            lblFiles.text = selectedPath?.replace(Paths.get("")
//                .toAbsolutePath()
//                .toString(), ".") ?: ""
//
//        }
//    }
//
//    private val btAddTask = button {
//        text = "Add task"
//        font = Font(13.0)
//
//        action {
//            presenter.addNewTask(
//                tfCategory.text,
//                tfName.text,
//                taDescription.text,
//                tfPrice.text.toInt(),
//                tfFlag.text,
//                lblFiles.text,
//                tfCtfName.text
//            )
//        }
//    }
//
//    private val btSaveChanges = button {
//        text = "Save database changes"
//        font = Font(13.0)
//        action {
//            presenter.updateDatabase(tasksTable.items.asSequence())
//            disableButtons()
//        }
//    }
//
//    private val btRollback = button {
//        text = "Cancel changes"
//        font = Font(13.0)
//        action {
//            tasksTable.rollback()
//            disableButtons()
//        }
//    }
//
//    private val btDeleteTask = button {
//        text = "Delete task"
//        font = Font(13.0)
//        maxWidth = 350.0
//
//        action {
//            when(val model = tasksTable.tableView.selectedItem) {
//                null -> return@action
//                else -> presenter.deleteTask(model)
//            }
//        }
//    }
//
//    private val newTaskBox = gridpane {
//
//        vgap = 10.0
//        hgap = 10.0
//
//        add(lblCtfName, 0, 0)
//        add(tfCtfName, 1, 0)
//        add(lblCategory, 0, 1)
//        add(tfCategory, 1, 1)
//        add(lblName, 0, 2)
//        add(tfName, 1, 2)
//        add(lblDescription, 0, 3)
//        add(taDescription, 1, 3)
//        add(lblPrice, 0, 4)
//        add(tfPrice, 1, 4)
//        add(lblFlag, 0, 5)
//        add(tfFlag, 1, 5)
//        add(lblFiles, 1, 6)
//        add(btFiles, 0, 6)
//        add(btAddTask, 0, 7)
//
//        btAddTask.gridpaneConstraints {
//            columnSpan = 2
//        }
//
//        lblCtfName.fitToParentWidth()
//        tfCtfName.fitToParentWidth()
//        lblCategory.fitToParentWidth()
//        tfCategory.fitToParentWidth()
//        lblName.fitToParentWidth()
//        tfName.fitToParentWidth()
//        lblDescription.fitToParentWidth()
//        taDescription.fitToParentWidth()
//        lblPrice.fitToParentWidth()
//        tfPrice.fitToParentWidth()
//        lblFlag.fitToParentWidth()
//        tfFlag.fitToParentWidth()
//        lblFiles.fitToParentWidth()
//        btFiles.fitToParentWidth()
//        btAddTask.fitToParentWidth()
//    }
//
//    private val additionalButtonsBox = vbox {
//        alignment = Pos.TOP_CENTER
//
//        add(btDeleteTask)
//        btDeleteTask.fitToParentWidth()
//
//        maxWidth = 400.0
//    }
//
//    private val header = hbox {
//        spacing = 10.0
//
//        add(newTaskBox)
//        add(additionalButtonsBox)
//
//        newTaskBox.fitToParentWidth()
//        additionalButtonsBox.fitToParentWidth()
//    }
//
//    override val root = vbox {
//        padding = Insets(10.0)
//        spacing = 15.0
//
//        add(header)
//        add(tasks)
//
//        header.fitToParentWidth()
//
//        add(btSaveChanges)
//        btSaveChanges.fitToParentWidth()
//
//        add(btRollback)
//        btRollback.fitToParentWidth()
//
//        tasks.fitToParentWidth()
//    }
//
//    fun onTasksListReady(tasksList: ObservableList<TaskModel>) { tasks.items = tasksList }
//
//    private fun enableButtons() {
//        btSaveChanges.isDisable = false
//        btRollback.isDisable = false
//    }
//
//    private fun disableButtons() {
//        btSaveChanges.isDisable = true
//        btRollback.isDisable = true
//    }
//
//    init {
//        disableButtons()
//        presenter.loadTasksList()
//    }
//}