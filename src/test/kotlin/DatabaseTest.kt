import db.DatabaseHelper
import kotlinx.coroutines.runBlocking
import database.DbHelper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseTest {

    @BeforeAll
    fun initDatabase() {
        runBlocking {
            println("Initializing database...")
            val initResult = DatabaseHelper.init()
            assert(initResult.result != null) {
                "Failed to init database. Exception: ${initResult.exception?.message}"
            }
            println("Database initialized.")
        }
    }

    @Test
    fun testFetchAddUpdateDeleteCompetition() {
        runBlocking {
            println("Fetching existing competitions...")
            var competitions = DatabaseHelper.getAllCompetitions()
            assert(competitions.result != null) {
                "Failed to fetch competitions. Exception: ${competitions.exception?.message}"
            }

            println("Competitions fetched. Generating name...")
            var firstName = "Competition ${Random.nextLong()}"
            while (competitions.result!!.any { it.name == firstName }) {
                firstName = "Competition ${Random.nextLong()}"
            }

            println("Will add competition with name $firstName")
            println("Adding competition...")
            val competitionAddResult = DatabaseHelper.addCompetition(firstName)
            assert(competitionAddResult.result != null) {
                "Failed to add competition. Exception: ${competitionAddResult.exception?.message}"
            }

            println("Competition added successfully. Checking competition exists..")
            competitions = DatabaseHelper.getAllCompetitions()
            assert(competitions.result != null) {
                "Failed to fetch competitions. Exception: ${competitions.exception?.message}"
            }
            assert(competitions.result!!.any { it.name == firstName }) {
                "Added competition was not found"
            }
            println("Added competition found. Generating next name...")

            val secondName = "Competition ${Random.nextLong()}"
            println("Trying to change name to $secondName")
            competitionAddResult.result!!.name = secondName
            val updateResult = competitionAddResult.result!!.commit()
            assert(updateResult.result == true) {
                "Failed to update competition. Exception: ${updateResult.exception?.message}"
            }
            println("Competition name changed. Checking the database...")

            competitions = DatabaseHelper.getAllCompetitions()
            assert(competitions.result != null) {
                "Failed to fetch competitions. Exception: ${competitions.exception?.message}"
            }
            assert(competitions.result!!.any { it.name == secondName && it.id == competitionAddResult.result!!.id }) {
                "Database check failed after name change. Competitions in database: ${competitions.result?.joinToString("\n"){ "${it.id}:${it.name}" }}"
            }
            println("Name changed successfully. Deleting competition...")

            val deletionResult = competitionAddResult.result!!.delete()
            assert(deletionResult.result == true) {
                "Failed to delete competition. Exception: ${deletionResult.exception?.message}"
            }
            println("Deletion successful. Checking database...")

            competitions = DatabaseHelper.getAllCompetitions()
            assert(competitions.result != null) {
                "Failed to fetch competitions. Exception: ${competitions.exception?.message}"
            }
            assert(competitions.result!!.none { it.name == secondName || it.name == firstName || it.id == competitionAddResult.result!!.id }) {
                "Deletion failed. Found undeleted competition"
            }
            println("Deletion checked.")
        }
    }

    @Test
    fun testFetchAddUpdateDeleteTask() {
        runBlocking {
            val competition = DatabaseHelper.addCompetition("Test competition").result!!
            println("Fetching existing tasks")
            var tasks = DatabaseHelper.getTasks(competition)
        }
    }

    @Test
    fun testTaskPriceDecay() {
        for (i in 0 .. DbHelper.DECAY) {
            println(DbHelper.getNewTaskPrice(i))
        }
    }
}