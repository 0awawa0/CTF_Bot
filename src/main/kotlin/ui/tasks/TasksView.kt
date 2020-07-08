package ui.tasks

import db.DatabaseHelper
import db.models.TaskModel
import javafx.geometry.Insets
import javafx.scene.control.TableView
import javafx.scene.text.Font
import javafx.stage.DirectoryChooser
import tornadofx.*
import java.nio.file.FileSystem
import java.nio.file.Paths


class TasksView: View("Tasks") {

    private var tasksTable: TableViewEditModel<TaskModel> by singleAssign()
    private val presenter = TasksPresenter(this)

    private val tasks = tableview<TaskModel>{
        tasksTable = editModel
        items = DatabaseHelper.tasksController.tasksList
        columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

        column("Category", TaskModel::category)
        column("Name", TaskModel::name)
        column("Description", TaskModel::description)
        column("Price", TaskModel::price)
        column("Flag", TaskModel::flag)
        column("Files directory", TaskModel::filesDirectory)
        column("CTF name", TaskModel::ctfName)
    }

    private val lblCtfName = label {
        text = "CTF name"
        font = Font(16.0)
    }
    private val tfCtfName = textfield { font = Font(13.0) }

    private val lblCategory = label {
        text = "Category"
        font = Font(16.0)
    }
    private val tfCategory = textfield { font = Font(13.0) }

    private val lblName = label {
        text = "Name"
        font = Font(16.0)
    }
    private val tfName = textfield { font = Font(13.0) }

    private val lblDescription = label {
        text = "Description"
        font = Font(16.0)
    }
    private val taDescription = textarea { font = Font(13.0) }

    private val lblPrice = label {
        text = "Price"
        font = Font(16.0)
    }
    private val tfPrice = textfield { font = Font(13.0) }

    private val lblFlag = label {
        text = "Flag"
        font = Font(16.0)
    }
    private val tfFlag = textfield { font = Font(13.0) }

    private val lblFiles = label { font = Font(16.0) }
    private val btFiles = button {
        text = "Add files directory"
        font = Font(13.0)

        action {
            lblFiles.text = DirectoryChooser()
                .showDialog(primaryStage)
                .absolutePath.replace(Paths.get("").toAbsolutePath().toString(), ".")

        }
    }

    private val btAddTask = button {
        text = "Add task"
        font = Font(13.0)

        action {
            DatabaseHelper.addNewTask(
                tfCategory.text,
                tfName.text,
                taDescription.text,
                tfPrice.text.toInt(),
                tfFlag.text,
                lblFiles.text,
                tfCtfName.text
            )
        }
    }

    private val newTaskBox = gridpane {

        vgap = 10.0
        hgap = 10.0

        add(lblCtfName, 0, 0)
        add(tfCtfName, 1, 0)
        add(lblCategory, 0, 1)
        add(tfCategory, 1, 1)
        add(lblName, 0, 2)
        add(tfName, 1, 2)
        add(lblDescription, 0, 3)
        add(taDescription, 1, 3)
        add(lblPrice, 0, 4)
        add(tfPrice, 1, 4)
        add(lblFlag, 0, 5)
        add(tfFlag, 1, 5)
        add(lblFiles, 1, 6)
        add(btFiles, 0, 6)
        add(btAddTask, 0, 7)

        btAddTask.gridpaneConstraints {
            columnSpan = 2
        }
        btAddTask.fitToParentWidth()
    }

    private val header = hbox {
        add(newTaskBox)
    }

    override val root = vbox {
        padding = Insets(10.0)
        spacing = 15.0

        add(header)
        add(tasks)

        tasks.fitToParentWidth()
    }
}