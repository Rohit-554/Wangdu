package io.jadu.wangdu.model

import io.ktor.websocket.DefaultWebSocketSession

data class WhiteBoardSession(val userId: String, val session: DefaultWebSocketSession)