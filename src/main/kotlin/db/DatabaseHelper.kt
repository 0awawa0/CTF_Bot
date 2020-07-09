package db

import bot.MessageMaker
import db.entities.PlayerEntity
import db.entities.TaskEntity
import db.models.PlayerModel
import db.models.TaskModel
import db.tables.*
import javafx.collections.ObservableList
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import tornadofx.Controller
import tornadofx.TableColumnDirtyState
import tornadofx.asObservable
import java.io.File
import java.util.*


const val DATABASE_FOLDER = "./db"
const val DATABASE_LOG_FILE = "$DATABASE_FOLDER/log.txt"

// Helper class to perform actions with database. All calls are blocking due to usage of database transactions.
//   If one needs to make non-blocking calls, it should implement asynchronous calls by itself.
class DatabaseHelper {

    companion object {

        const val FLAG_RESULT_SUCCESS = 0
        const val FLAG_RESULT_ALREADY_SOLVED = 1
        const val FLAG_RESULT_WRONG = 2
        const val FLAG_RESULT_ERROR = 8

        val database: Database by lazy {
            Database.connect("jdbc:sqlite:db/data.db", "org.sqlite.JDBC")
        }

        val playersController = PlayersController()
        val tasksController = TasksController()

        fun init() {
            transaction(db = database) {
                SchemaUtils.create(PlayersTable)
                SchemaUtils.create(TasksTable)
            }
        }

        fun addNewPlayer(id: Long, userName: String) {
            transaction(db = database) {
                val player = PlayerEntity.new(id) {
                    this.userName = userName
                    this.currentScore = 0
                    this.seasonScore = 0
                    this.solvedTasks = ""
                    this.lastRightAnswer = 0
                }
                playersController.add(PlayerModel().apply { item = player })
            }
        }


        fun onPlayerPassedFlag(playerId: Long, flag: String): Int {
            val player = getPlayerById(playerId) ?: return FLAG_RESULT_ERROR

            for (task in getTasksForCtf(MessageMaker.ctfName)) {
                if (flag == task.flag) {
                    val solvedTasks = player.solvedTasks.split("|")
                    return if (solvedTasks.contains(task.id.toString())) {
                        FLAG_RESULT_ALREADY_SOLVED
                    } else {
                        transaction {
                            player.currentScore += task.price
                            player.seasonScore += task.price
                            player.solvedTasks += "${task.id}|"
                            player.lastRightAnswer = Date().time

                            playersController.update()
                        }
                        FLAG_RESULT_SUCCESS
                    }
                }
            }

            return FLAG_RESULT_WRONG
        }

        fun getPlayerById(id: Long): PlayerEntity? {
            return transaction(db = database) { PlayerEntity.findById(id) }
        }

        fun checkPlayerInDatabase(id: Long): Boolean {
            return transaction(db = database) { PlayerEntity.findById(id) != null }
        }

        fun getSolvedTasksForPlayer(id: Long): List<Long> {
            return transaction(db = database) {
                PlayerEntity.findById(id)
                    ?.solvedTasks
                    ?.split("|")
                    ?.filter { it.isNotEmpty() }
                    ?.map { it.toLong() }
                    ?.toList()
                    ?: emptyList()
            }
        }

        fun deletePlayer(model: PlayerModel?) {
            transaction {
                model?.item?.delete()
            }
            playersController.playersList.remove(model)
        }

        fun deleteAllPlayers() {
            transaction(db = database) {
                PlayerEntity.all().forEach { it.delete() }
            }
            playersController.playersList.removeAll(playersController.playersList)
        }

        fun addNewTask(
            category: String,
            name: String,
            description: String,
            price: Int,
            flag: String,
            filesDirectory: String,
            ctfName: String
        ) {
            transaction(db = database) {
                val task = TaskEntity.new {
                    this.category = category
                    this.name = name
                    this.description = description
                    this.price = price
                    this.flag = flag
                    this.filesDirectory = filesDirectory
                    this.ctfName = ctfName
                }
                tasksController.tasksList.add(TaskModel().apply { item = task })
            }
        }

        fun getTaskById(id: Long): TaskEntity? {
            return transaction(db = database) { TaskEntity.findById(id) }
        }

        fun getTasksForCtf(ctfName: String): List<TaskEntity> {
            return transaction (db = database) {
                TaskEntity.find { TasksTable.ctfName eq ctfName }.toList()
            }
        }

        fun deleteTask(model: TaskModel?) {
            transaction {
                model?.item?.delete()
            }
            tasksController.tasksList.remove(model)
        }

        fun getAllTasks(): List<TaskEntity> {
            return transaction (db = database) { TaskEntity.all().toList() }
        }


        fun getScoreboard(): List<Triple<String, Int, Int>> {
            return transaction(db = database) {
                PlayerEntity.all().sortedByDescending { it.currentScore }.map { Triple(it.userName, it.currentScore, it.seasonScore) }
            }
        }

        fun getAllPlayers(): List<PlayerEntity> {
            return transaction(db = database) { PlayerEntity.all().toList() }
        }


        fun getTaskFiles(id: Long): Array<File> {
            val task = getTaskById(id) ?: return emptyArray()
            val directory = File(task.filesDirectory)
            if (!directory.exists()) {
                return emptyArray()
            }

            val files = directory.listFiles()
            return files ?: emptyArray()
        }

        // Sets current users' scores to 0, list of solved tasks and season score remains
        fun refreshCurrentScores() {
            transaction {
                val players = PlayerEntity.all()
                players.map {
                    it.currentScore = 0
                }
                playersController.update(players.toList())
            }
        }

        // Renews users list, doesn't delete users from database but sets their fields to default values
        fun refreshAllScores() {
            transaction {
                val players = PlayerEntity.all()
                players.map {
                    it.currentScore = 0
                    it.seasonScore = 0
                    it.solvedTasks = ""
                    it.lastRightAnswer = 0
                }
                playersController.update(players.toList())
            }
        }
    }

    class PlayersController: Controller() {
        val playersList: ObservableList<PlayerModel> by lazy {
            transaction(db = database) {
                PlayerEntity.all().map {
                    PlayerModel().apply {
                        item = it
                    }
                }
            }.asObservable()
        }

        fun add(player: PlayerModel) { playersList.add(player) }
        fun update() {
            playersList.setAll(transaction {
                PlayerEntity.all().map {
                    PlayerModel().apply { item = it }
                }
            })
        }
        fun update(players: List<PlayerEntity>) {
            playersList.setAll(players.map { PlayerModel().apply { item = it }})
        }

        fun commitChanges(changes: Sequence<Map.Entry<PlayerModel, TableColumnDirtyState<PlayerModel>>>) {
            transaction {
                changes.filter { it.value.isDirty }.forEach {
                    it.key.commit()
                    it.value.commit()
                }
            }
        }
    }

    class TasksController: Controller() {
        val tasksList: ObservableList<TaskModel> by lazy {
            transaction(db = database) {
                TaskEntity.all().map {
                    TaskModel().apply {
                        item = it
                    }
                }.asObservable()
            }
        }

        fun commitChanges(changes: Sequence<Map.Entry<TaskModel, TableColumnDirtyState<TaskModel>>>) {
            transaction {
                changes.filter { it.value.isDirty }.forEach {
                    it.key.commit()
                    it.value.commit()
                }
            }
        }
    }
}