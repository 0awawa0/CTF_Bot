package ui.players

import database.PlayerDTO
import database.ScoreDTO
import database.SolveDTO
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.paint.Paint
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.*
import ui.BaseView
import ui.Colors

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
                    val selectedPlayer = playersList.selectedItem ?: return@action
                    showAcceptPlayerDeletionDialog(selectedPlayer)
                }
            }
        }
    }

    private val playerName = label("") {
        style {
            fontSize = 24.px
            fontWeight = FontWeight.BLACK
        }

        onDoubleClick {
            playerNameEdit.text = this.text
            replaceWith(playerNameEdit)
        }
    }

    private val playerNameEdit: TextField = textfield {
        action {
            val newName = this.text
            if (newName.isNotEmpty()) {
                playerName.text = this.text
                viewModel.changeUserName(playerName.text)
            }
            replaceWith(playerName)
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
        val scoreColumn = column("Score", ScoreDTO::score) {
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        }.makeEditable()

        onEditCommit {
            if (this.tableColumn == scoreColumn) {
                it.score = this.newValue as Long
                viewModel.update(it)
            }
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

    private fun showAcceptPlayerDeletionDialog(player: PlayerDTO) {
        dialog("Delete player") {
            spacing = 8.0
            padding = Insets(8.0)
            alignment = Pos.CENTER
            prefWidth = 400.0

            label("Player deletion is unrecoverable. Are you sure you want to delete task") {
                textAlignment = TextAlignment.JUSTIFY
                isWrapText = true
            }.fitToParentWidth()

            label("'${player.name}'\n?") {
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
                        viewModel.deletePlayer(player)
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
}