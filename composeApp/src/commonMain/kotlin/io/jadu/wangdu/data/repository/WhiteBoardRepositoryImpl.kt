package io.jadu.wangdu.data.repository

import io.jadu.wangdu.data.dto.DrawEvent
import io.jadu.wangdu.data.mapper.toEvent
import io.jadu.wangdu.data.mapper.toPath
import io.jadu.wangdu.data.remote.WhiteboardJson
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

    private val _incomingStrokes = MutableSharedFlow<DrawPath>(extraBufferCapacity = 64)
    override val incomingStrokes: SharedFlow<DrawPath> = _incomingStrokes.asSharedFlow()

    private val _lastReceivedMessage = MutableStateFlow("")
    override val lastReceivedMessage: StateFlow<String> = _lastReceivedMessage.asStateFlow()

    private var session: DefaultClientWebSocketSession? = null
    override suspend fun connect(host: String, port: Int) {
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
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        receiveStroke(frame.readText())
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

    private suspend fun receiveStroke(text: String) {
        println("Received: $text")
        _lastReceivedMessage.value = text

        val event = try {
            WhiteboardJson.decodeFromString(DrawEvent.serializer(), text)
        } catch (e: SerializationException) {
            println("Ignoring malformed frame: $text — ${e.message}")
            return
        }

        if (event.points.isEmpty()) {
            println("Ignoring stroke with no points from ${event.userId}")
            return
        }
        _incomingStrokes.emit(event.toPath())
    }

    override suspend fun send(path: DrawPath, userId: String) {
        val activeSession = session
        if (activeSession == null || _connectionState.value !is ConnectionState.Connected) {
            println("Warning: cannot send stroke, no active session")
            return
        }
        val event = path.toEvent(userId)
        val json = WhiteboardJson.encodeToString(DrawEvent.serializer(), event)
        println("Sending DrawEvent: $json")
        activeSession.send(Frame.Text(json))
    }

    override suspend fun sendPing() {
        val activeSession = session
        if (activeSession == null || _connectionState.value !is ConnectionState.Connected) {
            println("Warning: cannot send ping, no active session")
            return
        }
        activeSession.send(Frame.Text("boom boom"))
        println("Ping sent")
    }

}