package ui.compose.shared.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class Column(
    val name: String,
    val editable: Boolean = false,
    val weight: Float = 1f,
    val maxLines: Int = 1,
    val textAlign: TextAlign = TextAlign.Start
)

interface Row {
    val id: Long
    val isSelected: Boolean
        get() = false
    val columns: List<Column>
    val values: MutableMap<Column, String>
    val editingColumn: Column?
    fun commitChanges()
}

@Composable
fun <T: Row>Table(
    columns: List<Column>,
    items: List<T>,
    modifier: Modifier = Modifier,
    onSelectRow: (T) -> Unit = {},
    onEditColumn: (Column) -> Unit = {},
) {
    Column(modifier) {
        TableHeader(columns)
        LazyColumn {
            items(items) {
                TableRow(it, onSelect = onSelectRow, onEditColumn = onEditColumn)
            }
        }
    }
}

@Composable
fun TableHeader(
    columns: List<Column>,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth()) {
        for (column in columns) {
            Text(
                text = column.name,
                modifier = Modifier.fillMaxWidth()
                    .weight(column.weight)
                    .border(BorderStroke(1.dp, Color.Black))
                    .padding(4.dp),
                maxLines = 1,
                style = MaterialTheme.typography.subtitle1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun <T: Row> TableRow(
    row: T,
    onSelect: (T) -> Unit,
    onEditColumn: (Column) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier
        .fillMaxWidth()
        .height(intrinsicSize = IntrinsicSize.Max)
        .clickable(enabled = true, onClick = { onSelect(row) })
        .then(if (row.isSelected) {
            Modifier.border(BorderStroke(2.dp, MaterialTheme.colors.secondary))
        } else {
            Modifier
        })
    ) {
        for (field in row.columns) {
            TableField(
                value = row.values[field] ?: "",
                onTap = { onSelect(row) },
                onEdit = { onEditColumn(field) },
                modifier = Modifier.fillMaxWidth().fillMaxHeight().weight(field.weight),
                editable = field.editable,
                onValueChanged = {
                    row.values[field] = it
                    row.commitChanges()
                },
                maxLines = field.maxLines,
                textAlign = field.textAlign,
                isEditing = field == row.editingColumn
            )
        }
    }
}

@Composable
fun TableField(
    value: String,
    onTap: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    onValueChanged: (String) -> Unit = {},
    editable: Boolean = false,
    maxLines: Int = 3,
    textAlign: TextAlign = TextAlign.Start,
    isEditing: Boolean = false,
) {
    var lastClick by remember { mutableStateOf(0L) }
    Box(modifier
        .border(BorderStroke(1.dp, if (isEditing) Color.Black else Color.LightGray))
        .background(if (isEditing) Color.White else Color.Transparent)
        .clickable {
            onTap()
            val time = System.nanoTime() / 1_000_000
            if (time - lastClick < 250) {
                if (editable) onEdit()
            }
            lastClick = time
        }
        .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isEditing) {

            var updatedValue by remember { mutableStateOf(value) }
            val focusRequester = remember { FocusRequester() }
            BasicTextField(
                value = updatedValue,
                modifier = Modifier.onKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Escape) onValueChanged(value)
                    if (keyEvent.key == Key.Enter) {
                        onValueChanged(updatedValue)
                    }
                    return@onKeyEvent false
                }
                    .focusRequester(focusRequester),
                onValueChange = { updatedValue = it },
                maxLines = maxLines,
                textStyle = MaterialTheme.typography.subtitle2
            )
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        } else {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = value,
                maxLines = maxLines,
                style = MaterialTheme.typography.subtitle2,
                overflow = TextOverflow.Ellipsis,
                textAlign = textAlign
            )
        }
    }
}