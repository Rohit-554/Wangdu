package io.jadu.wangdu

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respondText
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

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
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        webSocket("/whiteboard") {
            try {
                for (frame in incoming) {
                    if(frame is Frame.Text){
                        val text = frame.readText()
                        send(Frame.Text(text))
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                application.log.info("Client disconnected")
            } catch (e: Throwable) {
                application.log.error("WebSocket error : ${e.message}", e)
            }
        }
    }
}