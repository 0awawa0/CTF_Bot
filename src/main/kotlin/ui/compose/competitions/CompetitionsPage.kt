package ui.compose.competitions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import database.TasksTable
import javafx.scene.paint.Material
import ui.compose.shared.components.SelectableItemsList
import ui.compose.shared.components.TabPage
import ui.compose.shared.components.Table
import ui.compose.shared.dto.Competition
import ui.compose.shared.dto.Task

class CompetitionsPage: TabPage {

    private val viewModel by lazy { CompetitionsViewModel() }

    @Composable
    override fun TabContent(modifier: Modifier) {
        Text("Competitions", modifier = modifier)
    }

    @Composable
    override fun PageContent(modifier: Modifier) {
        LaunchedEffect(viewModel.hashCode()) { viewModel.updateCompetitionsList() }
        Content(viewModel, modifier = modifier.fillMaxSize())
    }

    @Composable
    fun Content(viewModel: CompetitionsViewModel, modifier: Modifier = Modifier) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LeftPane(viewModel, modifier = Modifier.fillMaxHeight())
                RightPane(viewModel, Modifier.fillMaxWidth())
            }
        }
    }

    @Composable
    fun LeftPane(viewModel: CompetitionsViewModel, modifier: Modifier = Modifier) {
        Column(
            modifier.border(
                border = BorderStroke(1.dp, MaterialTheme.colors.onBackground),
                shape = RoundedCornerShape(CornerSize(4.dp))
            ).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LeftPaneButtons()
            Divider(Modifier.width(150.dp))
            SelectableItemsList(
                items = viewModel.competitions,
                onItemSelected = viewModel::onSelected
            )
        }
    }

    @Composable
    fun LeftPaneButtons(modifier: Modifier = Modifier) {
        val buttonColors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
        Column(modifier.width(IntrinsicSize.Max), verticalArrangement = Arrangement.SpaceEvenly) {
            Row(modifier = Modifier.width(IntrinsicSize.Max), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onClick = {},
                    colors = buttonColors
                ) { Text("Add") }
                Spacer(Modifier.width(8.dp))
                Button(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onClick = {},
                    colors = buttonColors
                ) { Text("Delete") }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
                colors = buttonColors
            ) { Text("Add from JSON") }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
                colors = buttonColors
            ) { Text("Export to JSON") }
        }
    }

    @Composable
    fun RightPane(viewModel: CompetitionsViewModel, modifier: Modifier = Modifier) {
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = viewModel.selectedCompetition?.name ?: "",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Row(Modifier.fillMaxSize()) {
                Table(CompetitionsViewModel.tasksColumns, viewModel.tasks)
            }
        }
    }

    @Composable
    fun TasksTable(tasks: List<Task>, modifier: Modifier = Modifier) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = modifier
        ) {
            items(tasks) { task ->
                TasksTableRow(listOf(task.id.toString(), task.category, task.name, task.description, task.attachment, task.flag, task.solvesCount.toString()))
                Divider()
            }
        }
    }

    @Composable
    fun TasksTableRow(fields: List<String>) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (field in fields) {
                TasksTableRowField(
                    value = field,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun TasksTableRowField(value: String, modifier: Modifier = Modifier, editable: Boolean = false) {
        var editable by remember { mutableStateOf(false) }
        Box(modifier.pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = { editable = true }
            )
        }
            .border(BorderStroke(1.dp, if (editable) Color.Black else Color.LightGray))
            .background(if (editable) Color.White else Color.Transparent)
            .padding(4.dp)
        ) {
            if (editable) {

                var updatedValue by remember { mutableStateOf(value) }
                val focusRequester = remember { FocusRequester() }
                BasicTextField(
                    value = updatedValue,
                    modifier = Modifier.onKeyEvent { keyEvent ->
                        if (keyEvent.key == Key.Enter) editable = false
                        return@onKeyEvent false
                    }
                        .focusRequester(focusRequester),
                    onValueChange = { updatedValue = it },
                    maxLines = 1,
                    textStyle = MaterialTheme.typography.subtitle2
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            } else {
                Text(
                    text = value,
                    maxLines = 1,
                    style = MaterialTheme.typography.subtitle2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}