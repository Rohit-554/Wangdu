package io.jadu.wangdu

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.jadu.wangdu.navigation.AppNavigation
import io.jadu.wangdu.ui.theme.AppTheme

@Composable
@Preview
fun App(
    serverHost: String,
    serverPort: Int
) {
    AppTheme {
        AppNavigation(
            serverHost = serverHost,
            serverPort = serverPort
        )
    }
}
