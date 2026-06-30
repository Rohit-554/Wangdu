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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.jadu.wangdu.domain.model.CursorState
import io.jadu.wangdu.domain.model.DrawPath
import io.jadu.wangdu.domain.model.WhiteBoardState
import io.jadu.wangdu.ui.theme.WhiteBoardBackgroundColor

@Composable
fun WhiteBoardCanvas(
    state: WhiteBoardState,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onPointerMove:(Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBoardBackgroundColor)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down  = awaitFirstDown(requireUnconsumed = false)
                    onDragStart(down.position)
                    down.consume()
                    drag(down.id) {
                        onDrag(it.position)
                        onPointerMove(it.position.x, it.position.y)
                        it.consume()
                    }
                    onDragEnd()
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if(event.type != PointerEventType.Move) continue
                        val position = event.changes.first().position
                        onPointerMove(position.x, position.y)
                    }
                }
            }
    ){
        state.paths.forEach { path ->
            drawStroke(path)
        }
        state.currentPath?.let { drawStroke(it) }
        state.cursors.values.forEach { drawCursor(it,textMeasurer) }
    }
}

private fun DrawScope.drawCursor(
    cursor: CursorState,
    textMeasurer: TextMeasurer
) {
    val center = Offset(cursor.x, cursor.y)
    drawCircle(color = cursor.color, radius = 8.dp.toPx(), center = center)
    drawCircle(
        color = Color.White,
        radius = 9.dp.toPx(),
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )
    drawText(
        textMeasurer = textMeasurer,
        text = cursor.displayName,
        topLeft = Offset(cursor.x + 12f, cursor.y - 12f),
        style = TextStyle(color = cursor.color, fontSize = 12.sp)
    )
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