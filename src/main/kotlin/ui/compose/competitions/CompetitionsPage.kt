package ui.compose.competitions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ui.compose.shared.components.dialogs.Dialog
import ui.compose.shared.components.dialogs.FileChooserDialog
import ui.compose.shared.components.SelectableItemsList
import ui.compose.shared.components.TabPage
import ui.compose.shared.components.Table
import ui.compose.shared.components.dialogs.InstanceCreationDialog

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
    private fun Content(viewModel: CompetitionsViewModel, modifier: Modifier = Modifier) {
        val dialog by viewModel.dialog.collectAsState()
        val fileChooseDialog by viewModel.fileChooseDialog.collectAsState()
        val instanceCreationDialog by viewModel.instanceCreationModel.collectAsState()

        Box(modifier) {
            Row(
                modifier = Modifier.matchParentSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LeftPane(viewModel, modifier = Modifier.fillMaxHeight())
                RightPane(viewModel, modifier = Modifier.fillMaxWidth())
            }

            InstanceCreationDialog(modifier = Modifier.matchParentSize(), model = instanceCreationDialog)
        }

        Dialog(model = dialog)
        FileChooserDialog(model = fileChooseDialog)
    }

    @Composable
    private fun LeftPane(viewModel: CompetitionsViewModel, modifier: Modifier = Modifier) {

        val buttonsState = remember(viewModel.selectedCompetition) {
            LeftPaneButtonsState(
                addButtonState = ButtonState(true, viewModel::addCompetition),
                addFromJsonButtonState = ButtonState(true, viewModel::onAddFileFromJson),
                exportToJsonButtonState = ButtonState(
                    isEnabled = viewModel.selectedCompetition != null,
                    onClick = viewModel::exportCompetition
                ),
                deleteButtonState = ButtonState(
                    isEnabled = viewModel.selectedCompetition != null,
                    onClick = viewModel::onCompetitionDeleteClicked
                )
            )
        }
        Column(
            modifier.border(
                border = BorderStroke(1.dp, MaterialTheme.colors.onBackground),
                shape = RoundedCornerShape(CornerSize(4.dp))
            ).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LeftButtonsPane(buttonsState = buttonsState)
            Divider(Modifier.width(150.dp))
            SelectableItemsList(
                modifier = Modifier.width(200.dp),
                items = viewModel.competitions,
                onItemSelected = { viewModel.onSelected(it) }
            )
        }
    }

    @Composable
    private fun LeftButtonsPane(
        modifier: Modifier = Modifier,
        buttonsState: LeftPaneButtonsState
    ) {
        val buttonColors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
        Column(modifier.width(IntrinsicSize.Max), verticalArrangement = Arrangement.SpaceEvenly) {
            Row(modifier = Modifier.width(IntrinsicSize.Max), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onClick = buttonsState.addButtonState.onClick,
                    colors = buttonColors,
                    enabled = buttonsState.addButtonState.isEnabled
                ) { Text("Add") }
                Spacer(Modifier.width(8.dp))
                Button(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onClick = buttonsState.deleteButtonState.onClick,
                    colors = buttonColors,
                    enabled = buttonsState.deleteButtonState.isEnabled
                ) { Text("Delete") }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = buttonsState.addFromJsonButtonState.onClick,
                colors = buttonColors,
                enabled = buttonsState.addFromJsonButtonState.isEnabled
            ) { Text("Add from JSON") }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = buttonsState.exportToJsonButtonState.onClick,
                colors = buttonColors,
                enabled = buttonsState.exportToJsonButtonState.isEnabled
            ) { Text("Export to JSON") }
        }
    }

    @Composable
    private fun RightPane(viewModel: CompetitionsViewModel, modifier: Modifier = Modifier) {
        Column(modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = viewModel.selectedCompetition?.name ?: "",
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Row(Modifier.fillMaxSize()) {
                Table(
                    columns = taskColumns,
                    items = viewModel.tasks,
                    modifier = Modifier.fillMaxWidth().weight(17f)
                )
                Spacer(Modifier.width(4.dp))
                Table(
                    columns = playersColumns,
                    items = viewModel.scoreboard,
                    modifier = Modifier.fillMaxWidth().weight(4f),
                )
            }
        }
    }

    private data class ButtonState(
        val isEnabled: Boolean,
        val onClick: () -> Unit
    )

    private data class LeftPaneButtonsState(
        val addButtonState: ButtonState,
        val addFromJsonButtonState: ButtonState,
        val exportToJsonButtonState: ButtonState,
        val deleteButtonState: ButtonState
    )
}