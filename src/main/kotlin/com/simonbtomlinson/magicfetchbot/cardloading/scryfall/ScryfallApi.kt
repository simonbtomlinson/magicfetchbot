package com.simonbtomlinson.magicfetchbot.cardloading.scryfall

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url


interface ScryfallApi {
	@GET("cards/search")
	fun search(@Query("q") searchString: String): Call<ScryfallList<ScryfallPrinting>>

	@GET
	fun paginatePrintings(@Url paginationUrl: String): Call<ScryfallList<ScryfallPrinting>>

	@GET("sets")
	fun allSets(): Call<ScryfallList<ScryfallSet>>

	@GET
	fun paginateSets(@Url paginationUrl: String): Call<ScryfallList<ScryfallSet>>
}