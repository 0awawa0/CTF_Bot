package new_db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction


object DbHelper {

    const val INITIAL_TASK_PRICE = 1000
    const val MIN_POINTS = 100
    const val DECAY = 10

    val database: Database by lazy {
        Database.connect("jdbc:sqlite:db/data.db", "org.sqlite.JDBC")
    }

    suspend fun <T> transactionOn(database: Database, action: () -> T): T {
        return withContext(Dispatchers.IO) { transaction(db = database) { action() } }
    }

    fun getNewTaskPrice(solvesCount: Int): Int {
        return (MIN_POINTS - INITIAL_TASK_PRICE) / (DECAY * DECAY) * (solvesCount * solvesCount) + INITIAL_TASK_PRICE
    }
}