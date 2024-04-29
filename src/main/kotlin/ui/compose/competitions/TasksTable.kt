package ui.compose.competitions

import androidx.compose.ui.text.style.TextAlign
import database.DbHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ui.compose.shared.components.Column
import ui.compose.shared.components.Row
import ui.compose.shared.dto.Task

private val idColumn = Column("id", false, 1.5f, 1, TextAlign.Center)
private val categoryColumn = Column("Category", true, 2f, 1, TextAlign.Center)
private val nameColumn = Column("Name", true, 3f, 2, TextAlign.Center)
private val descriptionColumn = Column("Description", true, 4f, 4)
private val attachmentColumn = Column("Attachment", true, 3f, 3)
private val flagColumn = Column("Flag", true, 6f, 2)
private val solvesColumn = Column("Solves", false, 2f, 1, TextAlign.Center)

val taskColumns = listOf(
    idColumn,
    categoryColumn,
    nameColumn,
    descriptionColumn,
    attachmentColumn,
    flagColumn,
    solvesColumn
)

class TaskRow(
    private val dto: Task,
    private val changesCoroutineScope: CoroutineScope,
    private val onChangesSaved: suspend () -> Unit
) : Row {
    override val columns = taskColumns
    override val values: MutableMap<Column, String> = mutableMapOf(
        idColumn to dto.id.toString(),
        categoryColumn to dto.category,
        nameColumn to dto.name,
        descriptionColumn to dto.description,
        attachmentColumn to dto.attachment,
        flagColumn to dto.flag,
        solvesColumn to dto.solvesCount.toString()
    )

    override fun commitChanges() {
        changesCoroutineScope.launch {
            DbHelper.getTask(dto.id)?.let {
                it.category = values[categoryColumn] ?: ""
                it.name = values[nameColumn] ?: ""
                it.description = values[descriptionColumn] ?: ""
                it.attachment = values[attachmentColumn] ?: ""
                it.flag = values[flagColumn] ?: ""
                DbHelper.update(it)
            }
            onChangesSaved()
        }
    }
}