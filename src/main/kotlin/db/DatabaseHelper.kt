package db

import java.io.File
import java.io.FileReader
import java.io.FileWriter


const val DATABASE_FOLDER = ""
const val DATABASE_PLAYERS_FILE = "$DATABASE_FOLDER/players.txt"
const val DATABASE_TASKS_FILE = "$DATABASE_FOLDER/tasks.txt"
const val DATABASE_LOG_FILE = "$DATABASE_FOLDER/log.txt"
const val TASKS_FOLDER = "./tasks"

class DatabaseHelper {

    companion object {

        lateinit var players: HashMap<Long, Player>
        lateinit var tasks: HashMap<Long, Task>


        fun init() {
            players = parsePlayersFileToHashmap()
            tasks = parseTasksFileToHashmap()
            val file = File("./tasks")
            if (!file.exists()) {
                file.mkdir()
            }
        }


        private fun parsePlayersFileToHashmap(): HashMap<Long, Player> {
            val folder = File(DATABASE_FOLDER)
            val file = File(DATABASE_PLAYERS_FILE)
            if (!folder.exists()) {
                folder.mkdir()
                file.createNewFile()
                return HashMap()
            }

            if (!file.exists()) {
                file.createNewFile()
                return HashMap()
            }

            val result = HashMap<Long, Player>()
            val fileReader = FileReader(file)
            val playersList = fileReader.readLines()
            for (player in playersList) {
                val (id, username, score, tasks) = player.split(":|:")
                val taskList = arrayListOf<Long>()
                if (tasks.isNotEmpty()) {
                    for (task in tasks.split(","))
                        taskList.add(task.toLong())
                }

                result[id.toLong()] = Player(
                        id.toLong(),
                        username,
                        score.toInt(),
                        taskList
                )
            }

            return result
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


        fun getPlayerById(id: Long): Player? {
            return players[id]
        }


        fun getTaskById(id: Long): Task? {
            return tasks[id]
        }


        fun getScoreboard(): Array<Pair<String, Int>> {
            val scoreboard = ArrayList<Pair<String, Int>>()
            for (player in players.values) {
                scoreboard.add(Pair(player.userName, player.score))
            }
            return scoreboard.sortedBy { it.second }.toTypedArray()
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


        fun insertNewPlayer(player: Player) {
            players[player.userId] = player
            val file = File(DATABASE_PLAYERS_FILE)
            val fileWriter = FileWriter(file, true)
            fileWriter.append("${player.userId}:|:${player.userName}:|:${player.score}:|:${player.solvedTasks.joinToString(",")}\n")
            fileWriter.close()
        }
    }
}