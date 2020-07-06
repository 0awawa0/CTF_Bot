package ui.main

import javafx.geometry.Insets
import tornadofx.*

class MainView : View("CTF Bot") {
    override val root = vbox {
        this.padding = Insets(50, 50 )
        button("Click")
        label("Waiting")
    }
}
