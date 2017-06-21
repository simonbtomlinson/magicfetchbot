package com.simonbtomlinson.magicfetchbot.database

import java.sql.Connection

private const val call_bulk_load_printings = "{ call bulk_load_printings(?, ?, ?) }"
class MagicPrintingDAO(private val connectionManager: ConnectionManager) {

	fun transformPrintingsForBulkLoad(conn: Connection, printings: Collection<MagicPrinting>): Triple<SqlArray, SqlArray, SqlArray> {
		val cardNames = mutableListOf<String>()
		val setCodes = mutableListOf<String>()
		val imageURIs = mutableListOf<String>()
		for ((cardName, setCode, imageURI) in printings) {
			cardNames.add(cardName)
			setCodes.add(setCode)
			imageURIs.add(imageURI)
		}
		return Triple(
				conn.createArrayOf("text", cardNames.toTypedArray()),
				conn.createArrayOf("text", setCodes.toTypedArray()),
				conn.createArrayOf("text", imageURIs.toTypedArray())
		)
	}

	fun insertMagicPrintings(printings: Collection<MagicPrinting>) {
		connectionManager.withConnection { conn ->
			val (cardNames, setCodes, imageURIs) = transformPrintingsForBulkLoad(conn, printings)
			val bulkLoad = conn.prepareCall(call_bulk_load_printings)
			bulkLoad.setArray(1, cardNames)
			bulkLoad.setArray(2, setCodes)
			bulkLoad.setArray(3, imageURIs)
			bulkLoad.execute()
			bulkLoad.close()
		}
	}
}