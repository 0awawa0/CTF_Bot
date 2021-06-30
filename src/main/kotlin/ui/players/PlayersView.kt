package ui.players

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

    private val playersList = listview<PlayersViewModel.PlayerItem> {
        cellFormat {
            graphic = hbox {
                alignment = Pos.CENTER_LEFT
                padding = Insets(0.0, 4.0, 0.0, 4.0)
                spacing = 8.0

                label(it.name) {
                    style {
                        fontSize = 14.px
                        fontWeight = FontWeight.BOLD
                    }
                }

                label(it.totalScore.toString()) {
                    style {
                        fontSize = 12.px
                        fontWeight = FontWeight.NORMAL
                    }
                }
            }
        }
        onUserSelect(1) {
            it.onSelected()
            playerName.text = it.name
            playerScore.text = it.totalScore.toString()
        }
    }

    private val leftPane = vbox {
        add(playersList)
        playersList.fitToParentSize()

        vbox {
            padding = Insets(8.0)
            alignment = Pos.CENTER
            spacing = 8.0

            button {
                text = "Send message to player"

                action {
                    val player = playersList.selectedItem ?: return@action
                    showSendMessageToPlayerDialog(player)
                }
            }.fitToParentWidth()

            button {
                text = "Broadcast message"

                action { showSendBroadcastMessageDialog() }
            }.fitToParentWidth()

            button {
                text = "Delete"
                action {
                    val selectedPlayer = playersList.selectedItem ?: return@action
                    showAcceptPlayerDeletionDialog(selectedPlayer)
                }
            }.fitToParentWidth()
        }.fitToParentWidth()
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
                playersList.selectedItem?.apply {
                    this.name = playerName.text
                    pushChanges()
                }
            }
            replaceWith(playerName)
        }
    }

    private val playerScore = label("") {
        style {
            fontSize = 20.px
        }
    }

    private val scoresTable = tableview<PlayersViewModel.CompetitionItem> {

        readonlyColumn("Competition", PlayersViewModel.CompetitionItem::name)
        val scoreColumn = column("Score", PlayersViewModel.CompetitionItem::score) {
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        }
        onUserSelect(1) { it.onSelected() }
    }

    private val solvesTable = tableview<PlayersViewModel.SolveItem> {
        readonlyColumn("Task", PlayersViewModel.SolveItem::taskName)
        readonlyColumn("Timestamp", PlayersViewModel.SolveItem::timestamp) {
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

            constraintsForColumn(0).percentWidth = 20.0
            constraintsForColumn(1).percentWidth = 80.0
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

    private fun showAcceptPlayerDeletionDialog(player: PlayersViewModel.PlayerItem) {
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
                        player.delete()
                        playerName.text = ""
                        playerScore.text = ""
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

    private fun showSendMessageToPlayerDialog(player: PlayersViewModel.PlayerItem) {
        dialog {
            stage.centerOnScreen()
            prefWidth = 500.0
            prefHeight = 250.0
            spacing = 8.0
            padding = Insets(6.0)

            val messageText = textarea()
            messageText.fitToParentSize()

            hbox {
                spacing = 8.0

                button {
                    text = "Send"

                    action {
                        if (messageText.text.isNotBlank()) player.sendMessage(messageText.text)
                        close()
                    }
                }.fitToParentWidth()

                button {
                    text = "Cancel"

                    action { close() }
                }.fitToParentWidth()
            }
        }
    }

    private fun showSendBroadcastMessageDialog() {
        dialog {
            stage.centerOnScreen()
            prefWidth = 500.0
            prefHeight = 250.0
            spacing = 8.0
            padding = Insets(6.0)

            val messageText = textarea()
            messageText.fitToParentSize()

            hbox {
                spacing = 8.0

                button {
                    text = "Send"

                    action {
                        if (messageText.text.isNotBlank()) viewModel.broadcastMessage(messageText.text)
                        close()
                    }
                }.fitToParentWidth()

                button {
                    text = "Cancel"

                    action { close() }
                }.fitToParentWidth()
            }
        }
    }
}