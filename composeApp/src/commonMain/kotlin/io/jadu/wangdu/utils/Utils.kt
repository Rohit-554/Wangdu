package io.jadu.wangdu.utils

import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue

fun colorFromUserId(userId: String) : Color {
    val hue = (userId.hashCode().absoluteValue % 360).toFloat()
    return Color.hsl(hue = hue, saturation = 0.7f, lightness = 0.5f)
}