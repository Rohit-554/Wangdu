package io.jadu.wangdu.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data class Home(val displayName: String) : Screen

    @Serializable
    data object NameEntry : Screen
}