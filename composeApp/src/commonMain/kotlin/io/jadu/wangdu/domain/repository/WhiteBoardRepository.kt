package io.jadu.wangdu.domain.repository

import io.jadu.shared.WhiteBoardEvent
import io.jadu.wangdu.domain.model.ConnectionState
import io.jadu.wangdu.domain.model.DrawPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WhiteBoardRepository {
    val connectionState : StateFlow<ConnectionState>
    val incomingEvents: Flow<WhiteBoardEvent>
    suspend fun connect(host: String, port: Int, userId: String, displayName: String)
    suspend fun sendStroke(path: DrawPath, userId: String)
    suspend fun sendBoardCleared(userId: String)
}