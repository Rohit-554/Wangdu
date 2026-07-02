package io.jadu.wangdu.session

import io.jadu.shared.RoasterEntry
import io.jadu.wangdu.model.WhiteBoardSession
import io.ktor.websocket.Frame
import java.util.concurrent.ConcurrentHashMap

class WhiteBoardSessionRegistry {
    private val sessions = ConcurrentHashMap<String, WhiteBoardSession>()

    val activeCount : Int get() = sessions.size

    fun register(session: WhiteBoardSession) {
        sessions[session.userId] = session

    }
    fun unregister(userId: String) {
        sessions.remove(userId)
    }

    suspend fun broadCast(message: String, excludeUserId: String) {
        sessions.values.forEach { peer->
            if(peer.userId == excludeUserId) return@forEach
            sendQuietly(peer,message)
        }
    }
    fun roaster(excludeUserId: String) : List<RoasterEntry> =
        sessions.values
            .filter { it.userId!=excludeUserId }
            .map { RoasterEntry(it.userId, it.displayName) }

    private suspend fun sendQuietly(peer: WhiteBoardSession, message: String){
        try {
            peer.session.send(Frame.Text(message))
        }catch (e: Throwable) {
            println("Failed To send ${peer.userId} : ${e.message}")
        }
    }
}