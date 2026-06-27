package io.jadu.shared

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PointData(val x: Float, val y: Float)

@Serializable
sealed class WhiteBoardEvent {
    @Serializable
    @SerialName("stroke_drawn")
    data class StrokeDrawn(
        val userId: String,
        val points: List<PointData>,
        val color: Int,
        val strokeWidth: Float
    ) : WhiteBoardEvent()

    @Serializable
    @SerialName("board_cleared")
    data class BoardCleared(
        val userId: String
    ) : WhiteBoardEvent()

    @Serializable
    @SerialName("user_joined")
    data class UserJoined(
        val userId: String,
        val displayName: String
    ) : WhiteBoardEvent()

}