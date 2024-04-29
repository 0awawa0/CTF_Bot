package ui.compose.competitions.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ui.compose.shared.GreenButtonColor
import ui.compose.shared.RedButtonColor
import ui.compose.shared.WhiteSemiTransparent

@Composable
fun AddCompetition(modifier: Modifier, viewModel: AddCompetitionViewModel) {
    val competitionName by viewModel.competitionName.collectAsState()
    Box(modifier.background(color = WhiteSemiTransparent), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.matchParentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Field(
                modifier = Modifier,
                name = "Competition name",
                value = competitionName,
                onValueChanged = viewModel::onCompetitionNameChanged
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.width(IntrinsicSize.Max).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = viewModel::onSave,
                    colors = ButtonDefaults.buttonColors(backgroundColor = GreenButtonColor)
                ) { Text("Save", color = Color.White) }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = viewModel::onCancel,
                    colors = ButtonDefaults.buttonColors(backgroundColor = RedButtonColor)
                ) { Text("Cancel", color = Color.White) }
            }
        }
    }
}

@Composable
private fun Field(modifier: Modifier, name: String, value: String, onValueChanged: (String) -> Unit) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(name)
        Spacer(modifier = Modifier.width(8.dp))
        TextField(value = value, onValueChange = onValueChanged)
    }
}