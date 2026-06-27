package io.jadu.wangdu.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PointData(val x: Float, val y: Float)

@Serializable
data class DrawEvent(
    val userId: String,
    val points: List<PointData>,
    val color: Int,
    val strokeWidth: Float
)