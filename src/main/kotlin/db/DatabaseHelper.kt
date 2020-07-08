package db

import db.entities.PlayerEntity
import db.entities.TaskEntity
import db.models.PlayerModel
import db.models.TaskModel
import db.tables.*
import javafx.collections.ObservableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import tornadofx.Controller
import tornadofx.TableColumnDirtyState
import tornadofx.asObservable
import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.collections.HashMap


const val DATABASE_FOLDER = "./db"
const val DATABASE_PLAYERS_FILE = "$DATABASE_FOLDER/players.txt"
const val DATABASE_TASKS_FILE = "$DATABASE_FOLDER/tasks.txt"
const val DATABASE_LOG_FILE = "$DATABASE_FOLDER/log.txt"
const val TASKS_FOLDER = "./tasks"

class DatabaseHelper {

    companion object {

        val database: Database by lazy {
            Database.connect("jdbc:sqlite:db/data.db", "org.sqlite.JDBC")
        }

        val playersController = PlayersController()
        val tasksController = TasksController()

        lateinit var tasks: HashMap<Long, Task>

        fun init() {
            GlobalScope.launch(Dispatchers.IO) {
                transaction(db = database) {
                    SchemaUtils.create(PlayersTable)
                    SchemaUtils.create(TasksTable)
                }
            }
            tasks = parseTasksFileToHashmap()
            val file = File("./tasks")
            if (!file.exists()) {
                file.mkdir()
            }
        }

        private fun parseTasksFileToHashmap(): HashMap<Long, Task> {
            val folder = File(DATABASE_FOLDER)
            val file = File(DATABASE_TASKS_FILE)
            if (!folder.exists()) {
                folder.mkdir()
                file.createNewFile()
                return HashMap()
            }

            if (!file.exists()) {
                file.createNewFile()
                return HashMap()
            }

            val result = HashMap<Long, Task>()
            val fileReader = FileReader(file)
            val tasksList = fileReader.readLines()
            for (task in tasksList) {
                if (task.isEmpty()) continue
                val (id, category, name, cost, flag) = task.split(":|:")
                result[id.toLong()] = Task(id.toLong(), category, name, cost.toInt(), flag)
            }
            fileReader.close()
            return result
        }

        fun addNewPlayer(id: Long, userName: String) {
            GlobalScope.launch(Dispatchers.IO) {
                transaction(db = database) {
                    val player = PlayerEntity.new(id) {
                        this.userName = userName
                        this.currentScore = 0
                        this.seasonScore = 0
                        this.solvedTasks = ""
                    }
                    playersController.add(PlayerModel().apply { item = player })
                }
            }
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
            GlobalScope.launch(Dispatchers.IO) {
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
        }


        fun getPlayerById(id: Long): PlayerEntity? {
            return transaction(db = database) { PlayerEntity.findById(id) }
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


        fun getScoreboard(): List<Pair<String, Int>> {
            return transaction(db = database) {
                PlayerEntity.all().sortedBy { it.currentScore }.map { Pair(it.userName, it.currentScore) }
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

        fun refreshCurrentScores() {
            GlobalScope.launch(Dispatchers.IO) {
                transaction {
                    val players = PlayerEntity.all()
                    players.map {
                        it.currentScore = 0
                    }
                    playersController.update(players.toList())
                }
            }
        }

        fun refreshAllScores() {
            GlobalScope.launch(Dispatchers.IO) {
                transaction {
                    val players = PlayerEntity.all()
                    players.map {
                        it.currentScore = 0
                        it.seasonScore = 0
                        it.solvedTasks = ""
                    }
                    playersController.update(players.toList())
                }
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