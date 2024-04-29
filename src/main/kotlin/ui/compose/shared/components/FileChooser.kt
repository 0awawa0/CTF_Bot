package ui.compose.shared.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.filechooser.FileSystemView

class Dialog {
    enum class Mode { LOAD, SAVE }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun FileChooser(
    mode:Dialog.Mode = Dialog.Mode.LOAD,
    title:String = "Choisissez un fichier",
    extensions:List<FileNameExtensionFilter> = listOf(),
    onResult: (files: List<File>) -> Unit
) {
    DisposableEffect(Unit) {
        val job = GlobalScope.launch {
            val fileChooser = JFileChooser()
            fileChooser.dialogTitle = title
            fileChooser.isMultiSelectionEnabled = mode == Dialog.Mode.LOAD
            fileChooser.isAcceptAllFileFilterUsed = extensions.isEmpty()
            extensions.forEach { fileChooser.addChoosableFileFilter(it) }

            val returned = if(mode == Dialog.Mode.LOAD) {
                fileChooser.showOpenDialog(null)
            } else {
                fileChooser.showSaveDialog(null)
            }

            onResult(when(returned) {
                JFileChooser.APPROVE_OPTION -> {
                    if(mode == Dialog.Mode.LOAD) {
                        val files = fileChooser.selectedFiles.filter { it.canRead() }
                        files.ifEmpty { emptyList() }
                    } else {
                        if(!fileChooser.fileFilter.accept(fileChooser.selectedFile)) {
                            val ext = (fileChooser.fileFilter as FileNameExtensionFilter).extensions[0]
                            fileChooser.selectedFile = File(fileChooser.selectedFile.absolutePath + ".$ext")
                        }
                        listOf(fileChooser.selectedFile)
                    }
                };
                else -> listOf();
            })
        }

        onDispose {
            job.cancel()
        }
    }
}

@Composable
fun FileChooserDialog(
    title: String = "",
    onResult: (result: File?) -> Unit
) {
    LaunchedEffect(Unit) {
        val fileChooser = JFileChooser(FileSystemView.getFileSystemView())
        fileChooser.currentDirectory = File(System.getProperty("user.dir"))
        fileChooser.dialogTitle = title
        fileChooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
        fileChooser.isAcceptAllFileFilterUsed = true
        fileChooser.selectedFile = null
        fileChooser.currentDirectory = null
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            onResult(file)
        } else {
            onResult(null)
        }
    }
}