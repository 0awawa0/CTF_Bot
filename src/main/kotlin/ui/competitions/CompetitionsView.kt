package ui.competitions

import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.paint.Paint
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import javafx.stage.FileChooser
import tornadofx.*
import ui.BaseView
import ui.Colors

class CompetitionsView: BaseView<CompetitionsViewModel>(CompetitionsViewModel(), "Competitions") {

    private val competitionsList = listview<CompetitionsViewModel.CompetitionItem> {

        cellFormat {
            text = it.name
            style {
                fontWeight = FontWeight.BOLD
                fontSize = 16.px
            }
        }

        onUserSelect(clickCount = 1) {
            competitionName.text = it.name
            competitionNameEdit.text = it.name
            it.onSelected()
        }
    }

    private val leftPane = vbox {
        add(competitionsList)
        competitionsList.fitToParentSize()

        hbox {
            padding = Insets(8.0)
            alignment = Pos.CENTER
            spacing = 8.0

            button {
                text = "Add"

                action { showNewCompetitionDialog() }
            }
            button {
                text = "Delete"

                action {
                    val competition = competitionsList.selectedItem ?: return@action
                    showAcceptCompetitionDeletionDialog(competition)
                }
            }
        }.fitToParentWidth()
    }

    private val tasksTable = tableview<CompetitionsViewModel.TaskItem> {
        readonlyColumn("id", CompetitionsViewModel.TaskItem::id) {
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        }

        val columnCategory = column("Category", CompetitionsViewModel.TaskItem::category).makeEditable()
        val columnName = column("Name", CompetitionsViewModel.TaskItem::name).makeEditable()
        val columnDescription = column("Description", CompetitionsViewModel.TaskItem::description).makeEditable()
        val columnFlag = column("Flag", CompetitionsViewModel.TaskItem::flag).makeEditable()
        val columnAttachment = column("Attachment", CompetitionsViewModel.TaskItem::attachment).makeEditable()
        readonlyColumn("Solves count", CompetitionsViewModel.TaskItem::solvesCount)

        onEditCommit {
            val item = selectedItem ?: return@onEditCommit
            when (this.tableColumn) {
                columnCategory -> item.category = this.newValue as String
                columnName -> item.name = this.newValue as String
                columnDescription -> item.description = this.newValue as String
                columnFlag -> item.flag = this.newValue as String
                columnAttachment -> item.attachment = this.newValue as String
            }

            item.pushChanges()
        }
    }

    private val tasksSection = vbox {
        add(tasksTable)
        tasksTable.fitToParentSize()

        hbox {
            spacing = 8.0
            padding = Insets(8.0)
            alignment = Pos.CENTER

            button {
                text = "Add"

                action { showNewTaskDialog() }
            }

            button {
                text = "Add from JSON"

                action {
                    val competition = competitionsList.selectedItem ?: return@action
                    val file = chooseFile(
                        "Choose file",
                        arrayOf(FileChooser.ExtensionFilter("JSON", "*.json"))
                    ).firstOrNull() ?: return@action
                    competition.addTasksFromJson(file) { showFailedToParseJson() }
                }
            }

            button {
                text = "Delete"

                action {
                    val task = tasksTable.selectedItem ?: return@action
                    showAcceptTaskDeletionDialog(task)
                }
            }

        }.fitToParentWidth()
    }

    private val scoreboardTable = tableview<CompetitionsViewModel.ScoreboardItem> {
        readonlyColumn("Player", CompetitionsViewModel.ScoreboardItem::playerName)
        readonlyColumn("Score", CompetitionsViewModel.ScoreboardItem::playerScore) {
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        }
    }

    private val competitionName = label("") {
        padding = Insets(8.0)

        style {
            textAlignment = TextAlignment.CENTER
            fontSize = 24.px
            fontWeight = FontWeight.BLACK
        }

        onDoubleClick {
            competitionNameEdit.text = this.text
            replaceWith(competitionNameEdit)
        }
    }

    private val competitionNameEdit: TextField = textfield {
        padding = competitionName.padding
        alignment = Pos.CENTER
        style {
            textAlignment = TextAlignment.CENTER
            fontSize = 24.px
        }
        action {
            val newName = this.text
            if (newName.isNotEmpty()) {
                competitionName.text = this.text
                competitionsList.selectedItem?.apply {
                    this.name = competitionName.text
                    pushChanges()
                }
                replaceWith(competitionName)
            }
        }
    }

    private val rightPane = vbox {

        alignment = Pos.CENTER

        add(competitionName)
        splitpane(orientation = Orientation.HORIZONTAL) {
            add(tasksSection)
            add(scoreboardTable)

            this.setDividerPositions(0.9)
        }.fitToParentSize()
    }

    override val root = gridpane {

        row {
            add(leftPane)
            add(rightPane)

            constraintsForColumn(0).percentWidth = 15.0
            constraintsForColumn(1).percentWidth = 85.0
        }
    }

    override fun onDock() {
        super.onDock()

        currentStage?.width =  minOf(1000.0, primaryStage.maxWidth)
        currentStage?.height = 500.0
        currentStage?.centerOnScreen()

        competitionsList.items = viewModel.competitions
        tasksTable.items = viewModel.tasks
        scoreboardTable.items = viewModel.scoreboard
    }

    override fun onUndock() {
        viewModel.onViewUndock()
        super.onUndock()
    }

    private fun showNewCompetitionDialog() {
        dialog("New Competition") {
            spacing = 8.0
            alignment = Pos.CENTER
            padding = Insets(8.0)
            stage.width = 300.0
            stage.centerOnScreen()

            val competitionName = textfield()
            hbox {
                spacing = 8.0
                alignment = Pos.CENTER

                label("Name") {
                    textAlignment = TextAlignment.RIGHT
                }.fitToParentWidth()
                add(competitionName)
                competitionName.fitToParentWidth()
                competitionName.action {
                    if (competitionName.text.isBlank()) return@action
                    viewModel.addCompetition(competitionName.text)
                    this@dialog.close()
                }
            }.fitToParentWidth()

            hbox {
                spacing = 8.0
                alignment = Pos.CENTER

                button {
                    text = "OK"

                    action {
                        viewModel.addCompetition(competitionName.text)
                        this@dialog.close()
                    }
                }.fitToParentWidth()

                button {
                    text = "Cancel"

                    action {
                        this@dialog.close()
                    }
                }.fitToParentWidth()

            }.fitToParentWidth()
        }
    }

    private fun showAcceptCompetitionDeletionDialog(competition: CompetitionsViewModel.CompetitionItem) {
        dialog("Delete competition") {
            spacing = 8.0
            padding = Insets(8.0)
            alignment = Pos.CENTER
            prefWidth = 400.0

            label(
                "Competition deletion is unrecoverable. Are you sure you want to delete competition"
            ) {
                textAlignment = TextAlignment.JUSTIFY
                isWrapText = true
            }.fitToParentWidth()

            label("'${competition.name}'\n?") {
                style {
                    textAlignment = TextAlignment.CENTER
                    fontWeight = FontWeight.BOLD
                }
            }
            hbox {
                spacing = 8.0
                alignment = Pos.CENTER

                button {
                    text = "Yes"
                    textFill = Paint.valueOf(Colors.RED)

                    action {
                        competition.delete()
                        this@dialog.close()
                    }
                }.fitToParentWidth()

                button {
                    text = "No"
                    textFill = Paint.valueOf(Colors.GREEN)

                    action { this@dialog.close() }
                }.fitToParentWidth()
            }.fitToParentWidth()
        }
    }

    private fun showNewTaskDialog() {
        val competition = competitionsList.selectedItem ?: return
        val attachmentContent = label("")
        val category = textfield()
        val name = textfield()
        val description = textarea()
        val attachment = hbox {
            spacing = 8.0
            alignment = Pos.CENTER
            button {
                text = "Browse..."

                action {
                    val file = chooseFile(
                        "Choose file",
                        filters = arrayOf(FileChooser.ExtensionFilter("*", "*.*"))
                    ).firstOrNull()
                    attachmentContent.text = file?.absolutePath ?: ""
                }
            }
            add(attachmentContent)
        }
        val flag = textfield()

        dialog("New task ") {
            stage.width = 500.0
            stage.centerOnScreen()

            form {
                field("Category") { add(category) }
                field("Name") { add(name) }
                field("Description") { add(description) }
                field("Flag") { add(flag) }
                field("Attachment") { add(attachment) }
                hbox {
                    spacing = 8.0
                    button {
                        text = "Add"

                        action {
                            if (category.text.isBlank()) return@action
                            if (name.text.isBlank()) return@action
                            if (flag.text.isBlank()) return@action
                            competition.addTask(
                                category.text,
                                name.text,
                                description.text,
                                flag.text,
                                attachmentContent.text
                            )
                            this@dialog.close()
                        }
                    }.fitToParentWidth()

                    button {
                        text = "Cancel"

                        action { this@dialog.close() }
                    }.fitToParentWidth()
                }.fitToParentWidth()
            }
        }
    }

    private fun showAcceptTaskDeletionDialog(task: CompetitionsViewModel.TaskItem) {
        dialog("Delete task") {
            spacing = 8.0
            padding = Insets(8.0)
            alignment = Pos.CENTER
            prefWidth = 400.0

            label(
                "Task deletion is unrecoverable. Are you sure you want to delete task"
            ) {
                textAlignment = TextAlignment.JUSTIFY
                isWrapText = true
            }.fitToParentWidth()

            label("'${task.name}'\n?") {
                style {
                    textAlignment = TextAlignment.CENTER
                    fontWeight = FontWeight.BOLD
                }
            }
            hbox {
                spacing = 8.0
                alignment = Pos.CENTER

                button {
                    text = "Yes"
                    textFill = Paint.valueOf(Colors.RED)

                    action {
                        task.delete()
                        this@dialog.close()
                    }
                }.fitToParentWidth()

                button {
                    text = "No"
                    textFill = Paint.valueOf(Colors.GREEN)

                    action { this@dialog.close() }
                }.fitToParentWidth()
            }.fitToParentWidth()
        }
    }

    private fun showFailedToParseJson() {
        alert(
            Alert.AlertType.ERROR,
            header = "JSON parsing failed",
            "Failed to import tasks from JSON, check your JSON file",
            ButtonType.OK
        )
    }
}