package io.jadu.wangdu.data.mapper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.jadu.wangdu.data.dto.DrawEvent
import io.jadu.wangdu.data.dto.PointData
import io.jadu.wangdu.domain.model.DrawPath

fun DrawPath.toEvent(userId: String) : DrawEvent = DrawEvent(
    userId = userId,
    points = points.map { PointData(it.x, it.y) },
    color = color.value.toInt(),
    strokeWidth = strokeWidth
)

fun DrawEvent.toPath() : DrawPath = DrawPath (
    points = points.map { Offset(it.x,it.y) },
    color = Color(color),
    strokeWidth = strokeWidth
)
