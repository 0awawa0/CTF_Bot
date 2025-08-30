package ui.compose.shared.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import ui.compose.shared.GreenButtonColor
import ui.compose.shared.RedButtonColor
import utils.Logger

data class InstanceCreationModel(
    val title: String,
    val fields: List<InstanceField>,
    val onSave: () -> Unit,
    val onCancel: () -> Unit
) {
    sealed interface InstanceField {
        val name: String

        data class Text(
            override val name: String,
            val value: State<String>,
            val onValueChanged: (String) -> Unit,
            val maxLines: Int = 1
        ): InstanceField

        data class File(
            override val name: String,
            val selectedPath: State<String>,
            val onOpenFileSelection: () -> Unit
        ): InstanceField
    }
}

@Composable
fun InstanceCreationDialog(model: InstanceCreationModel?) {

    model ?: return

    DialogWindow(
        visible = true,
        title = model.title,
        onCloseRequest = {  model.onCancel() },
        state = rememberDialogState(width = 500.dp, height = minOf(model.fields.size * 125.dp + 200.dp, 1000.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(Modifier.weight(0.5f), horizontalAlignment = Alignment.CenterHorizontally) {
                    model.fields.forEach {
                        Box(Modifier.height(125.dp), contentAlignment = Alignment.Center) {
                            Text(it.name)
                        }
                    }
                }

                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    model.fields.forEach {
                        Box(Modifier.height(125.dp), contentAlignment = Alignment.Center) {
                            InstanceField(model = it, Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
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
private fun InstanceField(model: InstanceCreationModel.InstanceField, modifier: Modifier = Modifier) {
    when (model) {
        is InstanceCreationModel.InstanceField.Text -> TextInstanceFiled(model, modifier)
        is InstanceCreationModel.InstanceField.File -> FileInstanceField(model, modifier)
    }
}

@Composable
private fun TextInstanceFiled(model: InstanceCreationModel.InstanceField.Text, modifier: Modifier = Modifier) {
    val value by model.value
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        TextField(
            modifier = Modifier.weight(1f),
            value = value,
            onValueChange = model.onValueChanged,
            maxLines = model.maxLines
        )
    }
}

@Composable
private fun FileInstanceField(model: InstanceCreationModel.InstanceField.File, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        val path by model.selectedPath
        path.takeIf { it.isNotBlank() }?.let {
            Text(it, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(16.dp))
        Button(onClick = model.onOpenFileSelection) {
            Text("Select")
        }
    }
}