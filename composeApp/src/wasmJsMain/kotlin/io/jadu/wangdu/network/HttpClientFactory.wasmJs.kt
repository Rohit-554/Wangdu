package io.jadu.wangdu.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

actual fun httpClient(): HttpClient = HttpClient(Js) {
    configureCommonClient()
}