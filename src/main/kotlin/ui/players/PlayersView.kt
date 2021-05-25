package ui.players

import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.text.Font
import tornadofx.*
import java.util.*

class PlayersView: View("Players") {

    private var playersTable: TableViewEditModel<PlayerModel> by singleAssign()

    private val presenter = PlayersPresenter(this)

    private val players = tableview<PlayerModel>{
        playersTable = editModel
        columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

        column("Username", PlayerModel::userName).makeEditable()
        column("Current score", PlayerModel::currentScore).makeEditable()
        column("Season score", PlayerModel::seasonScore).makeEditable()
        column("Solved tasks", PlayerModel::solvedTasks).makeEditable()
        column("Last right answer", PlayerModel::lastRightAnswer) {
            value {
                SimpleDateFormat("dd.MM.YYYY HH:mm:ss").format(Date(it.value.lastRightAnswer.value))
            }
        }

        enableCellEditing()
        enableDirtyTracking()

        onEditCommit {
            enableButtons()
        }
    }

    private val btRefreshCurrentScores = button {
        text = "Refresh current scores"
        font = Font(14.0)
        action {
            presenter.refreshCurrentScores()
        }
    }
    private val btRefreshAllScores = button {
        text = "Refresh all scores"
        font = Font(14.0)
        action {
            presenter.refreshAllScores()
        }
    }

    private val btDeletePlayer = button {
        text = "Delete player"
        font = Font(14.0)
        action {
            when(val model = playersTable.tableView.selectedItem) {
                null -> return@action
                else -> presenter.deletePlayer(model)
            }
        }
    }

    private val btDeleteAllPlayers = button {
        text = "Delete all players"
        font = Font(14.0)
        action {
            presenter.deleteAllPlayers()
        }
    }

    private val btRollback = button {
        text = "Cancel changes"
        font = Font(14.0)
        action {
            playersTable.rollback()
            disableButtons()
        }
    }

    private val btSaveChanges = button {
        text = "Save changes"
        font = Font(14.0)
        action {
            presenter.updateDatabase(playersTable.items.asSequence())
            disableButtons()
        }
    }

    private val btSendMessageToSelected = button {
        text = "Send message to selected player"
        font = Font(14.0)
        action {
            presenter.sendMessageToSelectedPlayer(
                playersTable.tableView.selectedItem?.item?.id?.value,
                taMessage.text
            )
        }
    }

    private val btSendMessageToAll = button {
        text = "Send message to all players"
        font = Font(14.0)
        action {
            presenter.sendMessageToAll(
                taMessage.text
            )
        }
    }

    private val taMessage = textarea {
        font = Font(13.0)
    }

    private val messageBox = hbox {
        spacing = 10.0
        add(taMessage)
        taMessage.fitToParentWidth()
        taMessage.fitToParentHeight()
        val btns = vbox {
            spacing = 5.0
            add(btSendMessageToSelected)
            add(btSendMessageToAll)
            alignment = Pos.CENTER
        }
        btns.fitToParentWidth()
        btns.fitToParentHeight()
        btns.maxWidth = 500.0

        this.maxHeight = 400.0
    }
    private val buttons = hbox {
        spacing = 15.0

        alignment = Pos.CENTER

        add(btRefreshCurrentScores)
        add(btRefreshAllScores)
        add(btDeletePlayer)
        add(btDeleteAllPlayers)

        btRefreshCurrentScores.fitToParentWidth()
        btRefreshAllScores.fitToParentWidth()
        btDeletePlayer.fitToParentWidth()
        btDeleteAllPlayers.fitToParentWidth()

        maxHeight = 150.0
    }

    override val root = vbox {
        padding = Insets(10.0)
        spacing = 15.0

        add(messageBox)
        add(buttons)
        add(players)
        add(btSaveChanges)
        add(btRollback)

        messageBox.fitToParentWidth()
        messageBox.fitToParentHeight()
        buttons.fitToParentWidth()
        buttons.fitToParentHeight()
        players.fitToParentWidth()
        players.fitToParentHeight()
        btSaveChanges.fitToParentWidth()
        btRollback.fitToParentWidth()
    }

    init {
        disableButtons()
        presenter.loadPlayersList()
    }

    private fun enableButtons() {
        btSaveChanges.isDisable = false
        btRollback.isDisable = false
    }

    private fun disableButtons() {
        btSaveChanges.isDisable = true
        btRollback.isDisable = true
    }

    fun onPlayersListReady(playersList: ObservableList<PlayerModel>) { players.items = playersList }
}