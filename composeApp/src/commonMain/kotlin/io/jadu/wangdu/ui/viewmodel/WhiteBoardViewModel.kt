package io.jadu.wangdu.ui.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.jadu.shared.WhiteBoardEvent
import io.jadu.wangdu.data.mapper.toPath
import io.jadu.wangdu.domain.model.ConnectionState
import io.jadu.wangdu.domain.model.CursorState
import io.jadu.wangdu.domain.model.DrawPath
import io.jadu.wangdu.domain.model.DrawingTool
import io.jadu.wangdu.domain.model.WhiteBoardState
import io.jadu.wangdu.domain.repository.WhiteBoardRepository
import io.jadu.wangdu.ui.theme.WhiteBoardBackgroundColor
import io.jadu.wangdu.utils.colorFromUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
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
    private var displayName: String = ""
    private var drawingPoints = mutableListOf<Offset>()

    val selfId : String get() = userId
    private var lastCursorMark: TimeMark? = null

    init {
        observeIncomingEvents()
        observePresence()
    }

    fun selectTool(tool: DrawingTool) {
        _state.update { it.copy(activeTool = tool) }
    }

    private fun observeIncomingEvents() {
        viewModelScope.launch {
            repository.incomingEvents.collect { event ->
                when (event) {
                    is WhiteBoardEvent.StrokeDrawn -> handleStrokeDrawn(event)
                    is WhiteBoardEvent.BoardCleared -> handleBoardCleared()
                    is WhiteBoardEvent.UserJoined -> handleUserJoined(event)
                    is WhiteBoardEvent.CursorMoved -> handleCursorMoved(event)
                    is WhiteBoardEvent.UserLeft -> handleUserLeft(event)
                    is WhiteBoardEvent.RoasterSync -> handleRoasterSync(event)
                }
            }
        }
    }

    private fun handleRoasterSync(event: WhiteBoardEvent.RoasterSync) {
        _state.update { state->
            state.copy(
                connectedUsers = state.connectedUsers + event.users.associate { it.userID to it.displayName }
            )
        }
    }

    private fun observePresence(){
        viewModelScope.launch {
            connectionState.collect { state->
                if(state is ConnectionState.Connected) addSelfToPresence()
            }
        }
    }

    private fun addSelfToPresence(){
        _state.update { it.copy(connectedUsers = it.connectedUsers + (userId to displayName)) }
    }

    private fun handleCursorMoved(event: WhiteBoardEvent.CursorMoved) {
        if(event.userId == userId) return
        val cursor = CursorState(event.x, event.y, event.displayName, colorFromUserId(event.userId))
        _state.update { it.copy(cursors = it.cursors + (event.userId to cursor)) }
    }

    private fun handleUserLeft(event: WhiteBoardEvent.UserLeft) {
        _state.update {
            it.copy(
                cursors = it.cursors - event.userId,
                connectedUsers = it.connectedUsers - event.userId
            )
        }
    }

    private fun handleUserJoined(event: WhiteBoardEvent.UserJoined) {
        _state.update {
            it.copy(
                connectedUsers = it.connectedUsers + (event.userId to event.displayName)
            )
        }
    }
    fun connect(host: String, port: Int, secure: Boolean, displayName: String) {
        this.displayName = displayName
        viewModelScope.launch { repository.connect(host, port, secure, userId, displayName) }
    }

    fun onDragStart(offset: Offset) {
        drawingPoints = mutableListOf(offset)
        val (color,width) = activeStrokeStyle()
        _state.update { currentState ->
            currentState.copy(
                currentPath = DrawPath(
                    points = listOf(offset),
                    color = color,
                    strokeWidth = width
                )
            )
        }
    }

    private fun activeStrokeStyle() : Pair<Color, Float> =
        when(val tool = _state.value.activeTool) {
            is DrawingTool.Pen -> tool.color to tool.width
            is DrawingTool.Eraser -> WhiteBoardBackgroundColor to tool.width
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
        val(color,width) = activeStrokeStyle()
        val path = DrawPath(points = points, color = color, strokeWidth = width)
        if(connectionState.value is ConnectionState.Connected) {
            viewModelScope.launch { repository.sendStroke(path, userId) }
        } else {
            println("Stroke drawn offline, not sent to server")
        }

        _state.update { it.copy(paths = it.paths + path) }

    }

    fun onPointerMove(x: Float, y: Float){
        if(isWithinThrottleInterval()) return
        lastCursorMark = TimeSource.Monotonic.markNow()
        viewModelScope.launch { repository.sendCursor(x,y,userId, displayName) }
    }

    private fun isWithinThrottleInterval(): Boolean {
        val mark = lastCursorMark ?: return false
        return mark.elapsedNow() < CursorThrottleInterval
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
        const val MAX_PATHS = 500
        val CursorThrottleInterval = 50.milliseconds
    }
}


