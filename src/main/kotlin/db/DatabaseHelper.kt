package db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*

const val DATABASE_FOLDER = "./db"
const val DATABASE_LOG_FILE = "$DATABASE_FOLDER/log.txt"

// Helper class to perform actions with database. All calls are blocking due to usage of database transactions.
//   If one needs to make non-blocking calls, it should implement asynchronous calls by itself.
object DatabaseHelper {

    const val FLAG_RESULT_SUCCESS = 0
    const val FLAG_RESULT_ALREADY_SOLVED = 1
    const val FLAG_RESULT_WRONG = 2
    const val FLAG_RESULT_ERROR = 8

    sealed class FlagCheckResult {
        object Wrong: FlagCheckResult()
        object Exists: FlagCheckResult()
        class Success(price: Int): FlagCheckResult()
    }

    private val database: Database by lazy {
        Database.connect("jdbc:sqlite:db/data.db", "org.sqlite.JDBC")
    }

    val competitionsController = CompetitionsController()
    val playersController = PlayersController()
    val tasksController = TasksController()
    val solvesController = SolvesController()

    fun <T> perform(transaction: () -> T): T {
        return transaction(db = database) { transaction() }
    }

    fun init() {
        if (!File(DATABASE_FOLDER).exists()) { File(DATABASE_FOLDER).mkdir() }
        perform {
            SchemaUtils.create(PlayersTable)
            SchemaUtils.create(TasksTable)
        }
    }

    fun addCompetition(name: String) {
        perform {
            val newCompetition = CompetitionEntity.new {
                this.name = name
            }
            competitionsController.add(CompetitionDTO(newCompetition))
        }
    }

    fun addPlayer(id: Long, userName: String) {
        perform {
            val player = PlayerEntity.new(id) {
                this.userName = userName
                this.currentScore = 0
                this.seasonScore = 0
            }
            playersController.add(PlayerDTO(player))
        }
    }

    fun addTask(
        category: String,
        name: String,
        description: String,
        price: Int,
        flag: String,
        attachment: String,
        competition: CompetitionEntity
    ) {
        perform {
            val newTask = TaskEntity.new() {
                this.category = category
                this.name = name
                this.description = description
                this.price = price
                this.flag = flag
                this.attachment = attachment
                this.competition = competition.id
            }

            tasksController.add(TaskDTO(newTask))
        }
    }

    fun onPlayerPassedFlag(
        competitionId: Long,
        playerId: Long,
        flag: String,
        onResult: (FlagCheckResult) -> Unit
    ) {
        perform {
            val player = PlayerEntity.findById(playerId) ?: return@perform
            val result = TaskEntity.find {
                (TasksTable.competition eq competitionId) and (TasksTable.flag eq flag)
            }

            if (result.empty()) {
                onResult(FlagCheckResult.Wrong)
                return@perform
            }

            val task = result.first()
            val solves = SolveEntity.find {
                (SolvesTable.player eq playerId) and (SolvesTable.task eq task.id)
            }
            if (solves.empty()) {
                SolveEntity.new {
                    this.task = task.id
                    this.player = player.id
                    this.timestamp = Date().time
                }
                onResult(FlagCheckResult.Success(task.price))
            } else {
                onResult(FlagCheckResult.Exists)
            }
        }
    }

//    fun onPlayerPassedFlag(player: PlayerDTO, flag: String): Pair<Int, Int> {
//        val player = getPlayerById(playerId) ?: return Pair(FLAG_RESULT_ERROR, 0)
//
//        for (task in getTasksForCtf(MessageMaker.ctfName)) {
//            if (flag == task.flag) {
//                transaction(db = database) {
//                    SolveEntity.new {
//                        player =
//                    }
//                }
//                val solvedTasks = player.solvedTasks.split("|")
//                return if (solvedTasks.contains(task.id.toString())) {
//                    Pair(FLAG_RESULT_ALREADY_SOLVED, 0)
//                } else {
//                    transaction {
//                        player.currentScore += task.price
//                        player.seasonScore += task.price
//                        player.solvedTasks += "${task.id}|"
//                        player.lastRightAnswer = Date().time
//
//                        playersController.update()
//                    }
//                    Pair(FLAG_RESULT_SUCCESS, task.price)
//                }
//            }
//        }
//
//        return Pair(FLAG_RESULT_WRONG, 0)
//    }
//
//    fun getPlayerById(id: Long): PlayerEntity? {
//        return transaction(db = database) { PlayerEntity.findById(id) }
//    }
//
//    fun checkPlayerInDatabase(id: Long): Boolean {
//        return transaction(db = database) { PlayerEntity.findById(id) != null }
//    }
//
//    fun getSolvesForPlayer(player: PlayerEntity): List<SolveDTO> {
//        return transaction(db = database) {
//            SolveEntity.all().filter { it.player == player.id }.map { SolveDTO(it) }
//        }
//    }
////    fun getSolvedTasksForPlayer(id: Long): List<Long> {
////        return transaction(db = database) {
////            PlayerEntity.findById(id)
////                ?.solvedTasks
////                ?.split("|")
////                ?.filter { it.isNotEmpty() }
////                ?.map { it.toLong() }
////                ?.toList()
////                ?: emptyList()
////        }
////    }
//
//    fun deletePlayer(model: PlayerModel?) {
//        model?.item?.delete()
//        playersController.playersList.remove(model?.item)
//    }
//
//    fun deleteAllPlayers() {
//        transaction(db = database) {
//            PlayerEntity.all().forEach { it.delete() }
//        }
//        playersController.playersList.removeAll(playersController.playersList)
//    }
//
//    fun addNewTask(
//        category: String,
//        name: String,
//        description: String,
//        price: Int,
//        flag: String,
//        attachment: String,
//        competition: CompetitionEntity
//    ) {
//        transaction(db = database) {
//            val task = TaskEntity.new {
//                this.category = category
//                this.name = name
//                this.description = description
//                this.price = price
//                this.flag = flag
//                this.attachment = attachment
//                this.competition = competition.id
//            }
//            tasksController.tasksList.add(TaskDTO(task))
//        }
//    }
//
//    fun getTaskById(id: Long): TaskEntity? {
//        return transaction(db = database) { TaskEntity.findById(id) }
//    }
//
//    fun getTasksForCtf(name: String): List<TaskEntity> {
//        return transaction (db = database) {
//            TaskEntity.find { TasksTable.name eq name }.toList()
//        }
//    }
//
//    fun deleteTask(model: TaskModel?) {
//        model?.item?.delete()
//        tasksController.tasksList.remove(model?.item)
//    }
//
//    fun getAllTasks(): List<TaskEntity> {
//        return transaction (db = database) { TaskEntity.all().toList() }
//    }
//
//
//    fun getScoreboard(): List<Triple<String, Int, Int>> {
//        return transaction(db = database) {
//            PlayerEntity.all().sortedByDescending { it.currentScore }.map { Triple(it.userName, it.currentScore, it.seasonScore) }
//        }
//    }
//
//    fun getAllPlayers(): List<PlayerEntity> {
//        return transaction(db = database) { PlayerEntity.all().toList() }
//    }
//
//
//    fun getTaskFiles(id: Long): File? {
//        val task = getTaskById(id) ?: return null
//        val file = File(task.attachment)
//        if (!file.exists()) return null
//        return file
//    }
//
//    // Sets current users' scores to 0, list of solved tasks and season score remains
//    fun refreshCurrentScores() {
//        transaction {
//            val players = PlayerEntity.all()
//            players.map {
//                it.currentScore = 0
//            }
//            playersController.update(players.toList())
//        }
//    }
//
//    // Renews users list, doesn't delete users from database but sets their fields to default values
//    fun refreshAllScores() {
//        transaction {
//            val players = PlayerEntity.all()
//            players.map {
//                it.currentScore = 0
//                it.seasonScore = 0
//            }
//            playersController.update(players.toList())
//        }
//    }
}