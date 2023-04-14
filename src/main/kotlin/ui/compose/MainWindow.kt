package ui.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import ui.compose.main.MainViewModel

@Composable
fun ApplicationScope.MainWindow() {
    val viewModel = remember { MainViewModel() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "CTF Bot Compose",
        state = rememberWindowState(width = 600.dp, height = 600.dp)
    ) {
        LaunchedEffect(null) {
            viewModel.loadCompetitions()
        }
        MaterialTheme {
            MainWindow.Content(viewModel, Modifier.fillMaxSize())
        }
    }
}

object MainWindow {
    @Composable
    fun Content(viewModel: MainViewModel, modifier: Modifier = Modifier) {
        Row(modifier) {
            LazyColumn(Modifier.fillMaxHeight().background(Color.Green).padding(16.dp)) {
                items(viewModel.competitions) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(it.selected, onClick = { viewModel.onSelected(it.id) })
                        Text(it.name)
                    }
                }
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button({viewModel.addToLog()}) {
                    Text("Start bot")
                }
                Button({}) {
                    Text("Stop bot")
                }
                LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
                    items(viewModel.log) { LogEntry(it) }
                }
            }

        }
    }

    @Composable
    fun LogEntry(logMessage: MainViewModel.LogMessage, modifier: Modifier = Modifier) {
        Text(logMessage.message, color = logMessage.color)
    }
}

@Preview
@Composable
fun MainWindowPreview() {
    MainWindow.Content(MainViewModel())
}
