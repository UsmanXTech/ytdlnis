package com.deniscerri.ytdl.windows

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.deniscerri.ytdl.windows.runtime.WindowsYtdlEngine

fun main() {
    val engine = WindowsYtdlEngine()
    engine.initialize()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "YTDLnis"
        ) {
            App(engine)
        }
    }
}
