package com.simonbtomlinson.magicfetchbot.database

private const val search_for_cards = """SELECT DISTINCT ON (mc.name)
	mc.name, mp.image_uri
FROM
	magic_card mc
	INNER JOIN magic_printing mp ON mc.id = mp.card_id
	INNER JOIN magic_set ms ON mp.set_id = ms.id
WHERE lower(mc.name) LIKE ?
ORDER BY
	mc.name,
	CASE WHEN lower(ms.code) = ? THEN 1 ELSE 0 END DESC,
	ms.release_date DESC,
	mp.image_uri DESC
LIMIT ?;"""
class SearchProvider(private val connectionManager: ConnectionManager) {

	fun searchForCards(criteria: SearchCriteria, numToFetch: Int = 10): List<String> {
		val fetchedNames = mutableListOf<String>()
		connectionManager.withConnection { conn ->
			conn.prepareStatement(search_for_cards).use { statement ->
				statement.setString(1, (criteria.nameStartsWith?.toLowerCase() ?: "") + "%")
				statement.setString(2, (criteria.setCode?.toLowerCase() ?: ""))
				statement.setInt(3, numToFetch)
				statement.executeQuery().use { resultSet ->
					while (resultSet.next()) {
						fetchedNames.add(resultSet.getString("image_uri"))
					}
				}
			}
		}
		return fetchedNames
	}

}