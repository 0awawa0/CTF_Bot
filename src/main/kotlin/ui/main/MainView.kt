package ui.main

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
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
    private val logArea = textarea { isEditable = false }

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

            hbox {
                spacing = 8.0
                button {
                    text = "Start"

                    disableProperty().bind(viewModel.isRunning)
                    action {
                        val competition = competitionSelector.selectedItem ?: return@action
                        viewModel.startBot(competition)
                    }
                }.fitToParentWidth()

                button {
                    text = "Start for testing"
                    disableProperty().bind(viewModel.isRunning)
                    action {
                        val competition = competitionSelector.selectedItem ?: return@action
                        showTestingPasswordDialog(competition)
                    }
                }.fitToParentWidth()

                button {
                    text = "Stop"
                    disableProperty().bind(
                        viewModel.isRunning.booleanBinding { return@booleanBinding it?.not() ?: false}
                    )
                    action { viewModel.stopBot() }
                }.fitToParentWidth()
            }.fitToParentWidth()

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

    private fun showTestingPasswordDialog(competition: MainViewModel.CompetitionItem) {
        dialog {
            spacing = 8.0
            alignment = Pos.CENTER
            text = "Enter testing password"

            val password = passwordfield {
                action {
                    if (text.isNotBlank()) {
                        viewModel.startBotForTesting(competition, text)
                        close()
                    } else {
                        alert(
                            Alert.AlertType.ERROR,
                            "Empty password",
                            "Testing password must not be blank"
                        )
                    }
                }
            }
            button {
                text = "Ok"

                action {
                    if (password.text.isNotBlank()) {
                        viewModel.startBotForTesting(competition, password.text)
                        close()
                    } else {
                        alert(
                            Alert.AlertType.ERROR,
                            "Empty password",
                            "Testing password must not be blank"
                        )
                    }
                }
            }.fitToParentWidth()
        }
    }
}