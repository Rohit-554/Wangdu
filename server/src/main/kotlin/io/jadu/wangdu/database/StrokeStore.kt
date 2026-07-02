package io.jadu.wangdu.database

import io.jadu.shared.PointData
import io.jadu.shared.WhiteBoardEvent
import io.jadu.shared.WhiteboardJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class StrokeStore() {

    suspend fun save(stroke: WhiteBoardEvent.StrokeDrawn) {
        onDatabase("save stroke") {
            StrokesTable.insert {
                it[userId] = stroke.userId
                it[pointsJson]= encodePoints(stroke.points)
                it[color] = stroke.color
                it[strokeWidth] = stroke.strokeWidth
                it[createdAt] = System.currentTimeMillis()
            }
        }
    }

    suspend fun clear() {
        onDatabase("clear strokes") {
            StrokesTable.deleteAll()
        }
    }

    suspend fun loadInOrder(): List<WhiteBoardEvent.StrokeDrawn> =
        onDatabaseReturning(fallback = emptyList()) {
            StrokesTable.selectAll()
                .orderBy(StrokesTable.createdAt to SortOrder.ASC,
                    StrokesTable.id to SortOrder.ASC)
                .map { it.toStrokeDrawn() }
        }

    private fun ResultRow.toStrokeDrawn() : WhiteBoardEvent.StrokeDrawn =
        WhiteBoardEvent.StrokeDrawn(
            userId = this[StrokesTable.userId],
            points = decodePoints(this[StrokesTable.pointsJson]),
            color = this[StrokesTable.color],
            strokeWidth = this[StrokesTable.strokeWidth],
        )
    private suspend fun <T> onDatabaseReturning(fallback: T, block:() -> T) : T =
        withContext(Dispatchers.IO) {
            try {
                transaction { block() }
            }catch (e: Throwable) {
                println(e)
                fallback
            }
        }
    private suspend fun onDatabase(action: String, block : () -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                transaction { block() }
            } catch (e: Throwable) {
                println(e)
            }
        }
    }

    private fun encodePoints(points: List<PointData>): String =
        WhiteboardJson.encodeToString(ListSerializer(PointData.serializer()), points)

    private fun decodePoints(json: String) : List<PointData> =
        WhiteboardJson.decodeFromString(ListSerializer(PointData.serializer()), json)
}