package io.jadu.wangdu.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object WhiteBoardDataBase {
    fun connect() {
        Database.connect(
            "jdbc:sqlite:whiteboard.db",
            "org.sqlite.JDBC"
        )
        transaction {
            SchemaUtils.createMissingTablesAndColumns(StrokesTable)
        }
        println("Connection Successfull")
    }
}