package io.jadu.wangdu.database

import org.jetbrains.exposed.sql.Table

object StrokesTable : Table("strokes") {
    val id = integer("id").autoIncrement()
    val userId = varchar("user_id", 64)
    val pointsJson = text("points_json")
    val color = long("color")
    val strokeWidth = float("stroke_width")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}