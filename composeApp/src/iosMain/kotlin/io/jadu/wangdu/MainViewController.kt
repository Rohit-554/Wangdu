package io.jadu.wangdu

import androidx.compose.ui.window.ComposeUIViewController
import io.jadu.wangdu.di.appModule
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() }
) { App(serverHost = "127.0.0.1", serverPort = 8080) }
