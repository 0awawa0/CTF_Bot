package ui.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowSize
import androidx.compose.ui.window.rememberDialogState
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
        LaunchedEffect(null) { viewModel.loadCompetitions() }
        MaterialTheme {
            MainWindow.Content(viewModel, Modifier.fillMaxSize())
        }
    }
}

object MainWindow {

    data class ButtonsConfiguration(
        val startEnabled: Boolean,
        val stopEnabled: Boolean,
        val testingEnabled: Boolean,
        val onStartClicked: () -> Unit,
        val onStopClicked: () -> Unit,
        val onTestingClicked: () -> Unit
    )

    @Composable
    fun Content(viewModel: MainViewModel, modifier: Modifier = Modifier) {
        val canStartBot by viewModel.canStart.collectAsState(false)
        val canStopBot by viewModel.started.collectAsState(false)
        var testDialogVisible by remember { mutableStateOf(false) }

        val buttonsConfiguration = remember(canStartBot, canStopBot) {
            ButtonsConfiguration(
                startEnabled = canStartBot,
                stopEnabled = canStopBot,
                testingEnabled = canStartBot,
                onStartClicked = viewModel::startBot,
                onStopClicked = viewModel::stopBot,
                onTestingClicked = { testDialogVisible = true }
            )
        }

        Box(modifier, contentAlignment = Alignment.Center) {
            Row(Modifier.fillMaxSize()) {
                CompetitionsSection(
                    competitions = viewModel.competitions,
                    onCompetitionSelected = viewModel::onSelected,
                    modifier = Modifier.fillMaxHeight()
                )
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    ButtonsSection(buttonsConfiguration)
                    LogSection(viewModel.log, modifier = Modifier.fillMaxSize())
                }
            }

            Dialog(
                title = "Set testing password",
                visible = testDialogVisible,
                onCloseRequest = { testDialogVisible = false },
                resizable = false,
                state = rememberDialogState(size = DpSize(400.dp, 200.dp))
            ) {
                TestingModeSection(
                    onStartClicked = viewModel::startTesting,
                    onCancelClicked = { testDialogVisible = false }
                )
            }
        }
    }

    @Composable
    fun CompetitionsSection(
        competitions: List<MainViewModel.Competition>,
        onCompetitionSelected: (MainViewModel.Competition) -> Unit,
        modifier: Modifier = Modifier
    ) {
        LazyColumn(
            modifier = modifier
                .background(MaterialTheme.colors.background)
                .border(
                    border = BorderStroke(1.dp, MaterialTheme.colors.onBackground),
                    shape = RoundedCornerShape(CornerSize(4.dp))
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(competitions) {
                Row(
                    modifier = Modifier.clickable { onCompetitionSelected(it) }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start)
                {
                    RadioButton(it.selected, onClick = { onCompetitionSelected(it) })
                    Text(it.name)
                }
            }
        }
    }

    @Composable
    fun ButtonsSection(
        buttonsConfiguration: ButtonsConfiguration,
        modifier: Modifier = Modifier
    ) {
        Column(modifier.width(intrinsicSize = IntrinsicSize.Max)) {
            Button(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                onClick = buttonsConfiguration.onStartClicked,
                enabled = buttonsConfiguration.startEnabled) { Text("Start bot") }

            Button(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                onClick = buttonsConfiguration.onStopClicked,
                enabled = buttonsConfiguration.stopEnabled) { Text("Stop bot") }

            Button(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                onClick = buttonsConfiguration.onTestingClicked,
                enabled = buttonsConfiguration.testingEnabled,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
            ) { Text("Testing mode") }
        }
    }

    @Composable
    fun LogSection(
        log: List<MainViewModel.LogMessage>,
        modifier: Modifier = Modifier
    ) {
        LazyColumn(modifier = modifier
            .background(Color.White)
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(CornerSize(4.dp)))
        ) {
            items(log) { LogEntry(it, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
        }
    }

    @Composable
    fun LogEntry(logMessage: MainViewModel.LogMessage, modifier: Modifier = Modifier) {
        Text(logMessage.message, color = logMessage.color, modifier = modifier)
    }

    @Composable
    fun TestingModeSection(
        onStartClicked: (String) -> Unit,
        onCancelClicked: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        var passwordVisible by remember { mutableStateOf(false) }
        var password by remember { mutableStateOf("") }
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val hideTransformation = VisualTransformation { string ->
                    val newString = buildString { repeat(string.length) { append('*') } }
                    TransformedText(AnnotatedString(newString), OffsetMapping.Identity)
                }

                TextField(
                    value = password,
                    onValueChange = { newValue -> password = newValue },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else hideTransformation
                )

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = "Password visibility"
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                    onClick = { onStartClicked(password) }
                ) { Text("Start bot", style = MaterialTheme.typography.subtitle2) }

                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                    onClick = onCancelClicked
                ) { Text("Cancel", style = MaterialTheme.typography.subtitle2) }
            }
        }
    }
}

@Preview
@Composable
fun MainWindowPreview() {
    MainWindow.Content(MainViewModel())
}
