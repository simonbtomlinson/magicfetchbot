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
			// I should really just be running tests in nested transactions, but at this scale
			// this is slightly easier
			statement.execute("DROP FUNCTION bulk_load_printings(UUID[], TEXT[], TEXT[], TEXT[]);")
			statement.execute("DROP FUNCTION bulk_load_cards(TEXT[])")
			statement.execute("DROP FUNCTION bulk_load_sets(TEXT[], TEXT[], DATE[])")
			statement.execute("DROP TABLE magic_printing")
			statement.execute("DROP TABLE magic_card")
			statement.execute("DROP TABLE magic_set")
			statement.close()
		}
	}
}