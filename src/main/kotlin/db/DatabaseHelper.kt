package db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*

const val DATABASE_FOLDER = "./db"
const val DATABASE_LOG_FILE = "$DATABASE_FOLDER/log.txt"

// Helper class to perform actions with database. All calls are blocking due to usage of database transactions.
//   If one needs to make non-blocking calls, it should implement asynchronous calls by itself.
object DatabaseHelper {

    sealed class FlagCheckResult {
        object NoSuchPlayer: FlagCheckResult()
        object Wrong: FlagCheckResult()
        object Exists: FlagCheckResult()
        class Success(val price: Int): FlagCheckResult()
    }

    class DbOpResult<T>(val result: T? = null, val exception: Exception? = null)

    interface ChangeListener<T: BaseDTO> {
        fun onAdd(value: T)
        fun onDelete(value: T)
        fun onUpdate(value: T)
    }

    private val mCompetitions = MutableStateFlow<List<CompetitionDTO>>(emptyList())
    val competitions: SharedFlow<List<CompetitionDTO>> = mCompetitions

    private val mTasks = MutableStateFlow<List<TaskDTO>>(emptyList())
    val tasks: SharedFlow<List<TaskDTO>> = mTasks

    private val mPlayers = MutableStateFlow<List<PlayerDTO>>(emptyList())
    val players: SharedFlow<List<PlayerDTO>> = mPlayers

    private val mSolves = MutableStateFlow<List<SolveDTO>>(emptyList())
    val solves: SharedFlow<List<SolveDTO>> = mSolves

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T: BaseDTO> register(listener: ChangeListener<T>) {

        when(T::class) {
            CompetitionDTO::class -> competitionsListeners.add(listener as ChangeListener<CompetitionDTO>)
            TaskDTO::class -> tasksListeners.add(listener as ChangeListener<TaskDTO>)
            PlayerDTO::class -> playersListeners.add(listener as ChangeListener<PlayerDTO>)
            SolveDTO::class -> solvesListeners.add(listener as ChangeListener<SolveDTO>)
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T: BaseDTO> unregister(listener: ChangeListener<T>) {
        when (T::class) {
            CompetitionDTO::class -> competitionsListeners.remove(listener as ChangeListener<CompetitionDTO>)
            TaskDTO::class -> tasksListeners.remove(listener as ChangeListener<TaskDTO>)
            PlayerDTO::class -> playersListeners.remove(listener as ChangeListener<PlayerDTO>)
            SolveDTO::class -> solvesListeners.remove(listener as ChangeListener<SolveDTO>)
        }
    }

    private val database: Database by lazy {
        Database.connect("jdbc:sqlite:db/data.db", "org.sqlite.JDBC")
    }

    val competitionsListeners = LinkedList<ChangeListener<CompetitionDTO>>()
    val tasksListeners = LinkedList<ChangeListener<TaskDTO>>()
    val playersListeners = LinkedList<ChangeListener<PlayerDTO>>()
    val solvesListeners = LinkedList<ChangeListener<SolveDTO>>()

    private fun recalculateTaskPrice(initial: Int, solves: Int): Int {
        if (solves <= 0) return initial
        return initial * (1f / solves.toFloat()).toInt()
    }

    private suspend fun <T> performOn(database: Database, action: () -> T): T {
        return withContext(Dispatchers.IO) { transaction(db = database) { action() } }
    }

    suspend fun init(): DbOpResult<Boolean> {
        return try {
            if (!File(DATABASE_FOLDER).exists()) {
                File(DATABASE_FOLDER).mkdir()
            }
            performOn(database) {
                SchemaUtils.create(CompetitionsTable, TasksTable, SolvesTable, PlayersTable)
            }

            mCompetitions.emit(getAllCompetitions().result ?: emptyList())
            mTasks.emit(getAllTasks().result ?: emptyList())
            mPlayers.emit(getAllPlayers().result ?: emptyList())
            mSolves.emit(getAllSolves().result ?: emptyList())

            DbOpResult(true)
        } catch (ex: Exception) {
            DbOpResult(false, exception = ex)
        }
    }

    suspend fun getAllCompetitions(): DbOpResult<List<CompetitionDTO>> {
        return try {
            DbOpResult(performOn(database) { CompetitionEntity.all().map { CompetitionDTO(it) } })
        } catch (ex: Exception) {
            DbOpResult(exception = ex)
        }
    }

    suspend fun getAllTasks(): DbOpResult<List<TaskDTO>> {
        return try {
            DbOpResult(performOn(database) { TaskEntity.all().map { TaskDTO(it) } })
        } catch (ex: Exception) {
            DbOpResult(exception = ex)
        }
    }

    suspend fun getAllPlayers(): DbOpResult<List<PlayerDTO>> {
        return try {
            DbOpResult(performOn(database) { PlayerEntity.all().map { PlayerDTO(it) } })
        } catch (ex: Exception) {
            DbOpResult(exception = ex)
        }
    }

    suspend fun getAllSolves(): DbOpResult<List<SolveDTO>> {
        return try {
            DbOpResult(performOn(database) { SolveEntity.all().map { SolveDTO(it) } })
        } catch (ex: Exception) {
            DbOpResult(exception = ex)
        }
    }

    suspend fun getTask(id: Long): DbOpResult<TaskDTO> {
        return try {
            DbOpResult(performOn(database) { TaskEntity.findById(id)?.let { TaskDTO(it) } })
        } catch (ex: Exception) {
            DbOpResult(exception = ex)
        }
    }

    suspend fun getTasks(competition: CompetitionDTO): DbOpResult<List<TaskDTO>> {
        return try {
            val result = performOn(database) {
                TaskEntity.all().filter { it.competition == competition.id }.map { TaskDTO(it) }
            }
            DbOpResult(result)
        } catch (ex: Exception) {
            DbOpResult(exception = ex)
        }
    }

    suspend fun getSolvedTasksForPlayer(player: PlayerDTO): DbOpResult<List<TaskDTO>> {
        return try {
            val result = performOn(database) {
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
            DbOpResult(result)
        } catch (ex: Exception) {
            DbOpResult(exception = ex)
        }
    }

    suspend fun getPlayersSolvesForTask(task: TaskDTO): DbOpResult<List<PlayerDTO>> {
        return try {
            val result = performOn(database) {
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
            DbOpResult(result)
        } catch (ex: Exception) {
            DbOpResult(exception = ex)
        }
    }

    suspend fun getSolvesForTask(task: TaskDTO): DbOpResult<List<SolveDTO>> {
        return try {
            DbOpResult(performOn(database) {
                SolveEntity.all().filter { it.task == task.id }.map { SolveDTO(it) }
            })
        } catch (ex: Exception) {
            DbOpResult(exception = ex)
        }
    }

    suspend fun addCompetition(name: String): DbOpResult<CompetitionDTO> {
        return try {
            val competition = performOn(database) { CompetitionDTO(CompetitionEntity.new { this.name = name }) }
            competitionsListeners.forEach { it.onAdd(competition) }
            mCompetitions.emit(getAllCompetitions().result ?: emptyList())
            DbOpResult(competition)
        } catch (ex: Exception) {
            DbOpResult(null, ex)
        }
    }

    suspend fun addPlayer(id: Long, userName: String): DbOpResult<PlayerDTO> {
        return try {
            val player = performOn(database) {
                PlayerDTO(PlayerEntity.new(id) {
                    this.userName = userName
                    this.overallScore = 0
                })
            }
            playersListeners.forEach { it.onAdd(player) }
            mPlayers.emit(getAllPlayers().result ?: emptyList())
            DbOpResult(player)
        } catch (ex: Exception) {
            DbOpResult(exception = ex)
        }
    }

    suspend fun addTask(
        category: String,
        name: String,
        description: String,
        price: Int,
        flag: String,
        attachment: String,
        dynamicScoring: Boolean,
        competition: CompetitionDTO,
    ): DbOpResult<TaskDTO> {
        return try {
            val task = performOn(database) {
                TaskDTO(TaskEntity.new() {
                    this.category = category
                    this.name = name
                    this.description = description
                    this.price = price
                    this.flag = flag
                    this.attachment = attachment
                    this.dynamicScoring = dynamicScoring
                    this.competition = competition.id
                    this.solvesCount = 0
                })
            }
            tasksListeners.forEach { it.onAdd(task) }
            mTasks.emit(getAllTasks().result ?: emptyList())
            DbOpResult(task)
        } catch (ex: Exception) {
            DbOpResult(null, ex)
        }
    }

    suspend fun updateCompetition(competition: CompetitionDTO): DbOpResult<Boolean> {
        return try {
            performOn(database) {
                competition.entity.name = competition.name
            }
            competitionsListeners.forEach { it.onUpdate(competition) }
            mCompetitions.emit(getAllCompetitions().result ?: emptyList())
            DbOpResult(true)
        } catch (ex: Exception) {
            DbOpResult(false, ex)
        }
    }

    suspend fun updateTask(task: TaskDTO): DbOpResult<Boolean> {
        return try {
            performOn(database) {
                task.entity.category = task.category
                task.entity.name = task.name
                task.entity.description = task.description
                task.entity.price = task.price
                task.entity.flag = task.flag
                task.entity.attachment = task.attachment
                task.entity.competition = task.competition
            }
            tasksListeners.forEach { it.onUpdate(task) }
            mTasks.emit(getAllTasks().result ?: emptyList())
            DbOpResult(true)
        } catch (ex: Exception) {
            DbOpResult(false, ex)
        }
    }

    suspend fun updatePlayer(player: PlayerDTO): DbOpResult<Boolean> {
        return try {
            performOn(database) {
                player.entity.userName = player.userName
                player.entity.overallScore = player.overallScore
            }
            playersListeners.forEach { it.onUpdate(player) }
            mPlayers.emit(getAllPlayers().result ?: emptyList())
            DbOpResult(true)
        } catch (ex: Exception) {
            DbOpResult(false, ex)
        }
    }

    suspend fun deleteCompetition(competition: CompetitionDTO): DbOpResult<Boolean> {
        return try {
            performOn(database) {
                TasksTable.deleteWhere { TasksTable.competition eq competition.id }
                competition.entity.delete()
            }
            competitionsListeners.forEach { it.onDelete(competition) }
            mCompetitions.emit(getAllCompetitions().result ?: emptyList())
            DbOpResult(true)
        } catch (ex: Exception) {
            DbOpResult(false, ex)
        }
    }

    suspend fun deleteTask(task: TaskDTO): DbOpResult<Boolean> {
        return try {
            performOn(database) {
                task.entity.delete()
            }
            tasksListeners.forEach { it.onDelete(task) }
            mTasks.emit(getAllTasks().result ?: emptyList())
            DbOpResult(true)
        } catch (ex: Exception) {
            DbOpResult(false, ex)
        }
    }

    suspend fun deletePlayer(player: PlayerDTO): DbOpResult<Boolean> {
        return try {
            performOn(database) {
                player.entity.delete()
            }
            playersListeners.forEach { it.onDelete(player) }
            mPlayers.emit(getAllPlayers().result ?: emptyList())
            DbOpResult(true)
        } catch (ex: Exception) {
            DbOpResult(false, ex)
        }
    }

    suspend fun onPlayerPassedFlag(competitionId: Long, playerId: Long, flag: String): DbOpResult<FlagCheckResult> {
        try {
            val competitionDTO = performOn(database) {
                CompetitionEntity.findById(competitionId)?.let { CompetitionDTO(it) }
            } ?: return DbOpResult(FlagCheckResult.Wrong)

            val playerDTO = performOn(database) {
                PlayerEntity.findById(playerId)?.let { PlayerDTO(it) }
            } ?: return DbOpResult(FlagCheckResult.NoSuchPlayer)

            // Verify flag is valid for any flag
            val taskDTO = performOn(database) {
                TaskEntity.find {
                    (TasksTable.competition eq competitionDTO.id) and (TasksTable.flag eq flag)
                }.firstOrNull()?.let { TaskDTO(it) }
            } ?: return DbOpResult(FlagCheckResult.Wrong)

            // Find all other solves for this task
            val solves = getSolvesForTask(taskDTO)
            if (solves.result == null) return DbOpResult(exception = solves.exception)

            // Check if player already solved task
            if (solves.result.any { it.player.value == playerId }) return DbOpResult(FlagCheckResult.Exists)

            taskDTO.solvesCount++
            taskDTO.commit()

            // Update players table
            val newTaskPrice = if (taskDTO.dynamicScoring)
                recalculateTaskPrice(taskDTO.price, taskDTO.solvesCount)
            else taskDTO.price

            performOn(database) {
                for (solve in solves.result) {
                    val player = PlayerEntity.findById(solve.player.value) ?: continue
                    val score = ScoreEntity.find { PlayersTable.id eq player.id }.firstOrNull() ?: continue
                    score.score = score.score - taskDTO.price + newTaskPrice
                }
                taskDTO.entity.price = newTaskPrice

                val score = ScoreEntity.find { ScoresTable.player eq playerDTO.id }.firstOrNull()
                if (score == null) {
                    ScoreEntity.new {
                        this.competition = competitionDTO.id
                        this.player = playerDTO.id
                        this.score = taskDTO.price
                    }
                } else {
                    score.score += taskDTO.price
                }
            }

            mSolves.emit(getAllSolves().result ?: emptyList())
            return DbOpResult(FlagCheckResult.Success(newTaskPrice))
        } catch (ex: Exception) {
            return DbOpResult(exception = ex)
        }
    }

    suspend fun checkPlayerExists(id: Long): DbOpResult<Boolean> {
        return try {
            DbOpResult(performOn(database) { PlayersTable.select { PlayersTable.id eq id }.empty() })
        } catch (ex: Exception) {
            DbOpResult(null, ex)
        }
    }
}