import db.DatabaseHelper
import org.junit.Test

class DatabaseTest {

    @Test
    fun testAddDeleteCompetition() {
        assert(DatabaseHelper.init().result == true)
        val competitions = DatabaseHelper.getCompetitions().result
        assert(competitions != null)
        competitions?.filter { it.name == "Competition 1" }?.forEach { it.delete() }
        assert(DatabaseHelper.addCompetition("Competition 1").result != null)
        val newCompetitions = DatabaseHelper.getCompetitions().result
        assert(newCompetitions != null)
        assert(newCompetitions?.find { it.name == "Competition 1" } != null)
        newCompetitions?.find { it.name == "Competition 1" }?.delete()
        assert(DatabaseHelper.getCompetitions().result?.find { it.name == "Competition 1" } == null)
    }

    @Test
    fun testAddDeleteTask() {
        assert(DatabaseHelper.init().result == true)
        val competition = DatabaseHelper.addCompetition("Competition 1")
        assert(competition.result != null) { competition.exception?.stackTraceToString() ?: "" }
        val task = DatabaseHelper.addTask("category", "name", "description", 100, "flag", "", competition.result!!)
        assert(task.result != null) { task.exception?.stackTraceToString() ?: "" }
        competition.result?.delete()
    }
}