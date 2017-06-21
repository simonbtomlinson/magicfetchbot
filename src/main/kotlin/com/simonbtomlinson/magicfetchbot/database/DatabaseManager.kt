package com.simonbtomlinson.magicfetchbot.database

import com.simonbtomlinson.magicfetchbot.common.loadResourceAsString

class DatabaseManager(private val connectionManager: ConnectionManager) {
	fun createSchema() {
		connectionManager.withConnection { conn ->
			val statement = conn.createStatement()
			statement.execute(loadResourceAsString("/sql/create_schema.sql"))
			statement.close()
		}
	}
}