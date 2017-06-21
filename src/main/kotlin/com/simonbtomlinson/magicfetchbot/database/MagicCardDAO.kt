package com.simonbtomlinson.magicfetchbot.database

import java.sql.Connection

private const val call_bulk_load_cards = "{ call bulk_load_cards(?) }"

class MagicCardDAO(private val connectionManager: ConnectionManager) {

	private fun transformCardsForBulkLoad(conn: Connection, cards: Collection<MagicCard>): SqlArray {
		val names = cards.map { it.name }.toTypedArray()
		return conn.createArrayOf("text", names)
	}

	fun insertMagicCards(cards: Collection<MagicCard>) {
		connectionManager.withConnection { conn ->
			val names = transformCardsForBulkLoad(conn, cards)
			val bulkLoad = conn.prepareCall(call_bulk_load_cards)
			bulkLoad.setArray(1, names)
			bulkLoad.execute()
			bulkLoad.close()
		}
	}

}