package ui.compose.shared.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import ui.compose.shared.BlueButtonColor
import ui.compose.shared.GreenButtonColor
import ui.compose.shared.RedButtonColor


data class DialogModel(
    val title: String,
    val message: String,
    val options: List<Option>,
    val close: () -> Unit
) {
    data class Option(
        val text: String,
        val backgroundColor: Color,
        val textColor: Color,
        val action: () -> Unit
    )

    companion object {
        fun createInfoOption(text: String, action: () -> Unit) = Option(
            text = text,
            backgroundColor = BlueButtonColor,
            textColor = Color.White,
            action = action
        )

        fun createAcceptOption(text: String, action: () -> Unit) = Option(
            text = text,
            backgroundColor = GreenButtonColor,
            textColor = Color.White,
            action = action
        )

        fun createDenyOption(text: String, action: () -> Unit) = Option(
            text = text,
            backgroundColor = RedButtonColor,
            textColor = Color.White,
            action = action
        )
    }
}

@Composable
fun Dialog(model: DialogModel?) {
    val title = model?.title ?: ""
    val message = model?.message ?: ""
    val options = model?.options ?: emptyList()
    val closeAction = model?.close ?: {}

    DialogWindow(
        visible = model != null,
        title = title,
        onCloseRequest = closeAction,
        state = rememberDialogState(size = DpSize(400.dp, 200.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                text = message,
                textAlign = TextAlign.Center
            )
            for (idx in options.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    DialogOption(option = options[idx])
                    Spacer(Modifier.width(12.dp))
                    if (idx + 1 < options.size) {
                        DialogOption(option = options[idx + 1])
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DialogOption(modifier: Modifier = Modifier, option: DialogModel.Option) {
    Button(
        modifier = modifier,
        onClick = option.action,
        colors = ButtonDefaults.buttonColors(backgroundColor = option.backgroundColor)
    ) {
        Text(option.text, color = option.textColor)
    }
}