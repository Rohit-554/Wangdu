package io.jadu.wangdu

import io.jadu.wangdu.ui.theme.AppTheme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.jadu.wangdu.navigation.AppNavigation

@Composable
@Preview
fun App() {
    AppTheme {
        AppNavigation()
    }
}
