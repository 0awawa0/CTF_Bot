package ui.players

import db.DatabaseHelper
import db.models.PlayerModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.TableColumnDirtyState

class PlayersPresenter(private val view: PlayersView) {


    fun loadPlayersList() {
        GlobalScope.launch(Dispatchers.IO) {
            view.onPlayersListReady(DatabaseHelper.playersController.playersList)
        }
    }

    fun refreshCurrentScores() {
        GlobalScope.launch(Dispatchers.IO) {
            DatabaseHelper.refreshCurrentScores()
        }
    }

    fun refreshAllScores() {
        GlobalScope.launch(Dispatchers.IO) {
            DatabaseHelper.refreshAllScores()
        }
    }

    fun deletePlayer(model: PlayerModel?) {
        GlobalScope.launch(Dispatchers.IO) {
            DatabaseHelper.deletePlayer(model)
        }
    }
    fun deleteAllPlayers() {
        GlobalScope.launch(Dispatchers.IO) {
            DatabaseHelper.deleteAllPlayers()
        }
    }

    fun updateDatabase(changes: Sequence<Map.Entry<PlayerModel, TableColumnDirtyState<PlayerModel>>>) {
        DatabaseHelper.playersController.commitChanges(changes)
    }
}