package io.jadu.wangdu.ui.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.jadu.shared.WhiteBoardEvent
import io.jadu.wangdu.data.mapper.toPath
import io.jadu.wangdu.domain.model.ConnectionState
import io.jadu.wangdu.domain.model.DrawPath
import io.jadu.wangdu.domain.model.WhiteBoardState
import io.jadu.wangdu.domain.repository.WhiteBoardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class WhiteBoardViewModel(
    private val repository: WhiteBoardRepository
) : ViewModel() {
    private val _state = MutableStateFlow(WhiteBoardState())
    val state: StateFlow<WhiteBoardState> = _state.asStateFlow()

    @OptIn(ExperimentalUuidApi::class)
    private val userId : String = Uuid.random().toString()

    val connectionState = repository.connectionState
    private val displayName: String = "User ${userId.take(4)}"
    private var drawingPoints = mutableListOf<Offset>()

    init {
        viewModelScope.launch {
            repository.incomingEvents.collect { event ->
                when (event) {
                    is WhiteBoardEvent.StrokeDrawn -> handleStrokeDrawn(event)
                    is WhiteBoardEvent.BoardCleared -> handleBoardCleared()
                    is WhiteBoardEvent.UserJoined ->
                        println("User joined the board: ${event.displayName} (${event.userId})")
                }
            }
        }
    }

    fun connect(host: String, port: Int) {
        viewModelScope.launch { repository.connect(host, port, userId, displayName) }
    }

    fun onDragStart(offset: Offset) {
        drawingPoints = mutableListOf(offset)
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
        drawingPoints.add(offest)
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
        val points = drawingPoints.toList()
        drawingPoints = mutableListOf()
        _state.update { it.copy(currentPath = null) }
        if (points.isEmpty()) return
        val path = DrawPath(points = points, color = DefaultStrokeColor, strokeWidth = DEFAULTSTROKEWIDTH)
        if(connectionState.value is ConnectionState.Connected) {
            viewModelScope.launch { repository.sendStroke(path, userId) }
        } else {
            println("Stroke drawn offline, not sent to server")
        }

        _state.update { it.copy(paths = it.paths + path) }

    }

    fun clearBoard() {
        viewModelScope.launch { repository.sendBoardCleared(userId) }
    }

    private fun handleStrokeDrawn(event: WhiteBoardEvent.StrokeDrawn) {
        if(event.userId == userId) return
        val receivedPath = event.toPath() ?: return
        _state.update { currentState ->
            val base = if (currentState.paths.size >= MAX_PATHS) {
                currentState.paths.takeLast(MAX_PATHS - 1)
            } else {
                currentState.paths
            }
            currentState.copy(paths = base + receivedPath)
        }
    }

    private fun handleBoardCleared() {
        _state.update { it.copy(paths = emptyList(), currentPath = null) }
    }

    private companion object {
        val DefaultStrokeColor = Color.Black
        const val DEFAULTSTROKEWIDTH = 8f
        const val MAX_PATHS = 500
    }
}


