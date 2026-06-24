package io.jadu.wangdu

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.jadu.wangdu.di.appModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(appModule)
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Wangdu",
        ) {
            App(serverHost = "127.0.0.1", serverPort = 8080)
        }
    }
}
