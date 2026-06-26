package io.jadu.wangdu

import io.jadu.shared.WhiteBoardEvent
import io.jadu.shared.WhiteboardJson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.util.Collections
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    val connections: MutableSet<DefaultWebSocketSession> =
        Collections.synchronizedSet(LinkedHashSet())

    install(WebSockets){
        pingPeriod = 15.seconds
        timeout = 15.seconds
    }

    install(CORS){
        allowHost("localhost:8080")
        allowHost("localhost:8081")
        allowHost("localhost:5173")
        allowHost("127.0.0.1:8080")
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Options)
    }

    routing {

        get("/") {

        }

        webSocket("/whiteboard") {
            connections.add(this)
            try {
                for (frame in incoming) {
                    if(frame is Frame.Text){
                        val text = frame.readText()
                        val event = try {
                            WhiteboardJson.decodeFromString(WhiteBoardEvent.serializer(), text)
                        } catch (e: Exception) {
                            application.log.error("Failed to deserialize .. $text", e)
                            continue
                        }
                        parseEvent(event)
                        connections.forEach { session ->
                            try {
                                session.send(Frame.Text(text))
                            } catch (e: Exception){
                                application.log.warn("Failed to send to session")
                            }
                        }
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                application.log.info("Client disconnected")
            } catch (e: Throwable) {
                application.log.error("WebSocket error : ${e.message}", e)
            } finally {
                connections.remove(this)
            }
        }
    }
}

fun parseEvent(event: WhiteBoardEvent) {
    when(event) {
        is WhiteBoardEvent.BoardCleared -> {}
        is WhiteBoardEvent.StrokeDrawn -> {}
        is WhiteBoardEvent.UserJoined -> {}
    }
}