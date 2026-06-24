package io.jadu.wangdu.network

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun HttpClientConfig<*>.configureCommonClient() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true  // Ktor will ignore extraField instead of crashing
                isLenient = true   // API ka JSON thoda imperfect ho, tab bhi parse karne ki try karo.
                prettyPrint = true // This formats JSON nicely when converting Kotlin objects to JSON
            },
        )
    }
    install(Logging) {
        level = LogLevel.ALL
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000 // The whole request must complete within 30 seconds
        connectTimeoutMillis = 15_000  // Give the app 15 seconds to connect to the server
    }
    install(WebSockets)
}