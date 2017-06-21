package com.simonbtomlinson.magicfetchbot.database

import java.sql.Connection


private const val call_bulk_load_sets = "{ call bulk_load_sets(?, ?, ?) }"

class MagicSetDAO(private val connectionManager: ConnectionManager) {

	fun createTable() {
	}

	private fun transformSetsForBulkLoad(conn: Connection, sets: Collection<MagicSet>): Triple<SqlArray, SqlArray, SqlArray> {
		val names = mutableListOf<String>()
		val codes = mutableListOf<String>()
		val dates = mutableListOf<SqlDate?>()
		for ((name, code, releaseDate) in sets) {
			names.add(name)
			codes.add(code)
			dates.add(releaseDate?.let { SqlDate(it.time) })
		}
		return Triple(
				conn.createArrayOf("text", names.toTypedArray()),
				conn.createArrayOf("text", codes.toTypedArray()),
				conn.createArrayOf("date", dates.toTypedArray())
		)
	}

	fun insertMagicSets(sets: Collection<MagicSet>) {
		connectionManager.withConnection { conn ->
			val (names, codes, dates) = transformSetsForBulkLoad(conn, sets)
			val bulkLoad = conn.prepareCall(call_bulk_load_sets)
			bulkLoad.setArray(1, names)
			bulkLoad.setArray(2, codes)
			bulkLoad.setArray(3, dates)
			bulkLoad.execute()
			bulkLoad.close()
		}
	}
}