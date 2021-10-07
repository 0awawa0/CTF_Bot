import database.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseTest {

    @BeforeAll
    fun initDatabase() {
        runBlocking {
            println("Initializing database...")
            val dbFile = File("${DbHelper.DATABASE_FOLDER}/data.db")
            val backupFile = File("${DbHelper.DATABASE_FOLDER}/data_backup.db")
            if (dbFile.exists()) dbFile.renameTo(backupFile)

            val initResult = DbHelper.init()
            assert(initResult) { "Failed to init database." }
            println("Database initialized.")
        }
    }

    @AfterAll
    fun cleanup() {
        val dbFile = File("${DbHelper.DATABASE_FOLDER}/data.db")
        dbFile.delete()
        val backupFile = File("${DbHelper.DATABASE_FOLDER}/data_backup.db")
        if (backupFile.exists()) backupFile.renameTo(dbFile)
    }

    private fun createCompetition(model: CompetitionModel): CompetitionDTO {
        return runBlocking {
            val result = DbHelper.add(model)
            assert(result != null)

            return@runBlocking result!!
        }
    }

    private fun createTask(competition: CompetitionDTO, model: TaskModel): TaskDTO {
        return runBlocking {
            val result = DbHelper.add(competition, model)
            assert(result != null)

            return@runBlocking result!!
        }
    }

    private fun createPlayer(model: PlayerModel): PlayerDTO {
        return runBlocking {
            val result = DbHelper.add(model)
            assert(result != null)

            return@runBlocking result!!
        }
    }

    @Test
    fun testAddUpdateDeleteCompetition() {
        runBlocking {
            val competitions = DbHelper.getAllCompetitions()
            assert(competitions.isEmpty())

            val newCompetition = createCompetition(CompetitionModel("Test competition"))

            assert(DbHelper.getAllCompetitions().any { it.id == newCompetition.id })

            val newName = "New test competition"
            newCompetition.name = newName
            newCompetition.updateEntity()

            assert(
                DbHelper.getAllCompetitions().find { it.id == newCompetition.id }?.name == newName
            )

            DbHelper.delete(newCompetition)
            assert(DbHelper.getAllCompetitions().isEmpty())
        }
    }

    @Test
    fun testAddUpdateDeleteTask() {
        runBlocking {
            val competition = createCompetition(CompetitionModel("Test competition"))
            val tasks = competition.getTasks()

            assert(tasks.isEmpty())

            val newTask = createTask(
                competition,
                TaskModel(
                    "Category",
                    "Name",
                    "Description",
                    "Flag",
                    "Attachment"
                )
            )

            assert(competition.getTasks().any { it.id == newTask.id })

            val newCategory = "New category"
            val newName = "New name"
            val newDescription = "New description"
            val newFlag = "New flag"
            val newAttachment = "New attachment"

            newTask.category = newCategory
            newTask.name = newName
            newTask.description = newDescription
            newTask.flag = newFlag
            newTask.attachment = newAttachment
            newTask.updateEntity()

            assert(DbHelper.getAllTasks().any {
                it.id == newTask.id
                        && it.category == newCategory
                        && it.name == newName
                        && it.description == newDescription
                        && it.flag == newFlag
                        && it.attachment == newAttachment
            })

            DbHelper.delete(newTask)

            assert(DbHelper.getAllTasks().none { it.id == newTask.id }
                    && competition.getTasks().none { it.id == newTask.id}
            )

            DbHelper.delete(competition)
        }
    }

    @Test
    fun testAddUpdateDeletePlayer() {
        runBlocking {
            assert(DbHelper.getAllPlayers().isEmpty())

            val newPlayer = createPlayer(PlayerModel(1, "Name"))

            assert(DbHelper.getAllPlayers().any { it.id == newPlayer.id && it.name == newPlayer.name })
            val newName = "New name"
            newPlayer.name = newName
            newPlayer.updateEntity()
            assert(DbHelper.getAllPlayers().any { it.id == newPlayer.id && it.name == newName})

            DbHelper.delete(newPlayer)
            assert(DbHelper.getAllPlayers().none { it.id == newPlayer.id && it.name == newPlayer.name })
        }
    }

    @Test
    fun testPlayerPassedFlag() {
//        runBlocking {
//            assert(DbHelper.getAllCompetitions().isEmpty())
//            assert(DbHelper.getAllTasks().isEmpty())
//            assert(DbHelper.getAllPlayers().isEmpty())
//
//            val newCompetition = createCompetition(CompetitionModel("Competition"))
//            val newTask = createTask(
//                newCompetition,
//                TaskModel(
//                    "Category",
//                    "Name",
//                    "Description",
//                    "Flag",
//                    "Attachment"
//                )
//            )
//            val newPlayer = createPlayer(PlayerModel(1, "Player"))
//
//            assert(newPlayer.getTotalScore() == 0)
//            val taskPrice = newTask.getTaskPrice()
//            val result = DbHelper.onFlagPassed(newCompetition, newPlayer.id, newTask.flag)
//            assert(result is DbHelper.FlagCheckResult.CorrectFlag)
//            assert((result as DbHelper.FlagCheckResult.CorrectFlag).price == taskPrice)
//
//            val currScore = newPlayer.getCompetitionScore(newCompetition)
//            val currTotalScore = newPlayer.getTotalScore()
//            assert(currScore == taskPrice && currTotalScore == taskPrice)
//
//            val anotherPass = DbHelper.onFlagPassed(newCompetition, newPlayer.id, newTask.flag)
//            assert(anotherPass is DbHelper.FlagCheckResult.SolveExists)
//
//            assert(newTask.getSolvedPlayers().any { it.id == newPlayer.id })
//
//            val anotherScore = newPlayer.getCompetitionScore(newCompetition)
//            val anotherTotalScore = newPlayer.getTotalScore()
//            assert(currScore == anotherScore && currTotalScore == anotherTotalScore)
//
//            val wrongPass = DbHelper.onFlagPassed(newCompetition, newPlayer.id, "Wrong flag")
//            assert(wrongPass is DbHelper.FlagCheckResult.WrongFlag)
//
//            val wrongPassScore = newPlayer.getCompetitionScore(newCompetition)
//            val wrongPassTotalScore = newPlayer.getTotalScore()
//            assert(wrongPassScore == currScore && wrongPassTotalScore == currTotalScore)
//
//            val anotherCompetition = createCompetition(CompetitionModel("Another competition"))
//            val anotherTask = createTask(
//                anotherCompetition,
//                TaskModel(
//                    "Category",
//                    "Name",
//                    "Description",
//                    "Flag",
//                    "Attachment"
//                )
//            )
//
//            val anotherPrice = anotherTask.getTaskPrice()
//            val newPass = DbHelper.onFlagPassed(anotherCompetition, newPlayer.id, anotherTask.flag)
//            assert(newPass is DbHelper.FlagCheckResult.CorrectFlag)
//
//            val newScore = newPlayer.getCompetitionScore(anotherCompetition)
//            val newTotalScore = newPlayer.getTotalScore()
//            assert(newScore == anotherPrice && newTotalScore == currTotalScore + anotherPrice)
//
//            DbHelper.delete(newPlayer)
//            DbHelper.delete(newCompetition)
//            DbHelper.delete(anotherCompetition)
//            assert(DbHelper.getAllTasks().none { it.id == newTask.id || it.id == anotherTask.id })
//            assert(DbHelper.getAllSolves().isEmpty())
//        }
    }

    @Test
    fun testConcurrentFlagPassing() {
//        runBlocking {
//            assert(DbHelper.getAllCompetitions().isEmpty())
//            assert(DbHelper.getAllTasks().isEmpty())
//            assert(DbHelper.getAllPlayers().isEmpty())
//
//            val newCompetition = createCompetition(CompetitionModel("Competition"))
//            val task1 = createTask(
//                newCompetition,
//                TaskModel(
//                    "Category",
//                    "Name",
//                    "Description",
//                    "Flag",
//                    "Attachment"
//                )
//            )
//            val task2 = createTask(
//                newCompetition,
//                TaskModel(
//                    "Category",
//                    "Name",
//                    "Description",
//                    "Flag 2",
//                    "Attachment"
//                )
//            )
//            val newPlayer = createPlayer(PlayerModel(1, "Player"))
//
//            val price1 = task1.getTaskPrice()
//            val price2 = task2.getTaskPrice()
//            val expectedScore = price1 + price2
//            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
//            val coroutines: MutableList<Job> = mutableListOf()
//            repeat(10) {
//                coroutines.add(scope.launch {
//                    DbHelper.onFlagPassed(newCompetition, newPlayer.id, task1.flag)
//                })
//                coroutines.add(scope.launch {
//                    DbHelper.onFlagPassed(newCompetition, newPlayer.id, task2.flag)
//                })
//            }
//
//            coroutines.joinAll()
//
//            val competitionScore = newPlayer.getCompetitionScore(newCompetition)
//            val totalScore = newPlayer.getTotalScore()
//            assert(competitionScore == expectedScore && totalScore == expectedScore) {
//                "Expected $expectedScore but received ${competitionScore}"
//            }
//            assert(DbHelper.getAllSolves().size == 2)
//
//            val secondPlayer = createPlayer(PlayerModel(2, "Second player"))
//
//            val newPrice1 = task1.getTaskPrice()
//            val newPrice2 = task2.getTaskPrice()
//            val expectedSecondScore = newPrice1 + newPrice2
//
//            val result1 = DbHelper.onFlagPassed(newCompetition, secondPlayer.id, task1.flag)
//            val result2 = DbHelper.onFlagPassed(newCompetition, secondPlayer.id, task2.flag)
//            assert(result1 is DbHelper.FlagCheckResult.CorrectFlag)
//            assert(result2 is DbHelper.FlagCheckResult.CorrectFlag)
//
//            val secondTotalScore = secondPlayer.getTotalScore()
//            val firstPlayerTotalScore = newPlayer.getTotalScore()
//            assert(secondTotalScore == expectedSecondScore
//                    && firstPlayerTotalScore == expectedSecondScore
//            )
//
//            DbHelper.delete(newPlayer)
//            DbHelper.delete(newCompetition)
//
//            assert(DbHelper.getAllSolves().isEmpty())
//        }
    }
}