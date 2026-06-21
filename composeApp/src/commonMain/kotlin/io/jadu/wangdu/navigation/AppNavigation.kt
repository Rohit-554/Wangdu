package io.jadu.wangdu.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import io.jadu.wangdu.ui.screens.HomeScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(
        SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Screen.Home::class)
                }
            }
        },
        Screen.Home
    )

    Crossfade(targetState = backStack.lastOrNull() ?: Screen.Home) { screen ->
        when (screen) {
            is Screen.Home -> HomeScreen()
            
        }
    }
}