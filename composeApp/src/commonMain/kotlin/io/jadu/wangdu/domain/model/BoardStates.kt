package io.jadu.wangdu.domain.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class WhiteBoardState(
    val paths: List<DrawPath> = emptyList(),
    val currentPath: DrawPath? = null
)

data class DrawPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)