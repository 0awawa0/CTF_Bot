package ui.competitions

import database.CompetitionDTO
import database.TaskDTO
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TableView
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import javafx.stage.FileChooser
import tornadofx.*
import ui.Colors

class CompetitionsView: View("Competitions") {

    private val viewModel = CompetitionsViewModel()

    private val competitionsList = listview<CompetitionDTO> {
        cellFormat {
            text = it.name
            style {
                fontWeight = FontWeight.BOLD
                fontSize = 20.px
            }
        }
        onUserSelect(clickCount = 1) { viewModel.selectedCompetition = it }
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

    private val tasksTable = tableview<TaskDTO> {
        readonlyColumn("id", TaskDTO::id) {
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        }

        val columnCategory = column("Category", TaskDTO::category).makeEditable()
        val columnName = column("Name", TaskDTO::name).makeEditable()
        val columnDescription = column("Description", TaskDTO::description).makeEditable()
        val columnFlag = column("Flag", TaskDTO::flag).makeEditable()
        val columnAttachment = column("Attachment", TaskDTO::attachment).makeEditable()
        column("Solves count", TaskDTO::getSolvesCountSynchronous)

        onEditCommit {
            val item = selectedItem ?: return@onEditCommit
            when (this.tableColumn) {
                columnCategory -> item.category = this.newValue as String
                columnName -> item.name = this.newValue as String
                columnDescription -> item.description = this.newValue as String
                columnFlag -> item.flag = this.newValue as String
                columnAttachment -> item.attachment = this.newValue as String
            }

            viewModel.update(item)
        }
    }

    private val rightPane = vbox {
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
                    viewModel.tryAddFromJson(file, competition) { showFailedToParseJson() }
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

        viewModel.onViewDock()
        competitionsList.items = viewModel.competitions
        tasksTable.items = viewModel.tasks
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

    private fun showAcceptCompetitionDeletionDialog(competition: CompetitionDTO) {
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
                        viewModel.delete(competition)
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
                            viewModel.addTask(
                                category.text,
                                name.text,
                                description.text,
                                flag.text,
                                attachmentContent.text,
                                competition
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

    private fun showAcceptTaskDeletionDialog(task: TaskDTO) {
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
                        viewModel.delete(task)
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