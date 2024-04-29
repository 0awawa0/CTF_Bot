package ui.compose.players

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ui.compose.shared.components.TabPage

class PlayersPage: TabPage {

    @Composable
    override fun TabContent(modifier: Modifier) {
        Text("Players", modifier = modifier)
    }

    @Composable
    override fun PageContent(modifier: Modifier) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Players page")
        }
    }


}