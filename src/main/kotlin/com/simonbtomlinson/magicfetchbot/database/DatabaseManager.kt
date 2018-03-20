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

	fun dropSchema() {
		connectionManager.withConnection { conn ->
			val statement = conn.createStatement()
			// This should drop EVERYTHING. It doesn't work if current_user is the default postgres user,
			// but that shouldn't happen.
			statement.execute("DROP OWNED BY current_user;"	)
			statement.close()
		}
	}
}