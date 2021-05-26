//package ui.players
//
//import db.DatabaseHelper
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import tornadofx.TableColumnDirtyState
//import ui.Application
//
//class PlayersPresenter(private val view: PlayersView) {
//
//
//    fun loadPlayersList() {
//        GlobalScope.launch(Dispatchers.IO) {
//            view.onPlayersListReady(DatabaseHelper.playersController.playersList)
//        }
//    }
//
//    fun refreshCurrentScores() {
//        GlobalScope.launch(Dispatchers.IO) {
//            DatabaseHelper.refreshCurrentScores()
//        }
//    }
//
//    fun refreshAllScores() {
//        GlobalScope.launch(Dispatchers.IO) {
//            DatabaseHelper.refreshAllScores()
//        }
//    }
//
//    fun deletePlayer(model: PlayerModel?) {
//        GlobalScope.launch(Dispatchers.IO) {
//            DatabaseHelper.deletePlayer(model)
//        }
//    }
//    fun deleteAllPlayers() {
//        GlobalScope.launch(Dispatchers.IO) {
//            DatabaseHelper.deleteAllPlayers()
//        }
//    }
//
//    fun updateDatabase(changes: Sequence<Map.Entry<PlayerModel, TableColumnDirtyState<PlayerModel>>>) {
//        DatabaseHelper.playersController.commitChanges(changes)
//    }
//
//    fun sendMessageToSelectedPlayer(id: Long?, text: String) {
//        if (id == null) return
//
//        GlobalScope.launch {
//            Application.bot?.sendMessageToPlayer(id, text)
//        }
//    }
//
//    fun sendMessageToAll(text: String) {
//        GlobalScope.launch {
//            Application.bot?.sendMessageToAll(text)
//        }
//    }
//}