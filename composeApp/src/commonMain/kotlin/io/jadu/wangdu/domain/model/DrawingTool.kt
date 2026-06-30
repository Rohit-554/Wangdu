package io.jadu.wangdu.domain.model

import androidx.compose.ui.graphics.Color

sealed class DrawingTool {
    abstract val width: Float
    data class Pen(
        override val width: Float,
        val color: Color
    ) : DrawingTool()

    data class Eraser(
        override val width: Float
    ) : DrawingTool()
}