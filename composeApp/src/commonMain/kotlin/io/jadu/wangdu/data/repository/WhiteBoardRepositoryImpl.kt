package io.jadu.wangdu.data.repository

import io.jadu.shared.WhiteBoardEvent
import io.jadu.wangdu.data.mapper.toStrokeDrawn
import io.jadu.wangdu.domain.model.ConnectionState
import io.jadu.wangdu.domain.model.DrawPath
import io.jadu.wangdu.domain.repository.WhiteBoardRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerializationException

class WhiteBoardRepositoryImpl(
    private val client: HttpClient,
) : WhiteBoardRepository {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingEvents = MutableSharedFlow<WhiteBoardEvent>(extraBufferCapacity = 64)
    override val incomingEvents: SharedFlow<WhiteBoardEvent> = _incomingEvents.asSharedFlow()

    private var session: DefaultClientWebSocketSession? = null
    override suspend fun connect(host: String, port: Int, userId: String, displayName: String) {
        if (_connectionState.value is ConnectionState.Connecting ||
            _connectionState.value is ConnectionState.Connected
        ) {
            return
        }
        _connectionState.value = ConnectionState.Connecting
        try {
            client.webSocket(host = host, port = port, path = "/whiteboard") {
                session = this
                _connectionState.value = ConnectionState.Connected
                sendEvent(WhiteBoardEvent.UserJoined(userId = userId, displayName = displayName))
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val event = try {
                            io.jadu.shared.WhiteboardJson.decodeFromString(WhiteBoardEvent.serializer(), frame.readText())
                        } catch (e: SerializationException) {
                            continue
                        }
                        _incomingEvents.emit(event)
                    }
                }
            }
        } catch (e: Throwable) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
        } finally {
            session = null
            // If we errored, keep the error visible; otherwise mark disconnected.
            if (_connectionState.value !is ConnectionState.Error) {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }


    override suspend fun sendStroke(path: DrawPath, userId: String) {
        sendEvent(path.toStrokeDrawn(userId))
    }

    override suspend fun sendBoardCleared(userId: String) {
        sendEvent(WhiteBoardEvent.BoardCleared(userId))
    }
     private suspend fun sendEvent(event: WhiteBoardEvent) {
        val activeSession = session
        if (activeSession == null || _connectionState.value !is ConnectionState.Connected) {
            println("Warning: cannot send stroke, no active session")
            return
        }
        val json = io.jadu.shared.WhiteboardJson.encodeToString(WhiteBoardEvent.serializer(), event)
         println(">>> WS send: $json")
        activeSession.send(Frame.Text(json))
    }

}