package ui.competitions

import db.CompetitionDTO
import db.TaskDTO
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TableView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import tornadofx.*

class CompetitionsView: View("Competitions") {

    private val viewModel = CompetitionsViewModel()
    private val viewScope = CoroutineScope(Dispatchers.JavaFx)
    private var flowJob: Job? = null

    private val competitionsList = listview<CompetitionDTO> {
        cellFormat { text = "${it.id.value}. ${it.name}" }
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

                action {

                }
            }
            button {
                text = "Delete"

                action {
                    val competition = competitionsList.selectedItem ?: return@action
                    viewModel.delete(competition)
                }
            }
        }.fitToParentWidth()
    }

    private val tasksTable = tableview<TaskDTO> {
        readonlyColumn("id", TaskDTO::id) { columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY }

        column("Category", TaskDTO::category)
        column("Name", TaskDTO::name)
        column("Description", TaskDTO::description)
        column("Price", TaskDTO::price)
        column("Flag", TaskDTO::flag)
        column("Attachment", TaskDTO::attachment)
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
            }

            button {
                text = "Delete"
            }
        }.fitToParentWidth()
    }

    override val root = gridpane {
        prefWidth = minOf(1000.0, primaryStage.maxWidth)
        prefHeight = 500.0

        row {
            add(leftPane)
            add(rightPane)

            constraintsForColumn(0).percentWidth = 15.0
            constraintsForColumn(1).percentWidth = 85.0
        }
    }

    override fun onDock() {
        super.onDock()

        flowJob = viewScope.launch {
            viewModel.competitions.onEach { competitionsList.items = it.toObservable() }.launchIn(this)
            viewModel.tasks.collect { tasksTable.items = it.toObservable() }
        }
    }

    override fun onUndock() {
        flowJob?.cancel()

        super.onUndock()
    }
}