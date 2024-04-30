package ui.compose.shared.components.dialogs

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
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ui.compose.shared.GreenButtonColor
import ui.compose.shared.RedButtonColor
import ui.compose.shared.WhiteSemiTransparent


data class InstanceCreationModel(
    val fields: List<InstanceField>,
    val onSave: () -> Unit,
    val onCancel: () -> Unit
) {
    data class InstanceField(
        val name: String,
        val value: State<String>,
        val onValueChanged: (String) -> Unit
    )
}

@Composable
fun InstanceCreationDialog(
    modifier: Modifier,
    model: InstanceCreationModel?
) {
    model ?: return
    Box(
        modifier = modifier.background(WhiteSemiTransparent),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.matchParentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            model.fields.forEach {
                InstanceField(model = it)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.width(IntrinsicSize.Max).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = model.onSave,
                    colors = ButtonDefaults.buttonColors(backgroundColor = GreenButtonColor)
                ) { Text("Save", color = Color.White) }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = model.onCancel,
                    colors = ButtonDefaults.buttonColors(backgroundColor = RedButtonColor)
                ) { Text("Cancel", color = Color.White) }
            }
        }
    }
}

@Composable
private fun InstanceField(modifier: Modifier = Modifier, model: InstanceCreationModel.InstanceField) {
    val value by model.value
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(model.name)
        Spacer(modifier = Modifier.width(8.dp))
        TextField(value = value, onValueChange = model.onValueChanged)
    }
}