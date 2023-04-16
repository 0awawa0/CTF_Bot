package ui.compose.shared.dto

import ui.compose.shared.components.SelectableItem

data class Competition(
    val id: Long,
    override val name: String,
    override val selected: Boolean = false
): SelectableItem