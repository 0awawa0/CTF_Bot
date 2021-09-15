package database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import utils.Logger
import java.io.File
import java.util.*
import java.util.Collections.emptyList
import java.util.concurrent.ConcurrentHashMap


object DbHelper {

    const val DATABASE_FOLDER = "./db"

    const val INITIAL_TASK_PRICE = 1000
    const val MIN_POINTS = 100
    const val DECAY = 10

    sealed class DbEvent(val dto: BaseDTO) {
        class Add(dto: BaseDTO): DbEvent(dto)
        class Update(dto: BaseDTO): DbEvent(dto)
        class Delete(dto: BaseDTO): DbEvent(dto)
    }

    sealed class FlagCheckResult {
        object NoSuchPlayer: FlagCheckResult()
        object WrongFlag: FlagCheckResult()
        object SolveExists: FlagCheckResult()
        class CorrectFlag(val price: Int): FlagCheckResult()
    }

    val database: Database by lazy {
        Database.connect("jdbc:sqlite:db/data.db?foreign_keys=on", "org.sqlite.JDBC")
    }

    private val tag = "DbHelper"

    private val flagCheckMutex = Mutex()
    private val mEventsPipe = MutableSharedFlow<DbEvent>()
    val eventsPipe = mEventsPipe.asSharedFlow()

    private val taskPrices = ConcurrentHashMap<Long, Int>()
    private val solvedTasksPrices = ConcurrentHashMap<Long, Int>()

    suspend fun init(): Boolean {
        try {
            if (!File(DATABASE_FOLDER).exists()) File(DATABASE_FOLDER).mkdir()

            transactionOn(database) {
                SchemaUtils.create(CompetitionsTable, TasksTable, PlayersTable, SolvesTable)
            }
            return true
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to initialize database: ${ex.message}\n${ex.stackTraceToString()}")
            return false
        }
    }

    suspend fun <T> transactionOn(database: Database, action: () -> T): T {
        return withContext(Dispatchers.IO) {
            val start = System.nanoTime()
            val result = transaction(db = database) { action() }
            val end = System.nanoTime()
            Logger.debug(tag, "Transaction finished in ${(end - start) / 1000000} ms")
            return@withContext result
        }
    }

    fun getNewTaskPrice(solvesCount: Int): Int {
        val x = maxOf(0, solvesCount)
        return maxOf(MIN_POINTS, (MIN_POINTS - INITIAL_TASK_PRICE) / (DECAY * DECAY) * (x * x) + INITIAL_TASK_PRICE)
    }

    suspend fun getAllCompetitions(): List<CompetitionDTO> {
        try {
            return transactionOn(database) { CompetitionEntity.all().map { CompetitionDTO(it) }}
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to retrieve all competitions: ${ex.message}\n${ex.stackTraceToString()}")
            return emptyList()
        }
    }

    suspend fun getAllTasks(): List<TaskDTO> {
        try {
            return transactionOn(database) { TaskEntity.all().map { TaskDTO(it) }}
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to retrieve all tasks: ${ex.message}\n${ex.stackTraceToString()}")
            return emptyList()
        }
    }

    suspend fun getAllPlayers(): List<PlayerDTO> {
        try {
            return transactionOn(database) { PlayerEntity.all().map { PlayerDTO(it) }}
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to retrieve all players: ${ex.message}\n${ex.stackTraceToString()}")
            return emptyList()
        }
    }

    suspend fun getAllSolves(): List<SolveDTO> {
        try {
            return transactionOn(database) { SolveEntity.all().map { SolveDTO(it) }}
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to retrieve all solves: ${ex.message}\n${ex.stackTraceToString()}")
            return emptyList()
        }
    }

    suspend fun getScoreboard(competition: CompetitionDTO): List<Pair<String, Int>> {
        try {
            return transactionOn(database) {
                val result = LinkedList<Pair<String, Int>>()
                val players = PlayerEntity.all()
                val tasks = competition.entity.tasks.map { it.id.value }
                for (player in players) {
                    var playerScore = 0
                    val solves = player.solves
                    for (solve in solves) {
                        val taskId = solve.task.id.value
                        if (taskId !in tasks) continue
                        val price = solvedTasksPrices[taskId]
                        if (price == null)
                            solvedTasksPrices[taskId] = getNewTaskPrice(solve.task.solves.count().toInt() - 1)
                        playerScore += solvedTasksPrices[taskId] ?: 0
                    }
                    result.add(Pair(player.name, playerScore))
                }

                return@transactionOn result.sortedByDescending { it.second }
            }
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to retrieve all scores: ${ex.message}\n${ex.stackTraceToString()}")
            return emptyList()
        }
    }

    suspend fun getScoreboard(): List<Pair<String, Int>> {
        try {
            return transactionOn(database) {
                val result = LinkedList<Pair<String, Int>>()
                val players = PlayerEntity.all()
                for (player in players) {
                    var playerScore = 0
                    val solves = player.solves
                    for (solve in solves) {
                        val taskId = solve.task.id.value
                        val price = solvedTasksPrices[taskId]
                        if (price == null)
                            solvedTasksPrices[taskId] = getNewTaskPrice(solve.task.solves.count().toInt() - 1)
                        playerScore += solvedTasksPrices[taskId] ?: 0
                    }
                    result.add(Pair(player.name, playerScore))
                }

                return@transactionOn result.sortedByDescending { it.second }
            }
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to retrieve all scores: ${ex.message}\n${ex.stackTraceToString()}")
            return emptyList()
        }
    }

    suspend fun getCompetition(id: Long): CompetitionDTO? {
        try {
            return transactionOn(database) { CompetitionEntity.findById(id)?.let { CompetitionDTO(it) } }
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to get competition by id: ${ex.message}\n${ex.stackTraceToString()}")
            return null
        }
    }

    suspend fun getTask(id: Long): TaskDTO? {
        try {
            return transactionOn(database) { TaskEntity.findById(id)?.let { TaskDTO(it) } }
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to get task by id: ${ex.message}\n${ex.stackTraceToString()}")
            return null
        }
    }

    suspend fun getTaskPrice(task: TaskDTO): Int {
        val price = taskPrices[task.id]
        if (price == null) taskPrices[task.id] = getNewTaskPrice(task.getSolves().count())
        return taskPrices[task.id] ?: 0
    }

    suspend fun getSolvedTaskPrice(task: TaskDTO): Int {
        val price = solvedTasksPrices[task.id]
        if (price == null) solvedTasksPrices[task.id] = getNewTaskPrice(task.getSolves().count() - 1)
        return solvedTasksPrices[task.id] ?: 0
    }

    suspend fun getPlayer(id: Long): PlayerDTO? {
        try {
            return transactionOn(database) { PlayerEntity.findById(id)?.let { PlayerDTO(it) } }
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to get player by id: ${ex.message}\n${ex.stackTraceToString()}")
            return null
        }
    }

    suspend fun getCompetitionAndTotalScores(player: PlayerDTO, competition: CompetitionDTO? = null): Pair<Int, Int> {
        try {
            var competitionScore = 0
            var totalScore = 0
            val tasks = transactionOn(database) {
                player.entity.solves.map { Pair(it.task.competition.id.value, TaskDTO(it.task)) }
            }

            for (task in tasks) {
                val price = getSolvedTaskPrice(task.second)
                if (competition != null && task.first == competition.id) competitionScore += price
                totalScore += price
            }

            return Pair(competitionScore, totalScore)
        } catch (ex: Exception) {
            Logger.error(
                tag,
                "Failed to get competition and global score for player: ${ex.message}\n${ex.stackTraceToString()}"
            )
            return Pair(0, 0)
        }
    }

    suspend fun getTasksList(player: PlayerDTO, competition: CompetitionDTO): List<Triple<TaskDTO, Int, Boolean>> {
        try {
            val tasks = transactionOn(database) {
                val playerSolves = player.entity.solves.map { it.task.id.value }.toHashSet()

                val result = competition.entity.tasks.map {
                    Pair(TaskDTO(it), it.id.value in playerSolves)
                }
                return@transactionOn result
            }

            return tasks.map { Triple(it.first, getTaskPrice(it.first), it.second) }
        } catch (ex: Exception) {
            Logger.error(
                tag,
                "Failed to get tasks list for player: ${ex.message}\n${ex.stackTraceToString()}"
            )
            return emptyList()
        }
    }

    suspend fun getSolves(player: PlayerDTO, competition: CompetitionDTO? = null): List<Pair<TaskDTO, Long>> {
        try {
            return transactionOn(database) {
                val result = player.entity.solves
                return@transactionOn result.filter {
                    competition == null || it.task.competition.id == competition.entity.id
                }.map {
                    Pair(TaskDTO(it.task), it.timestamp)
                }
            }
        } catch (ex: Exception) {
            Logger.error(
                tag,
                "Failed to retrieve player solves for competition: ${ex.message}\n${ex.stackTraceToString()}"
            )
            return emptyList()
        }
    }

    suspend fun add(competitionModel: CompetitionModel): CompetitionDTO? {
        try {
            val dto = transactionOn(database) {
                val competition = CompetitionEntity.new { this.name = competitionModel.name }
                for (task in competitionModel.tasks) {
                    TaskEntity.new {
                        this.category = task.category
                        this.name = task.name
                        this.description = task.description
                        this.flag = task.flag
                        this.attachment = task.attachment
                        this.competition = competition
                    }
                }
                return@transactionOn CompetitionDTO(competition)
            }
            mEventsPipe.emit(DbEvent.Add(dto))
            return dto
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to add competition: ${ex.message}\n${ex.stackTraceToString()}")
            return null
        }
    }

    suspend fun add(competitionDTO: CompetitionDTO, taskModel: TaskModel): TaskDTO? {
        try {
            val dto = transactionOn(database) {
                val task = TaskEntity.new {
                    this.category = taskModel.category
                    this.name = taskModel.name
                    this.description = taskModel.description
                    this.flag = taskModel.flag
                    this.attachment = taskModel.attachment
                    this.competition = competitionDTO.entity
                }
                return@transactionOn TaskDTO(task)
            }
            mEventsPipe.emit(DbEvent.Add(dto))
            return dto
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to add task: ${ex.message}\n${ex.stackTraceToString()}")
            return null
        }
    }

    suspend fun add(playerModel: PlayerModel): PlayerDTO? {
        try {
            val dto = transactionOn(database) {
                val player = PlayerEntity.new(playerModel.id) {
                    this.name = playerModel.name
                }

                return@transactionOn PlayerDTO(player)
            }
            mEventsPipe.emit(DbEvent.Add(dto))
            return dto
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to add new player: ${ex.message}\n${ex.stackTraceToString()}")
            return null
        }
    }

    suspend fun update(competition: CompetitionDTO) {
        try {
            transactionOn(database) { competition.entity.name = competition.name }
            mEventsPipe.emit(DbEvent.Update(competition))
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to update competition: ${ex.message}\n${ex.stackTraceToString()}")
        }
    }

    suspend fun update(player: PlayerDTO) {
        try {
            transactionOn(database) { player.entity.name = player.name }
            mEventsPipe.emit(DbEvent.Update(player))
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to update player: ${ex.message}\n${ex.stackTraceToString()}")
        }
    }

    suspend fun update(task: TaskDTO) {
        try {
            transactionOn(database) {
                task.entity.category = task.category
                task.entity.name = task.name
                task.entity.description = task.description
                task.entity.flag = task.flag
                task.entity.attachment = task.attachment
            }
            mEventsPipe.emit(DbEvent.Update(task))
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to update task: ${ex.message}\n${ex.stackTraceToString()}")
        }
    }

    suspend fun onFlagPassed(competition: CompetitionDTO, playerId: Long, flag: String): FlagCheckResult {
        flagCheckMutex.withLock {
            try {
                val (player, task, solved) = transactionOn(database) {
                    val player = PlayerEntity.findById(playerId)
                    val task = competition.entity.tasks.firstOrNull { it.flag == flag }
                    val solved = player?.solves?.any { it.task == task } ?: false
                    return@transactionOn if (player != null && task != null)
                        Triple(PlayerDTO(player), TaskDTO(task), solved)
                    else
                        Triple(null, null, false)
                }

                if (player == null) return FlagCheckResult.NoSuchPlayer
                if (task == null) return FlagCheckResult.WrongFlag
                if (solved) return FlagCheckResult.SolveExists

                val dto = transactionOn(database) {
                    SolveDTO(SolveEntity.new {
                        this.player = player.entity
                        this.task = task.entity
                        this.timestamp = Date().time
                    })
                }

                val solves = task.getSolves().count()
                solvedTasksPrices[task.id] = getNewTaskPrice(solves - 1)
                taskPrices[task.id] = getNewTaskPrice(solves)

                mEventsPipe.emit(DbEvent.Add(dto))
                return FlagCheckResult.CorrectFlag(solvedTasksPrices[task.id] ?: 0)
            } catch (ex: Exception) {
                Logger.error(tag, "Failed to check player's flag: ${ex.message}\n${ex.stackTraceToString()}")
                return FlagCheckResult.NoSuchPlayer
            }
        }
    }

    suspend fun checkPlayerExists(id: Long): Boolean {
        try {
            return transactionOn(database) { PlayerEntity.findById(id) != null }
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to check user: ${ex.message}\n${ex.stackTraceToString()}")
            return false
        }
    }

    suspend fun delete(competitionDTO: CompetitionDTO): Boolean {
        try {
            transactionOn(database) { CompetitionsTable.deleteWhere { CompetitionsTable.id eq competitionDTO.id } }
            mEventsPipe.emit(DbEvent.Delete(competitionDTO))
            clearCache()
            return true
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to delete competition: ${ex.message}\n${ex.stackTraceToString()}")
            return false
        }
    }

    suspend fun delete(taskDTO: TaskDTO): Boolean {
        try {
            transactionOn(database) { TasksTable.deleteWhere { TasksTable.id eq taskDTO.id } }
            mEventsPipe.emit(DbEvent.Delete(taskDTO))
            clearCache()
            return true
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to delete task: ${ex.message}\n${ex.stackTraceToString()}")
            return false
        }
    }

    suspend fun delete(playerDTO: PlayerDTO): Boolean {
        try {
            transactionOn(database) { PlayersTable.deleteWhere { PlayersTable.id eq playerDTO.id } }
            mEventsPipe.emit(DbEvent.Delete(playerDTO))
            clearCache()
            return true
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to delete player: ${ex.message}\n${ex.stackTraceToString()}")
            return false
        }
    }

    private fun clearCache() {
        taskPrices.clear()
        solvedTasksPrices.clear()
    }
}