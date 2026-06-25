package io.jadu.wangdu.data.remote

import kotlinx.serialization.json.Json

val WhiteboardJson = Json {
    ignoreUnknownKeys  = true
    isLenient = false
}