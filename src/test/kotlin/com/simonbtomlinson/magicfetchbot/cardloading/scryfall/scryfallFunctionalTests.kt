package com.simonbtomlinson.magicfetchbot.cardloading.scryfall

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.contains
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.present
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

object ScryfallFunctionalTests : Spek({
	given("A ScryfallRetriever") {
		val retrofit = Retrofit.Builder()
					.baseUrl("https://api.scryfall.com")
					.addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()
							.findAndRegisterModules()
							.setSerializationInclusion(JsonInclude.Include.NON_NULL)
							.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false))
					)
					.build()
		val api = retrofit.create(ScryfallApi::class.java)

		val retriever = ScryfallRetriever(api)
		on("a non-mocked request for ISD") {
			val isd = retriever.retrieveCardsForSet("isd")
			val delver = isd.find { it.name.startsWith("Delver of Secrets") }
			assert.that(delver, present()) // Asserts that the delver card is not null
			delver!!
			it("retrieves a 'Delver of Secrets' that has the correct structure") {
				assert.that(delver.cardFaces, present()) { "Delver card's 'cardFaces' property should not be null" }
				delver.cardFaces!!
				assert.that(delver.cardFaces!!.size, equalTo(2))
			}
			it ("retrieves a 'Delver of Secrets' that lists itself as double-sided") {
				assert.that(delver.isDoubleFace(), equalTo(true))
			}
			it ("Chooses a good image uri for 'Delver of Secrets'") {
				assert.that(delver.bestImageUri(), present())
				// The correct url should include front, since it's a double-sided card
				assert.that(delver.bestImageUri()!!, contains("front".toRegex()))
			}
			it ("retrieves a non-double-sided cards and correctly identifies it as not double-sided") {
				val snapcaster = isd.find { it.name == "Snapcaster Mage" }
				assert.that(snapcaster, present())
				snapcaster!!
				assert.that(snapcaster.isDoubleFace(), equalTo(false))
			}
		}
		on("A non-mocked request for Apocalypse (APC)") {
			val apc = retriever.retrieveCardsForSet("apc")
			it("Chooses the correct url for Fire // Ice (a split card)") {
				val fireIce = apc.find { it.name.startsWith("Fire") }
				assert.that(fireIce, present())
				fireIce!!
				assert.that(fireIce.imageUris, present())
				assert.that(fireIce.bestImageUri(), equalTo(fireIce.imageUris!!.bestUri()))
			}
		}
	}
})
