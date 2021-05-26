package db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.*
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
        object NoSuchPlayer: FlagCheckResult()
        object Wrong: FlagCheckResult()
        object Exists: FlagCheckResult()
        class Success(val price: Int): FlagCheckResult()
    }

    class DbOpResult<T>(val result: T? = null, val exception: Exception? = null)

    private val database: Database by lazy {
        Database.connect("jdbc:sqlite:db/data.db", "org.sqlite.JDBC")
    }

    val competitionsController by lazy { CompetitionsController() }
    val playersController by lazy { PlayersController() }
    val tasksController by lazy { TasksController() }
    val solvesController by lazy { SolvesController() }

    fun init(): DbOpResult<Boolean> {
        try {
            if (!File(DATABASE_FOLDER).exists()) { File(DATABASE_FOLDER).mkdir() }
            transaction(db = database) {
                SchemaUtils.create(CompetitionsTable, TasksTable, SolvesTable, PlayersTable)
            }
            return DbOpResult(true)
        } catch (ex: Exception) {
            return DbOpResult(false, exception = ex)
        }
    }

    fun addCompetition(name: String): DbOpResult<CompetitionDTO> {
        try {
            val competition = transaction(db = database) {
                return@transaction CompetitionEntity.new {
                    this.name = name
                }
            }
            val dto = CompetitionDTO(competition)
//            competitionsController.add(dto)
            return DbOpResult(dto)
        } catch (ex: Exception) {
            return DbOpResult(null, ex)
        }
    }

    fun addPlayer(id: Long, userName: String): DbOpResult<Boolean> {
        try {
            transaction(db = database) {
                val player = PlayerEntity.new(id) {
                    this.userName = userName
                    this.currentScore = 0
                    this.seasonScore = 0
                }
//                playersController.add(PlayerDTO(player))
            }
            return DbOpResult(true)
        } catch (ex: Exception) {
            return DbOpResult(false, ex)
        }
    }

    fun addTask(
        category: String,
        name: String,
        description: String,
        price: Int,
        flag: String,
        attachment: String,
        competition: CompetitionDTO,
    ): DbOpResult<TaskDTO> {
        try {
            val task = transaction(db = database) {
                return@transaction TaskEntity.new() {
                    this.category = category
                    this.name = name
                    this.description = description
                    this.price = price
                    this.flag = flag
                    this.attachment = attachment
                    this.competition = competition.id
                }
            }
            val dto = TaskDTO(task)
//            tasksController.add(dto)
            return DbOpResult(dto)
        } catch (ex: Exception) {
            return DbOpResult(null, ex)
        }
    }

    fun getCompetitions(): DbOpResult<List<CompetitionDTO>> {
        try {
            return DbOpResult(transaction(db = database) {
                CompetitionEntity.all().map {
                    CompetitionDTO(it)
                }
            })
        } catch (ex: Exception) {
            return DbOpResult(exception = ex)
        }
    }

    fun onPlayerPassedFlag(competitionId: Long, playerId: Long, flag: String): DbOpResult<FlagCheckResult> {
        try {
            val result = transaction(db = database) {
                val player = PlayerEntity.findById(playerId) ?: return@transaction FlagCheckResult.NoSuchPlayer
                val result = TaskEntity.find {
                    (TasksTable.competition eq competitionId) and (TasksTable.flag eq flag)
                }

                if (result.empty()) {
                    return@transaction FlagCheckResult.Wrong
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
                    return@transaction FlagCheckResult.Success(task.price)
                } else {
                    return@transaction FlagCheckResult.Exists
                }
            }
            return DbOpResult(result)
        } catch (ex: Exception) {
            return DbOpResult(exception = ex)
        }
    }

    fun checkPlayerExists(id: Long): DbOpResult<Boolean> {
        try {
            val result = transaction(db = database) {
                return@transaction PlayersTable.select { PlayersTable.id eq id }.empty()
            }
            return DbOpResult(result)
        } catch (ex: Exception) {
            return DbOpResult(null, ex)
        }
    }

    fun getTask(id: Long): DbOpResult<TaskDTO> {
        try {
            val result = transaction(db = database) {
                TaskEntity.findById(id)
            }
            return DbOpResult(if (result == null) null else TaskDTO(result))
        } catch (ex: Exception) {
            return DbOpResult(exception = ex)
        }
    }

    fun getTasks(competition: CompetitionDTO): DbOpResult<List<TaskDTO>> {
        try {
            val result = transaction(db = database) {
                return@transaction TaskEntity.all().filter { it.competition == competition.id }.map { TaskDTO(it) }
            }
            return DbOpResult(result)
        } catch (ex: Exception) {
            return DbOpResult(exception = ex)
        }
    }

    fun getSolvedTasksForPlayer(player: PlayerDTO): DbOpResult<List<TaskDTO>> {
        try {
            val result = transaction(db = database) {
                TaskEntity.wrapRows(
                    TasksTable.innerJoin(
                        SolvesTable
                    )
                        .slice(TasksTable.columns)
                        .select {
                            (SolvesTable.task eq TasksTable.id) and (SolvesTable.player eq player.id)
                        }
                ).map {
                    TaskDTO(it)
                }
            }
            return DbOpResult(result)
        } catch (ex: Exception) {
            return DbOpResult(exception = ex)
        }
    }

    fun getPlayersSolvesForTask(task: TaskDTO): DbOpResult<List<PlayerDTO>> {
        try {
            val result = transaction(db = database) {
                PlayerEntity.wrapRows(
                    PlayersTable.innerJoin(SolvesTable)
                        .slice(PlayersTable.columns)
                        .select {
                            (SolvesTable.player eq PlayersTable.id) eq (SolvesTable.task eq task.id)
                        }
                        .withDistinct()
                ).map {
                    PlayerDTO(it)
                }
            }
            return DbOpResult(result)
        } catch (ex: Exception) {
            return DbOpResult(exception = ex)
        }
    }

    fun updateCompetition(competition: CompetitionDTO): DbOpResult<Boolean> {
        try {
            transaction(db = database) {
                competition.entity.name = competition.name
            }
            return DbOpResult(true)
        } catch (ex: Exception) {
            return DbOpResult(false, ex)
        }
    }

    fun updateTask(task: TaskDTO): DbOpResult<Boolean> {
        try {
            transaction(db = database) {
                task.entity.category = task.category
                task.entity.name = task.name
                task.entity.description = task.description
                task.entity.price = task.price
                task.entity.flag = task.flag
                task.entity.attachment = task.attachment
                task.entity.competition = task.competition
            }
            return DbOpResult(true)
        } catch (ex: Exception) {
            return DbOpResult(false, ex)
        }
    }

    fun updatePlayer(player: PlayerDTO): DbOpResult<Boolean> {
        try {
            transaction(db = database) {
                player.entity.userName = player.userName
                player.entity.currentScore = player.currentScore
                player.entity.seasonScore = player.seasonScore
            }
            return DbOpResult(true)
        } catch (ex: Exception) {
            return DbOpResult(false, ex)
        }
    }

    fun deleteCompetition(competition: CompetitionDTO): DbOpResult<Boolean> {
        try {
            transaction(db = database) {
                competition.entity.delete()
            }
            return DbOpResult(true)
        } catch (ex: Exception) {
            return DbOpResult(false, ex)
        }
    }

    fun deleteTask(task: TaskDTO): DbOpResult<Boolean> {
        try {
            transaction(db = database) {
                task.entity.delete()
            }
            return DbOpResult(true)
        } catch (ex: Exception) {
            return DbOpResult(false, ex)
        }
    }

    fun deletePlayer(player: PlayerDTO): DbOpResult<Boolean> {
        try {
            transaction(db = database) {
                player.entity.delete()
            }
            return DbOpResult(true)
        } catch (ex: Exception) {
            return DbOpResult(false, ex)
        }
    }
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