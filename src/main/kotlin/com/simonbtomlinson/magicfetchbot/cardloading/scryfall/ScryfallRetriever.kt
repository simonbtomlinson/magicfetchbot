package com.simonbtomlinson.magicfetchbot.cardloading.scryfall

class ScryfallRetriever(private val api: ScryfallApi, private val rateLimitDelay: Long = 100) {

	private var lastRateLimitTime: Long = 0

	/**
	 * The Scryfall API requires a delay of at least 50-100ms between requests.
	 * This method waits until at least 100ms after the last time it was called.
	 */
	private fun rateLimit() {
		val currentTime = System.currentTimeMillis()
		val timeSinceLastRequest = currentTime - lastRateLimitTime
		if (timeSinceLastRequest < rateLimitDelay) {
			println("Rate limiting")
			Thread.sleep(100 - timeSinceLastRequest)
		}
		lastRateLimitTime = currentTime
	}

	fun retrieveCardsForSet(setCode: String): List<ScryfallPrinting> {
		rateLimit()
		var response = api.search("set:$setCode").execute().body()
		val printings = response.data.toMutableList()
		while (response.hasMore) {
			rateLimit()
			response = api.paginatePrintings(response.nextPage!!).execute().body()
			printings.addAll(response.data)
		}
		return printings
	}

	fun retrieveSets(): List<ScryfallSet> {
		rateLimit()
		var response = api.allSets().execute().body()
		val allSets = response.data.toMutableList()
		while (response.hasMore) {
			rateLimit()
			response = api.paginateSets(response.nextPage!!).execute().body()
			allSets.addAll(response.data)
		}
		return allSets
	}
}