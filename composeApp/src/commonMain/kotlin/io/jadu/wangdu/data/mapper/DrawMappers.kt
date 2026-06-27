package io.jadu.wangdu.data.mapper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.jadu.shared.PointData
import io.jadu.shared.WhiteBoardEvent
import io.jadu.wangdu.domain.model.DrawPath

fun DrawPath.toStrokeDrawn(userId: String) : WhiteBoardEvent = WhiteBoardEvent.StrokeDrawn(
    userId = userId,
    points = points.map { PointData(it.x, it.y) },
    color = color.value.toInt(),
    strokeWidth = strokeWidth
)

fun WhiteBoardEvent.StrokeDrawn.toPath(): DrawPath? {
    if (points.isEmpty()) return null

    return DrawPath(
        points = points.map { Offset(it.x, it.y) },
        color = Color(color.toULong()),
        strokeWidth = strokeWidth,
    )
}
