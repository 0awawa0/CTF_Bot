package ui.players

import database.PlayerDTO
import database.ScoreDTO
import database.SolveDTO
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.text.FontWeight
import tornadofx.*
import ui.BaseView

class PlayersView: BaseView<PlayersViewModel>(PlayersViewModel(), "Players") {

    private val playersList = listview<PlayerDTO> {
        cellFormat {
            graphic = hbox {
                padding = Insets(0.0, 4.0, 0.0, 4.0)
                spacing = 8.0

                label(it.name) {
                    style {
                        fontSize = 14.px
                        fontWeight = FontWeight.BOLD
                    }
                }

                label(it.getTotalScoreSynchronous().toString()) {
                    style {
                        fontSize = 12.px
                        fontWeight = FontWeight.NORMAL
                    }
                }
            }
        }
        onUserSelect(1) {
            viewModel.selectedPlayer = it
            playerName.text = it.name
            playerScore.text = it.getTotalScoreSynchronous().toString()
        }
    }

    private val leftPane = vbox {
        add(playersList)
        playersList.fitToParentSize()

        hbox {
            padding = Insets(8.0)
            alignment = Pos.CENTER
            spacing = 8.0

            button {
                text = "Delete"
                action {
                    val player = playersList.selectedItem
                }
            }
        }
    }

    private val playerName = label("") {
        style {
            fontSize = 24.px
            fontWeight = FontWeight.BLACK
        }
    }

    private val playerScore = label("") {
        style {
            fontSize = 20.px
        }
    }

    private val scoresTable = tableview<ScoreDTO> {

        column("Competition", ScoreDTO::getCompetitionSynchronous).cellFormat {
            text = it.name
        }
        column("Score", ScoreDTO::score) {
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        }

        onUserSelect(1) { viewModel.selectedScore = it }
    }

    private val solvesTable = tableview<SolveDTO> {
        column("Task", SolveDTO::getTaskSynchronous).cellFormat {
            text = it.name
        }
        readonlyColumn("Timestamp", SolveDTO::timestamp) {
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        }
    }

    private val tables = hbox {
        add(scoresTable)
        add(solvesTable)
        scoresTable.fitToParentSize()
        solvesTable.fitToParentSize()
    }

    private val rightPane = vbox {
        padding = Insets(8.0, 0.0, 0.0, 0.0)
        spacing = 8.0
        alignment = Pos.CENTER
        add(playerName)
        add(playerScore)
        add(tables)

        tables.fitToParentSize()
    }

    override val root =  gridpane {
        row {
            add(leftPane)
            add(rightPane)

            constraintsForColumn(0).percentWidth = 25.0
            constraintsForColumn(1).percentWidth = 85.0
        }
    }

    override fun onDock() {
        super.onDock()

        currentStage?.width = minOf(1000.0, primaryStage.maxWidth)
        currentStage?.height = 500.0
        currentStage?.centerOnScreen()

        playersList.items = viewModel.scoreBoard
        scoresTable.items = viewModel.scores
        solvesTable.items = viewModel.solves
    }
}