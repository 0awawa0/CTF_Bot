package ui.compose.shared.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


interface SelectableItem {
    val name: String
    val selected: Boolean
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T: SelectableItem> SelectableItemsList(
    items: List<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    header: @Composable () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        stickyHeader { header() }
        items(items) {
            Row(
                modifier = Modifier.clickable(enabled = enabled) { onItemSelected(it) }
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start)
            {
                RadioButton(it.selected, enabled = enabled, onClick = { onItemSelected(it) })
                Text(it.name)
            }
        }
    }
}