package ui.compose

import androidx.compose.ui.window.application
import database.DbHelper
import kotlinx.coroutines.runBlocking

object ComposeApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            DbHelper.init()
        }
        application(exitProcessOnExit = true) {
            MainWindow()
        }
    }
}