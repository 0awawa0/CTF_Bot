package ui.main

import db.CompetitionDTO
import javafx.geometry.Insets
import javafx.geometry.Pos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import tornadofx.*
import ui.competitions.CompetitionsView

class MainView: View("CTF Bot") {

    private val viewModel = MainViewModel()
    private val viewScope = CoroutineScope(Dispatchers.JavaFx)
    private var flowJob: Job? = null

    private val menuBar = menubar {
        menu("Menu") {
            item("Competitions") {
                action { find<CompetitionsView>().openModal() }
            }
            item("Players")
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

        flowJob = viewScope.launch {
            viewModel.competitions.collect { competitionSelector.items = it.toObservable() }
        }
    }

    override fun onUndock() {
        flowJob?.cancel()
        super.onUndock()
    }
}