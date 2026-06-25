package io.jadu.wangdu.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import io.jadu.wangdu.domain.model.DrawPath

import io.jadu.wangdu.domain.model.WhiteBoardState

@Composable
fun WhiteBoardCanvas(
    state: WhiteBoardState,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down  = awaitFirstDown(requireUnconsumed = false)
                    onDragStart(down.position)
                    down.consume()
                    drag(down.id) {
                        onDrag(it.position)
                        it.consume()
                    }
                    onDragEnd()
                }
            }
    ){
        state.paths.forEach { path ->
            drawStroke(path)
        }
        state.currentPath?.let { drawStroke(it) }
    }
}

private fun DrawScope.drawStroke(
    path: DrawPath,
    alpha: Float = 1f
) {
    val firstPoint = path.points.firstOrNull() ?: return

    if(path.points.size == 1) {
        drawCircle(
            color = path.color.copy(alpha = alpha),
            radius = path.strokeWidth/2,
            center = firstPoint
        )
        return
    }

    val composedPath = Path().apply {
        moveTo(firstPoint.x, firstPoint.y)
        path.points.drop(1).forEach { point ->
            lineTo(point.x, point.y)
        }
    }

    drawPath(
        path = composedPath,
        color = path.color.copy(alpha = alpha),
        style = Stroke(
            width = path.strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}