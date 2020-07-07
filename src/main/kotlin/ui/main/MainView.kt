package ui.main

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import tornadofx.*

class MainView : View("CTF Bot") {

    private val borderPane = BorderPane()

    val startBotButton = button {
        text = "Start bot"
        font = Font(14.0)
        borderpaneConstraints {
            marginBottom = 10.0
        }
    }

    val startTestingButton = button {
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

    val tfTestingPassword = textfield {
        font = Font(12.0)
    }

    val taLog = textarea {
        this.maxHeight = Double.MAX_VALUE
        this.maxWidth = Double.MAX_VALUE
        vboxConstraints {
            marginTop = 25.0
            vgrow = Priority.ALWAYS
            fitToParentWidth()
        }
    }

    override val root = vbox {

        borderPane.top = startBotButton
        borderPane.left = lblTestingPassword
        borderPane.center = tfTestingPassword
        borderPane.right = startTestingButton

        startBotButton.prefWidthProperty().bind(borderPane.widthProperty())

        this.padding = Insets(10.0)
        add(borderPane)
        add(taLog)
    }
}
