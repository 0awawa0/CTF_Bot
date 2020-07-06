package ui.main

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import tornadofx.*

class MainView : View("CTF Bot") {

    private val borderPane = BorderPane()
    private val startBotButton = button {
        text = "Start bot"
        font = Font(14.0)
        borderpaneConstraints {
            marginBottom = 10.0
        }
    }
    private val startTestingButton = button {
        text = "Start testing bot"
        font = Font(14.0)
        borderpaneConstraints {
            marginLeft = 10.0
        }
    }

    private val lblTestingPassword = label {
        text = "Testing password"
        borderpaneConstraints {
            alignment = Pos.CENTER
            marginRight = 15.0
        }
    }
    private val tfTestingPassword = textfield {
        font = Font(12.0)
    }

    override val root = vbox {

        borderPane.top = startBotButton
        borderPane.left = lblTestingPassword
        borderPane.center = tfTestingPassword
        borderPane.right = startTestingButton

        prefWidth = 600.0
        prefHeight = 400.0

        primaryStage.isResizable = false
        startBotButton.prefWidthProperty().bind(borderPane.widthProperty())

        this.padding = Insets(10.0)
        add(borderPane)
    }
}
