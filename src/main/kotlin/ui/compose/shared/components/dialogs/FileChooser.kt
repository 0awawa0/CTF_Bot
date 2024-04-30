package ui.compose.shared.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

data class FileChooseModel(
    val title: String,
    val mode: Mode,
    val onResult: (result: File?) -> Unit
) {
    enum class Mode(val value: Int){
        OPEN_FILE(JFileChooser.FILES_ONLY),
        OPEN_DIR(JFileChooser.DIRECTORIES_ONLY),
        OPEN_FILE_OR_DIR(JFileChooser.FILES_AND_DIRECTORIES)
    }
}

@Composable
fun FileChooserDialog(
    model: FileChooseModel?
) {
    if (model == null) return

    LaunchedEffect(Unit) {
        val fileChooser = JFileChooser(FileSystemView.getFileSystemView())
        fileChooser.currentDirectory = File(System.getProperty("user.dir"))
        fileChooser.dialogTitle = model.title
        fileChooser.fileSelectionMode = model.mode.value
        fileChooser.isAcceptAllFileFilterUsed = true
        fileChooser.selectedFile = null
        fileChooser.currentDirectory = null
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            model.onResult(file)
        } else {
            model.onResult(null)
        }
    }
}

@Composable
fun FileSaveDialog(
    title: String = "",
    onResult: (result: File?) -> Unit
) {
    LaunchedEffect(Unit) {
        val fileChooser = JFileChooser(FileSystemView.getFileSystemView())
        fileChooser.currentDirectory = File(System.getProperty("user.dir"))
        fileChooser.dialogTitle = title
        fileChooser.fileSelectionMode = JFileChooser.SAVE_DIALOG
        fileChooser.isAcceptAllFileFilterUsed = true
        fileChooser.selectedFile = null
        fileChooser.currentDirectory = null
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            onResult(file)
        } else {
            onResult(null)
        }
    }
}