package ui.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import ui.compose.competitions.CompetitionsPage
import ui.compose.main.MainPage
import ui.compose.players.PlayersPage
import ui.compose.shared.components.TabsView

@Composable
fun ApplicationScope.MainWindow() {
    val tabs = remember { listOf(MainPage(), CompetitionsPage(), PlayersPage()) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "CTF Bot Compose",
        state = rememberWindowState(width = 600.dp, height = 600.dp)
    ) {
        MaterialTheme {
            TabsView(
                tabs = tabs,
                modifier = Modifier.fillMaxSize(),
                tabBackgroundColor = MaterialTheme.colors.secondary
            )
        }
    }
}
