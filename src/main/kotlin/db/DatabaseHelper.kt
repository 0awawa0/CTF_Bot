package db

import kotlinx.coroutines.Dispatchers
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

    interface ChangeListener<T: BaseDTO> {
        fun onAdd(value: T)
        fun onDelete(value: T)
        fun onUpdate(value: T)
    }

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

    suspend fun init(): DbOpResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (!File(DATABASE_FOLDER).exists()) {
                    File(DATABASE_FOLDER).mkdir()
                }
                transaction(db = database) {
                    SchemaUtils.create(CompetitionsTable, TasksTable, SolvesTable, PlayersTable)
                }

                DbOpResult(true)
            } catch (ex: Exception) {
                DbOpResult(false, exception = ex)
            }
        }
    }

    suspend fun getAllCompetitions(): DbOpResult<List<CompetitionDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                DbOpResult(transaction(db = database) {
                    CompetitionEntity.all().map {
                        CompetitionDTO(it)
                    }
                })
            } catch (ex: Exception) {
                DbOpResult(exception = ex)
            }
        }
    }

    suspend fun getAllTasks(): DbOpResult<List<TaskDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                DbOpResult(transaction(db = database) {
                    TaskEntity.all().map {
                        TaskDTO(it)
                    }
                })
            } catch (ex: Exception) {
                DbOpResult(exception = ex)
            }
        }
    }

    suspend fun getAllPlayers(): DbOpResult<List<PlayerDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                DbOpResult(transaction(db = database) {
                    PlayerEntity.all().map {
                        PlayerDTO(it)
                    }
                })
            } catch (ex: Exception) {
                DbOpResult(exception = ex)
            }
        }
    }

    suspend fun getAllSolves(): DbOpResult<List<SolveDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                DbOpResult(transaction(db = database) {
                    SolveEntity.all().map {
                        SolveDTO(it)
                    }
                })
            } catch (ex: Exception) {
                DbOpResult(exception = ex)
            }
        }
    }

    suspend fun getTask(id: Long): DbOpResult<TaskDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val result = transaction(db = database) {
                    TaskEntity.findById(id)
                }
                DbOpResult(if (result == null) null else TaskDTO(result))
            } catch (ex: Exception) {
                DbOpResult(exception = ex)
            }
        }
    }

    suspend fun getTasks(competition: CompetitionDTO): DbOpResult<List<TaskDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val result = transaction(db = database) {
                    TaskEntity.all().filter { it.competition == competition.id }.map { TaskDTO(it) }
                }
                DbOpResult(result)
            } catch (ex: Exception) {
                DbOpResult(exception = ex)
            }
        }
    }

    suspend fun getSolvedTasksForPlayer(player: PlayerDTO): DbOpResult<List<TaskDTO>> {
        return withContext(Dispatchers.IO) {
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
                DbOpResult(result)
            } catch (ex: Exception) {
                DbOpResult(exception = ex)
            }
        }
    }

    suspend fun getPlayersSolvesForTask(task: TaskDTO): DbOpResult<List<PlayerDTO>> {
        return withContext(Dispatchers.IO) {
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
                DbOpResult(result)
            } catch (ex: Exception) {
                DbOpResult(exception = ex)
            }
        }
    }

    suspend fun getSolvesForTask(task: TaskDTO): DbOpResult<List<SolveDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val result = transaction(db = database) {
                    SolveEntity.all().filter { it.task == task.id }.map { SolveDTO(it) }
                }
                return@withContext DbOpResult(result)
            } catch (ex: Exception) {
                return@withContext DbOpResult(exception = ex)
            }
        }
    }

    suspend fun addCompetition(name: String): DbOpResult<CompetitionDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val competition = transaction(db = database) {
                    return@transaction CompetitionEntity.new {
                        this.name = name
                    }
                }
                val dto = CompetitionDTO(competition)
                competitionsListeners.forEach { it.onAdd(dto) }
                DbOpResult(dto)
            } catch (ex: Exception) {
                DbOpResult(null, ex)
            }
        }
    }

    suspend fun addPlayer(id: Long, userName: String): DbOpResult<PlayerDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val player = transaction(db = database) {
                    return@transaction PlayerEntity.new(id) {
                        this.userName = userName
                        this.currentScore = 0
                        this.seasonScore = 0
                    }
                }
                val dto = PlayerDTO(player)
                playersListeners.forEach { it.onAdd(dto) }
                DbOpResult(dto)
            } catch (ex: Exception) {
                DbOpResult(exception = ex)
            }
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
        return withContext(Dispatchers.IO) {
            try {
                val task = transaction(db = database) {
                    return@transaction TaskEntity.new() {
                        this.category = category
                        this.name = name
                        this.description = description
                        this.price = price
                        this.flag = flag
                        this.attachment = attachment
                        this.dynamicScoring = dynamicScoring
                        this.competition = competition.id
                    }
                }
                val dto = TaskDTO(task)
                tasksListeners.forEach { it.onAdd(dto) }
                DbOpResult(dto)
            } catch (ex: Exception) {
                DbOpResult(null, ex)
            }
        }
    }

    suspend fun updateCompetition(competition: CompetitionDTO): DbOpResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                transaction(db = database) {
                    competition.entity.name = competition.name
                }
                competitionsListeners.forEach { it.onUpdate(competition) }
                DbOpResult(true)
            } catch (ex: Exception) {
                DbOpResult(false, ex)
            }
        }
    }

    suspend fun updateTask(task: TaskDTO): DbOpResult<Boolean> {
        return withContext(Dispatchers.IO) {
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
                tasksListeners.forEach { it.onUpdate(task) }
                DbOpResult(true)
            } catch (ex: Exception) {
                DbOpResult(false, ex)
            }
        }
    }

    suspend fun updatePlayer(player: PlayerDTO): DbOpResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                transaction(db = database) {
                    player.entity.userName = player.userName
                    player.entity.currentScore = player.currentScore
                    player.entity.seasonScore = player.seasonScore
                }
                playersListeners.forEach { it.onUpdate(player) }
                DbOpResult(true)
            } catch (ex: Exception) {
                DbOpResult(false, ex)
            }
        }
    }

    suspend fun deleteCompetition(competition: CompetitionDTO): DbOpResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                transaction(db = database) {
                    competition.entity.delete()
                }
                competitionsListeners.forEach { it.onDelete(competition) }
                DbOpResult(true)
            } catch (ex: Exception) {
                DbOpResult(false, ex)
            }
        }
    }

    suspend fun deleteTask(task: TaskDTO): DbOpResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                transaction(db = database) {
                    task.entity.delete()
                }
                tasksListeners.forEach { it.onDelete(task) }
                DbOpResult(true)
            } catch (ex: Exception) {
                DbOpResult(false, ex)
            }
        }
    }

    suspend fun deletePlayer(player: PlayerDTO): DbOpResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                transaction(db = database) {
                    player.entity.delete()
                }
                playersListeners.forEach { it.onDelete(player) }
                DbOpResult(true)
            } catch (ex: Exception) {
                DbOpResult(false, ex)
            }
        }
    }

    suspend fun onPlayerPassedFlag(competitionId: Long, playerId: Long, flag: String): DbOpResult<FlagCheckResult> {
        return withContext(Dispatchers.IO) {
            try {
                // Check player exists
                val players = getAllPlayers()
                if (players.result == null) return@withContext DbOpResult(exception = players.exception)
                if (players.result.none { it.id.value == playerId })
                    return@withContext DbOpResult(FlagCheckResult.NoSuchPlayer)

                // Verify flag is valid for any flag
                val task = transaction(db = database) {
                    TaskEntity.find {
                        (TasksTable.competition eq competitionId) and (TasksTable.flag eq flag)
                    }.firstOrNull()
                } ?: return@withContext DbOpResult(FlagCheckResult.Wrong)

                // Find all other solves for this task
                val solves = getSolvesForTask(TaskDTO(task))
                if (solves.result == null) return@withContext DbOpResult(exception = solves.exception)

                // Check if player already solved task
                if (solves.result.any { it.player.value == playerId })
                    return@withContext DbOpResult(FlagCheckResult.Exists)

                // Update players table
                val newTaskPrice = recalculateTaskPrice(
                    task.price,
                    if (task.dynamicScoring) task.solvesCount + 1
                    else 0
                )
                transaction(db = database) {
                    solves.result.forEach {
                        val player = PlayerEntity.findById(it.player.value)
                        player?.currentScore = player?.currentScore ?: 0 - task.price + newTaskPrice
                    }
                    task.price = newTaskPrice
                    task.solvesCount = task.solvesCount + 1
                    val player = PlayerEntity.findById(playerId)
                    player?.currentScore = player?.currentScore ?: 0 + newTaskPrice
                }

                DbOpResult(FlagCheckResult.Success(newTaskPrice))
            } catch (ex: Exception) {
                DbOpResult(exception = ex)
            }
        }
    }

    suspend fun checkPlayerExists(id: Long): DbOpResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val result = transaction(db = database) {
                    return@transaction PlayersTable.select { PlayersTable.id eq id }.empty()
                }
                DbOpResult(result)
            } catch (ex: Exception) {
                DbOpResult(null, ex)
            }
        }
    }
}