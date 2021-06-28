package database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import utils.Logger
import java.io.File
import java.util.*


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

    suspend fun init(): Boolean {
        try {
            if (!File(DATABASE_FOLDER).exists()) File(DATABASE_FOLDER).mkdir()

            transactionOn(database) {
                SchemaUtils.create(CompetitionsTable, TasksTable, PlayersTable, SolvesTable, ScoresTable)
            }
            return true
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to initialize database: ${ex.message}\n${ex.stackTraceToString()}")
            return false
        }
    }

    suspend fun <T> transactionOn(database: Database, action: () -> T): T {
        return withContext(Dispatchers.IO) { transaction(db = database) { action() } }
    }

    fun getNewTaskPrice(solvesCount: Int): Int {
        val x = maxOf(0, solvesCount)
        return (MIN_POINTS - INITIAL_TASK_PRICE) / (DECAY * DECAY) * (x * x) + INITIAL_TASK_PRICE
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

    suspend fun getAllScores(): List<ScoreDTO> {
        try {
            return transactionOn(database) { ScoreEntity.all().map { ScoreDTO(it) }}
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to retrieve all scores: ${ex.message}\n${ex.stackTraceToString()}")
            return emptyList()
        }
    }

    suspend fun getScoreboard(): List<PlayerDTO> {
        try {
            return transactionOn(database) {
                PlayerEntity.all().map { PlayerDTO(it) }.sortedBy { it.getTotalScoreSynchronous() }
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

    suspend fun getPlayer(id: Long): PlayerDTO? {
        try {
            return transactionOn(database) { PlayerEntity.findById(id)?.let { PlayerDTO(it) } }
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to get player by id: ${ex.message}\n${ex.stackTraceToString()}")
            return null
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

    suspend fun update(score: ScoreDTO) {
        try {
            transactionOn(database) { score.entity.score = score.score }
            mEventsPipe.emit(DbEvent.Update(score))
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to update score: ${ex.message}\n${ex.stackTraceToString()}")
        }
    }

    suspend fun onFlagPassed(competition: CompetitionDTO, playerId: Long, flag: String): FlagCheckResult {
        flagCheckMutex.withLock {
            try {
                val player = transactionOn(database) { PlayerEntity.findById(playerId)?.let { PlayerDTO(it) }}
                    ?: return FlagCheckResult.NoSuchPlayer

                val task = competition.getTasks().find { it.flag == flag } ?: return FlagCheckResult.WrongFlag

                // Adding new solution must be synchronized in order to protect of adding solution twice
                val solvedPlayers = task.getSolvedPlayers()
                if (solvedPlayers.any { it.id == player.id }) {
                    return FlagCheckResult.SolveExists
                }

                val dto = transactionOn(database) {
                    SolveDTO(SolveEntity.new {
                        this.player = player.entity
                        this.task = task.entity
                        this.timestamp = Date().time
                    })
                }


                mEventsPipe.emit(DbEvent.Add(dto))

                val previousPrice = getNewTaskPrice(solvedPlayers.count() - 1)
                val currentPrice = getNewTaskPrice(solvedPlayers.count())

                // Updating other players' scores
                for (p in solvedPlayers) {
                    val score = p.getCompetitionScore(competition) ?: continue
                    score.score = score.score - previousPrice + currentPrice
                    score.updateEntity()
                }

                // Add
                val playerSore = player.getCompetitionScore(competition)
                if (playerSore == null) {
                    val scoreDto = transactionOn(database) {
                        ScoreDTO(ScoreEntity.new {
                            this.competition = competition.entity
                            this.player = player.entity
                            this.score = currentPrice
                        })
                    }
                    mEventsPipe.emit(DbEvent.Add(scoreDto))
                } else {
                    playerSore.score += currentPrice
                    playerSore.updateEntity()
                    mEventsPipe.emit(DbEvent.Update(playerSore))
                }
                return FlagCheckResult.CorrectFlag(currentPrice)
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
            return true
        } catch (ex: Exception) {
            Logger.error(tag, "Failed to delete player: ${ex.message}\n${ex.stackTraceToString()}")
            return false
        }
    }
}