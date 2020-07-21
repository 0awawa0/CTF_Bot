package ui.main

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.stage.Stage
import tornadofx.*
import ui.players.PlayersView
import ui.tasks.TasksView
import utils.LogListener
import utils.Logger

class MainView : View("CTF Bot"), LogListener {

    private val presenter = MainPresenter(this)

    private val lblCtfName = label {
        text = "CTF name"
        font = Font(18.0)
        textAlignment = TextAlignment.CENTER
    }

    private val tfCtfName = textfield {
        font = Font(14.0)
    }

    private val borderPane = BorderPane()

    private val playersButton = button {
        text = "Players"
        font = Font(14.0)
        maxWidth = 400.0
        vboxConstraints {
            fitToParentWidth()
        }
    }

    private val tasksButton = button {
        text = "Tasks"
        font = Font(14.0)
        maxWidth = 400.0
        vboxConstraints {
            fitToParentWidth()
        }
    }

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

    private val taLog = textarea {
        this.maxHeight = Double.MAX_VALUE
        this.maxWidth = Double.MAX_VALUE
        vboxConstraints {
            marginTop = 25.0
            vgrow = Priority.ALWAYS
            fitToParentWidth()
        }
    }

    override val root = vbox {

        spacing = 15.0

        add(lblCtfName)
        lblCtfName.fitToParentWidth()
        lblCtfName.alignment = Pos.CENTER

        add(tfCtfName)

        borderPane.top = startBotButton
        borderPane.left = lblTestingPassword
        borderPane.center = tfTestingPassword
        borderPane.right = startTestingButton

        val header = hbox {
            add(borderPane)
            add(vbox {
                add(playersButton)
                add(tasksButton)
                minWidth = 100.0
                maxWidth = 200.0
                hgrow = Priority.SOMETIMES
                spacing = 10.0
                hboxConstraints {
                    marginLeft = 15.0
                    alignment = Pos.CENTER
                }
            })
            borderPane.prefWidthProperty().bind(this.widthProperty())
        }

        this.padding = Insets(10.0)
        add(header)
        add(taLog)

        header.fitToParentWidth()
        startBotButton.prefWidthProperty().bind(borderPane.widthProperty())
    }

    init {
        startBotButton.action { presenter.startBot(tfCtfName.text) }
        startTestingButton.action { presenter.startTestingBot(tfCtfName.text, tfTestingPassword.text) }
        playersButton.action {
            Stage().apply {
                this.title = "Players"
                this.scene = Scene(PlayersView().root, 800.0, 400.0)
                this.minWidth = 800.0
                this.minHeight = 400.0
            }.show()
        }
        tasksButton.action {
            Stage().apply {
                this.title = "Tasks"
                this.scene = Scene(TasksView().root, 800.0, 700.0)
                this.minWidth = 800.0
                this.minHeight = 700.0
            }.show()
        }
        Logger.registerLogListener(this)
    }

    fun onBotStarted() {
        startTestingButton.isDisable = true
        startBotButton.isDisable = true
    }
    override fun onLog(message: String) { taLog.appendText("$message\n") }
}
