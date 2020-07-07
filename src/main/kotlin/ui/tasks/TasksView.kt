package ui.tasks

import javafx.scene.Parent
import tornadofx.View
import tornadofx.vbox


class TasksView: View("Tasks") {

    private val presenter = TasksPresenter(this)

    override val root = vbox {

    }
}