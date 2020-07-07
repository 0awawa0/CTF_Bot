package ui.players

import db.DatabaseHelper
import db.models.PlayerModel
import javafx.geometry.Insets
import javafx.scene.control.TableView
import javafx.scene.text.Font
import tornadofx.*

class PlayersView: View("Players") {

    private var playersTable: TableViewEditModel<PlayerModel> by singleAssign()

    private val presenter = PlayersPresenter(this)

    private val players = tableview<PlayerModel>{
        playersTable = editModel
        items = DatabaseHelper.playersController.playersList
        columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

        column("Username", PlayerModel::userName)
        column("Current score", PlayerModel::currentScore)
        column("Season score", PlayerModel::seasonScore)
        column("Solved tasks", PlayerModel::solvedTasks)
    }

    private val refreshCurrentScoresButton = button {
        text = "Refresh current scores"
        font = Font(14.0)
        action {
            DatabaseHelper.refreshCurrentScores()
        }
    }
    private val refreshAllScoresButton = button {
        text = "Refresh all scores"
        font = Font(14.0)
        action {
            DatabaseHelper.refreshAllScores()
        }
    }

    private val buttons = hbox {
        spacing = 15.0

        add(refreshCurrentScoresButton)
        add(refreshAllScoresButton)
    }

    override val root = vbox {
        padding = Insets(10.0)
        spacing = 15.0

        add(buttons)
        add(players)

        players.fitToParentWidth()
    }
}