package io.jadu.wangdu.ui.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.jadu.wangdu.model.ConnectionState
import io.jadu.wangdu.model.DrawPath
import io.jadu.wangdu.model.WhiteBoardState
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WhiteBoardViewModel(
    private val client: HttpClient
) : ViewModel() {
    private val _state = MutableStateFlow(WhiteBoardState())
    val state: StateFlow<WhiteBoardState> = _state.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _lastReceivedMessage = MutableStateFlow("")
    val lastReceivedMessage : StateFlow<String> = _lastReceivedMessage.asStateFlow()

    private var session: DefaultClientWebSocketSession? = null

    fun connect(host: String, port: Int){
        if (_connectionState.value is ConnectionState.Connecting ||
            _connectionState.value is ConnectionState.Connected
        ) {
            return
        }

        _connectionState.value = ConnectionState.Connecting
        viewModelScope.launch {
            try {
                client.webSocket(
                    host = host,
                    port = port,
                    path = "/whiteboard"
                ) {
                    session = this
                    _connectionState.value = ConnectionState.Connected
                    for(frame in incoming) {
                        if(frame is Frame.Text) {
                            val text = frame.readText()
                            println("Received :$text")
                            _lastReceivedMessage.value = text
                        }
                    }
                }
            } catch (e : Throwable) {
                _connectionState.value = ConnectionState.Error(e.message ?: "unknown Error")
            } finally {
                session = null
                if(_connectionState.value !is ConnectionState.Error) {
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }
    }

    fun sendPing(){
        val activeSession = session
        if(activeSession == null || _connectionState.value !is ConnectionState.Connected) {
            println("Cannot send ping, no active session is there")
            return
        }
        viewModelScope.launch {
            activeSession.send(Frame.Text("ping"))
            println("Ping sent")
        }
    }

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


