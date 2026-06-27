package io.jadu.wangdu.domain.repository

import io.jadu.wangdu.domain.model.ConnectionState
import io.jadu.wangdu.domain.model.DrawPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WhiteBoardRepository {
    val connectionState : StateFlow<ConnectionState>
    val incomingStrokes: Flow<DrawPath>
    val lastReceivedMessage: StateFlow<String>
    suspend fun connect(host: String, port: Int)
    suspend fun send(path: DrawPath, userId: String)
    suspend fun sendPing()
}