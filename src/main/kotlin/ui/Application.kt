package ui

import ui.compose.ComposeApplication
import ui.fx.FxApplication

fun main(args: Array<String> = emptyArray()) {
    if ("--compose" in args) {
        ComposeApplication.main(args)
    } else {
        FxApplication.main(args)
    }
}