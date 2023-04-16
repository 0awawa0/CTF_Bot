package ui.compose.shared.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


interface TabPage {
    @Composable
    fun TabContent(modifier: Modifier)

    @Composable
    fun PageContent(modifier: Modifier)
}


@Composable
fun TabsView(
    tabs: List<TabPage>,
    modifier: Modifier = Modifier,
    tabBackgroundColor: Color = MaterialTheme.colors.primarySurface,
    indicatorModifier: Modifier = Modifier.padding(8.dp),
    contentModifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TabRow(selectedTab, backgroundColor = tabBackgroundColor) {
                tabs.forEachIndexed { index, tab ->
                    Tab(index == selectedTab, onClick = { selectedTab = index }) {
                        tab.TabContent(modifier = indicatorModifier)
                    }
                }
            }
        }
    ) {
        tabs[selectedTab].PageContent(contentModifier)
    }
}