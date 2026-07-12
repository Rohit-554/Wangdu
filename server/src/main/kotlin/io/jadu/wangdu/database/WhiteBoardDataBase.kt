package io.jadu.wangdu.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object WhiteBoardDataBase {
    fun connect() {
        val dbPath = System.getenv("DB_PATH") ?: "whiteboard.db"
        Database.connect(
            "jdbc:sqlite:$dbPath",
            "org.sqlite.JDBC"
        )
        transaction {
            SchemaUtils.createMissingTablesAndColumns(StrokesTable)
        }
        println("Connection Successfull")
    }
}