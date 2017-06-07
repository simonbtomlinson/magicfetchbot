package com.simonbtomlinson.magicfetchbot.cardloading.scryfall

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.mock
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import retrofit2.Call
import retrofit2.Response

inline fun <reified T> mockCallReturning(value: T): Call<T> {
	val response = mock<Response<T>> {
		on { body() }.thenReturn(value)
	}
	val call = mock<Call<T>> {
		on { execute() }.thenReturn(response)
	}
	return call
}

object ScryfallSpec : Spek({
	given("A ScryfallRetriever") {
		on("retreiveCardsForSet") {
			it("retrieves a list of cards without pagination") {
				val printings = listOf<ScryfallPrinting>()
				val scryfallList = ScryfallList(
						data = printings,
						hasMore = false,
						nextPage = null
				)
				val call = mockCallReturning(scryfallList)
				val api = mock<ScryfallApi> {
					on { search("set:test") }.thenReturn(call)
				}
				val retriever = ScryfallRetriever(api)
				assert.that(retriever.retrieveCardsForSet("test"), equalTo(printings))
			}
			it("paginates correctly") {
				val printing1 = mock<ScryfallPrinting>()
				val firstPage = ScryfallList(
						data = listOf(printing1),
						hasMore = true,
						nextPage = "nextPage"
				)
				val printing2 = mock<ScryfallPrinting>()
				val secondPage = ScryfallList(data = listOf(printing2), hasMore = false, nextPage = null)
				val firstCall = mockCallReturning(firstPage)
				val secondCall = mockCallReturning(secondPage)
				val api = mock<ScryfallApi> {
					on { search("set:test") }.thenReturn(firstCall)
					on { paginatePrintings("nextPage") }.thenReturn(secondCall)
				}
				val retriever = ScryfallRetriever(api, rateLimitDelay = 0)
				assert.that(retriever.retrieveCardsForSet("test"), equalTo(listOf(printing1, printing2)))
			}
		}
	}
})