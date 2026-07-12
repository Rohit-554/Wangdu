package io.jadu.wangdu

import io.jadu.wangdu.database.StrokeStore
import io.jadu.wangdu.database.WhiteBoardDataBase
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
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    WhiteBoardDataBase.connect()

    val registry = WhiteBoardSessionRegistry()
    val strokeStore = StrokeStore()
    install(WebSockets){
        pingPeriod = 15.seconds
        timeout = 15.seconds
    }

    install(CORS){
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Options)
    }

    routing {

        get("/") {

        }

        webSocket("/whiteboard") {
            WhiteBoardConnection(this, registry, strokeStore = strokeStore).handle()
        }
    }
}