package com.simonbtomlinson.magicfetchbot.database

//language=PostgreSQL
private const val create_magic_set_table = """
CREATE TABLE magic_set(
	id SERIAL PRIMARY KEY NOT NULL,
	name VARCHAR(50) UNIQUE NOT NULL,
	code VARCHAR(4) UNIQUE NOT NULL,
	release_date DATE NULL -- Not all sets have a release date
);
"""

//language=PostgreSQL
private const val create_magic_set_indexes = """
CREATE UNIQUE INDEX ON magic_set(lower(code));
"""

//language=PostgreSQL
private const val insert_magic_set = """
INSERT INTO magic_set(name, code, release_date) VALUES (?, ?, ?)
ON CONFLICT DO NOTHING;
"""

class MagicSetDAO(private val connectionManager: ConnectionManager) {

	fun createTable() {
		connectionManager.withConnection { conn ->
			val statement = conn.createStatement()
			statement.execute(create_magic_set_table)
			statement.execute(create_magic_set_indexes)
			statement.close()
		}
	}

	fun insertMagicSets(sets: Collection<MagicSet>) {
		connectionManager.withConnection { conn ->
			conn.autoCommit = false
			val statement = conn.prepareStatement(insert_magic_set)
			sets.forEach {
				val releaseDate = if (it.releaseDate == null) null else java.sql.Date(it.releaseDate.time)
				statement.setString(1, it.name)
				statement.setString(2, it.code)
				statement.setDate(3, releaseDate)
				statement.addBatch()
			}
			statement.executeBatch()
			conn.commit()
		}
	}

}