package io.jadu.wangdu.session

import io.jadu.shared.WhiteBoardEvent
import io.jadu.shared.WhiteboardJson
import io.jadu.wangdu.model.WhiteBoardSession
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.SerializationException

class WhiteBoardConnection (
    private val socket: DefaultWebSocketServerSession,
    private val registry: WhiteBoardSessionRegistry,
) {

    suspend fun handle() {
        val joinRequest = awaitJoin() ?: return
        registry.register(WhiteBoardSession(joinRequest.event.userId, socket))
        announce(joinRequest.event, joinRequest.rawJson,joinRequest.event.userId)
        relayUntilClosed(joinRequest.event.userId)
    }

    private suspend fun awaitJoin(): JoinRequest? {
        val firstFrame = socket.incoming.receiveCatching().getOrNull() as? Frame.Text
            ?: return reject("No Join Message received")
        val rawJson = firstFrame.readText()
        val join = decode(rawJson) as? WhiteBoardEvent.UserJoined
            ?: return reject("First message must be UserJoined")
        return JoinRequest(join, rawJson)
    }

    private suspend fun relayUntilClosed(userId : String) {
        try {
            for (frame in socket.incoming) {
                if (frame is Frame.Text) relay(frame.readText(), userId)
            }
        } catch (e: ClosedReceiveChannelException){

        } catch ( e: Throwable) {

        } finally {
            registry.unregister(userId)
            broadCastUserLeft(userId)
        }
    }

    private suspend fun relay(rawJson: String, senderId: String){
        val event = decode(rawJson) ?: run {
            println("Failed to deserialize whiteboard event $rawJson")
            return
        }
        announce(event, rawJson, senderId)
    }

    private suspend fun broadCastUserLeft(userId: String) {
        val event = WhiteBoardEvent.UserLeft(userId)
        val json = WhiteboardJson.encodeToString(WhiteBoardEvent.serializer(), event)
        registry.broadCast(json, excludeUserId = userId)
    }
    private suspend fun announce(event: WhiteBoardEvent, rawJson: String, excludedUserId: String) {
        registry.broadCast(rawJson, excludedUserId)
    }


    private fun decode(rawJson: String) : WhiteBoardEvent? =
        try {
            WhiteboardJson.decodeFromString(WhiteBoardEvent.serializer(), rawJson)
        }catch (e :  SerializationException) {
            null
        }

    private suspend fun reject(reason: String) : JoinRequest? {
        socket.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, reason))
        return null
    }

    private data class JoinRequest(val event: WhiteBoardEvent.UserJoined, val rawJson: String)
}