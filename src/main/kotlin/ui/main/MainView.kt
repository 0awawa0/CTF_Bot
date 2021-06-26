package ui.main

import javafx.geometry.Insets
import javafx.geometry.Pos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import tornadofx.*
import ui.BaseView
import ui.competitions.CompetitionsView
import ui.players.PlayersView
import utils.Logger
import java.text.SimpleDateFormat
import java.util.*

class MainView: BaseView<MainViewModel>(MainViewModel(), "CTF Bot") {

    private val menuBar = menubar {
        menu("Menu") {
            item("Competitions") {
                action { find<CompetitionsView>().openModal() }
            }
            item("Players") {
                action { find<PlayersView>().openModal() }
            }
            item("Exit")
        }
        menubutton("About")
    }

    private val viewScope = CoroutineScope(Dispatchers.JavaFx)

    private val competitionSelector = combobox<MainViewModel.CompetitionItem> {
        cellFormat { text = it.name }
    }
    private val logArea = textarea()

    override val root = vbox {
        spacing = 8.0
        alignment = Pos.CENTER

        add(menuBar)
        menuBar.fitToParentWidth()
        vbox {
            spacing = 8.0
            padding = Insets(8.0)
            alignment = Pos.CENTER

            add(competitionSelector)

            button {
                text = "Start"

                action {

                }
            }

            add(logArea)

            competitionSelector.fitToParentWidth()
            logArea.fitToParentSize()
        }.fitToParentSize()
    }

    override fun onDock() {
        super.onDock()

        viewScope.launch {
            Logger.messages.collect {
                val dateFormat = SimpleDateFormat("dd.MM.YYYY HH:mm:ss")
                val date = Date().time
                val importanceTag = when(it.importance) {
                    Logger.Message.Importance.DEBUG -> "\\D"
                    Logger.Message.Importance.INFO -> "\\I"
                    Logger.Message.Importance.ERROR -> "\\E"
                }
                logArea.appendText("${dateFormat.format(date)}: ${it.tag}: $importanceTag: ${it.message}\n")
            }
        }
        competitionSelector.items = viewModel.competitions
    }
}