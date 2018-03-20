package com.simonbtomlinson.magicfetchbot.database

import java.sql.Connection
import java.util.*

private const val call_bulk_load_printings = "{ call bulk_load_printings(?, ?, ?, ?) }"

private data class TransformResult(val scryfallIDs: SqlArray, val cardNames: SqlArray, val setCodes: SqlArray, val imageUris: SqlArray)

class MagicPrintingDAO(private val connectionManager: ConnectionManager) {

	private fun transformPrintingsForBulkLoad(conn: Connection, printings: Collection<MagicPrinting>): TransformResult {
		val scryfallIDs = mutableListOf<UUID>()
		val cardNames = mutableListOf<String>()
		val setCodes = mutableListOf<String>()
		val imageURIs = mutableListOf<String>()
		for ((scryfallID, cardName, setCode, imageURI) in printings) {
			scryfallIDs.add(scryfallID)
			cardNames.add(cardName)
			setCodes.add(setCode)
			imageURIs.add(imageURI)
		}
		return TransformResult(
				conn.createArrayOf("uuid", scryfallIDs.toTypedArray()),
				conn.createArrayOf("text", cardNames.toTypedArray()),
				conn.createArrayOf("text", setCodes.toTypedArray()),
				conn.createArrayOf("text", imageURIs.toTypedArray())
		)
	}

	fun insertMagicPrintings(printings: Collection<MagicPrinting>) {
		connectionManager.withConnection { conn ->
			val (scryfallIDs, cardNames, setCodes, imageURIs) = transformPrintingsForBulkLoad(conn, printings)
			val bulkLoad = conn.prepareCall(call_bulk_load_printings)
			bulkLoad.setArray(1, scryfallIDs)
			bulkLoad.setArray(2, cardNames)
			bulkLoad.setArray(3, setCodes)
			bulkLoad.setArray(4, imageURIs)
			bulkLoad.execute()
			bulkLoad.close()
		}
	}

	fun getNumPrintings(): Int {
		connectionManager.withConnection { conn ->
			val statement = conn.createStatement()
			val resultSet = statement.executeQuery("SELECT COUNT(*) AS num_rows FROM magic_printing")
			resultSet.next()
			val numRows = resultSet.getInt("num_rows")
			resultSet.close()
			statement.close()
			return numRows
		}
	}
}