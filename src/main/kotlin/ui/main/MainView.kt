package ui.main

import database.CompetitionDTO
import javafx.geometry.Insets
import javafx.geometry.Pos
import tornadofx.*
import ui.BaseView
import ui.competitions.CompetitionsView
import ui.players.PlayersView

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

    private val competitionSelector = combobox<CompetitionDTO>() {
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
        competitionSelector.items = viewModel.competitions
    }
}