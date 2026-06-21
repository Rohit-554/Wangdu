package io.jadu.wangdu.ui.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import io.jadu.wangdu.model.DrawPath
import io.jadu.wangdu.model.WhiteBoardState
import io.ktor.client.plugins.sse.SSEBufferPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WhiteBoardViewModel : ViewModel() {
    private val _state = MutableStateFlow(WhiteBoardState())
    val state: StateFlow<WhiteBoardState> = _state.asStateFlow()

    fun onDragStart(offset: Offset) {
        _state.update { currentState ->
            currentState.copy(
                currentPath = DrawPath(
                    points = listOf(offset),
                    color = DefaultStrokeColor,
                    strokeWidth = DEFAULTSTROKEWIDTH
                )
            )
        }
    }

    fun onDrag(offest: Offset) {
        _state.update {currentState ->
            val activePath = currentState.currentPath ?: return@update currentState
            currentState.copy(
                currentPath = activePath.copy(
                    points = activePath.points + offest
                )
            )
        }
    }

    fun onDragEnd() {
        _state.update { currentState->
            val activePath = currentState.currentPath ?: return@update currentState
            currentState.copy(
                paths = currentState.paths + activePath,
                currentPath = null
            )
        }
    }

    fun clearBoard() {
        _state.update { WhiteBoardState() }
    }

    private companion object {
        val DefaultStrokeColor = Color.Black
        const val DEFAULTSTROKEWIDTH = 8f
    }
}


