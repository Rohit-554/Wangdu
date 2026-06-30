package io.jadu.wangdu.domain.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class WhiteBoardState(
    val paths: List<DrawPath> = emptyList(),
    val currentPath: DrawPath? = null,
    val cursors: Map<String, CursorState> = emptyMap(),
    val connectedUsers: Map<String, String> = emptyMap(),
    val activeTool: DrawingTool = DrawingTool.Pen(color = Color.Black, width = 8f)
)

data class DrawPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)