package io.jadu.wangdu

import io.jadu.shared.WhiteBoardEvent
import io.jadu.wangdu.session.WhiteBoardConnection
import io.jadu.wangdu.session.WhiteBoardSessionRegistry
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    val registry = WhiteBoardSessionRegistry()

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
            WhiteBoardConnection(this, registry).handle()
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