package com.simonbtomlinson.magicfetchbot.cardloading.scryfall

import com.fasterxml.jackson.databind.ObjectMapper
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

private interface ScryfallApi {
	@GET("cards/search")
	fun search(@Query("q") searchString: String): Call<ScryfallList<ScryfallPrinting>>

	@GET
	fun paginatePrintings(@Url paginationUrl: String): Call<ScryfallList<ScryfallPrinting>>

	@GET("sets")
	fun allSets(): Call<ScryfallList<ScryfallSet>>

	@GET
	fun paginateSets(@Url paginationUrl: String): Call<ScryfallList<ScryfallSet>>
}

class ScryfallRetriever(objectMapper: ObjectMapper) {
	private val api: ScryfallApi

	private var lastRateLimitTime: Long = 0

	init {
		val retrofit = Retrofit.Builder()
				.baseUrl("https://api.scryfall.com")
				.addConverterFactory(JacksonConverterFactory.create(objectMapper))
				.build()
		api = retrofit.create(ScryfallApi::class.java)
	}

	/**
	 * The Scryfall API requires a delay of at least 50-100ms between requests.
	 * This method waits until at least 100ms after the last time it was called.
	 */
	private fun rateLimit() {
		val currentTime = System.currentTimeMillis()
		val timeSinceLastRequest = currentTime - lastRateLimitTime
		if (timeSinceLastRequest < 100) {
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