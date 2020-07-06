package db

import db.entities.PlayerEntity
import db.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.collections.HashMap


const val DATABASE_FOLDER = "./db"
const val DATABASE_PLAYERS_FILE = "$DATABASE_FOLDER/players.txt"
const val DATABASE_TASKS_FILE = "$DATABASE_FOLDER/tasks.txt"
const val DATABASE_LOG_FILE = "$DATABASE_FOLDER/log.txt"
const val TASKS_FOLDER = "./tasks"

class DatabaseHelper {

    companion object {

        private val database: Database by lazy {
            Database.connect("jdbc:sqlite:db/data.db", "org.sqlite.JDBC")
        }


        lateinit var players: HashMap<Long, Player>
        val playersFromDb: HashMap<Long, PlayerEntity> = HashMap()
        lateinit var tasks: HashMap<Long, Task>


        fun init() {
            transaction(db = database) {
                SchemaUtils.create(PlayersTable)
                SchemaUtils.create(TasksTable)
            }
//            players = parsePlayersFileToHashmap()
            tasks = parseTasksFileToHashmap()
            val file = File("./tasks")
            if (!file.exists()) {
                file.mkdir()
            }
        }

//        private fun parsePlayersFileToHashmap(): HashMap<Long, Player> {
//            val folder = File(DATABASE_FOLDER)
//            val file = File(DATABASE_PLAYERS_FILE)
//            if (!folder.exists()) {
//                folder.mkdir()
//                file.createNewFile()
//                return HashMap()
//            }
//
//            if (!file.exists()) {
//                file.createNewFile()
//                return HashMap()
//            }
//
//            val result = HashMap<Long, Player>()
//            val fileReader = FileReader(file)
//            val playersList = fileReader.readLines()
//            for (player in playersList) {
//                val (id, username, score, tasks) = player.split(":|:")
//                val taskList = arrayListOf<Long>()
//                if (tasks.isNotEmpty()) {
//                    for (task in tasks.split(","))
//                        taskList.add(task.toLong())
//                }
//
//                result[id.toLong()] = Player(
//                        id.toLong(),
//                        username,
//                        score.toInt(),
//                        taskList
//                )
//            }
//
//            return result
//        }


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
            val d = Date()
            val startTime = d.time
            transaction(db = database) {
                PlayerEntity.new(id) {
                    this.userName = userName
                    this.score = 0
                    this.solvedTasks = ""
                }
            }
            val stopTime = d.time
            print(startTime - stopTime)
        }


        fun getPlayerById(id: Long): PlayerEntity? {
            return transaction(db = database) { PlayerEntity.findById(id) }
        }

        fun getTaskById(id: Long): Task? {
            return tasks[id]
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
                PlayerEntity.all().sortedBy { it.score }.map { Pair(it.userName, it.score) }
            }
        }


        fun getTaskFiles(id: Long): Array<File> {
            val task = getTaskById(id)
            val directory = File("$TASKS_FOLDER/${task?.name}")
            if (!directory.exists()) {
                return emptyArray()
            }

            val files = directory.listFiles()
            return files ?: emptyArray()
        }


        fun updatePlayersDatabase() {
            val fileWriter = FileWriter(DATABASE_PLAYERS_FILE)
            for (player in players.values) {
                fileWriter.write("${player.userId}:|:${player.userName}:|:${player.score}:|:${player.solvedTasks.joinToString(",")}\n")
            }
            fileWriter.close()
        }
    }
}