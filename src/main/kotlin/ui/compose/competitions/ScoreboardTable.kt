package ui.compose.competitions

import androidx.compose.ui.text.style.TextAlign
import ui.compose.shared.components.Column
import ui.compose.shared.components.Row
import ui.compose.shared.dto.Score

private val playerNameColumn = Column("Player", false, 3f, 1, TextAlign.Center)
private val playerScoreColumn = Column("Score", false, 1f, 1, TextAlign.Center)

val playersColumns = listOf(playerNameColumn, playerScoreColumn)

data class ScoreboardRow(private val dto: Score): Row {
    override val columns: List<Column> = playersColumns
    override val values: MutableMap<Column, String> = mutableMapOf(
        playerNameColumn to dto.name,
        playerScoreColumn to dto.score.toString()
    )

    override fun commitChanges() = throw UnsupportedOperationException("Players editing is not allowed")

    companion object {

    }
}