package io.jadu.shared

import kotlinx.serialization.json.Json


val WhiteboardJson = Json {
    ignoreUnknownKeys  = true
    isLenient = false
}