package new_db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction


object DbHelper {

    val database: Database by lazy {
        Database.connect("jdbc:sqlite:db/data.db", "org.sqlite.JDBC")
    }

    suspend fun <T> transactionOn(database: Database, action: () -> T): T {
        return withContext(Dispatchers.IO) { transaction(db = database) { action() } }
    }
}