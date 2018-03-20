package com.simonbtomlinson.magicfetchbot

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.simonbtomlinson.magicfetchbot.bot.MagicSetLoader
import com.simonbtomlinson.magicfetchbot.cardloading.scryfall.ScryfallApi
import com.simonbtomlinson.magicfetchbot.cardloading.scryfall.ScryfallRetriever
import com.simonbtomlinson.magicfetchbot.database.DaggerDatabaseComponent
import com.simonbtomlinson.magicfetchbot.database.DatabaseModule
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory


/* End-To-End functional testing for the bot. */

/* These tests assume a postgres database set up on local host with a user
named test_user that has a password of password. They could probably be improved
to make the bot testable in more situations and with less setup,
but this is good enough for now.

I usually start a server with
docker run --rm --name fetchbot-postgres -e POSTGRES_PASSWORD=password -e POSTGRES_USER=test_user -p 5432:5432 postgres
 */

object EndToEndSpec : Spek({

	given("a set-up database") {
		val databaseComponent = DaggerDatabaseComponent.builder()
			.databaseModule(DatabaseModule(
				databaseUser = "test_user",
				databasePassword = "password",
				databaseName = "postgres",
				serverName = "localhost",
				portNumber = 5432,
				maxPoolSize = 1
			))
			.build()

		// TODO: Refactor dagger structure so that these objects can be built more easily.

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
		val setLoader = MagicSetLoader(
				scryfallRetriever = retriever,
				magicSetDAO = databaseComponent.magicSetDAO(),
				magicCardDAO = databaseComponent.magicCardDAO(),
				magicPrintingDAO = databaseComponent.magicPrintingDAO()
		)

		beforeEachTest {
			databaseComponent.databaseManager().createSchema()
		}

		afterEachTest {
			databaseComponent.databaseManager().dropSchema()
		}

		on("a duplicate load of the same set") {
			setLoader.loadSetFromCode("dst") // Darksteel is a pretty small set at 165 cards
			setLoader.loadSetFromCode("dst")
			it("Only loads printings once") {
				databaseComponent.connectionManager().withConnection { conn ->
					val statement = conn.createStatement()
					val resultSet = statement.executeQuery("SELECT COUNT(*) AS num_rows FROM magic_printing")
					resultSet.next()
					val numRows = resultSet.getInt("num_rows")
					assert.that(numRows, equalTo(165))
					resultSet.close()
					statement.close()
				}

			}
			it("Only loads cards once") {
				databaseComponent.connectionManager().withConnection { conn ->
					val statement = conn.createStatement()
					val resultSet = statement.executeQuery("SELECT COUNT(*) AS num_rows from magic_card")
					resultSet.next()
					val numRows = resultSet.getInt("num_rows")
					assert.that(numRows, equalTo(165))
					resultSet.close()
					statement.close()
				}
			}

			it("Only loads the set itself once") {
				databaseComponent.connectionManager().withConnection { conn ->
					val statement = conn.createStatement()
					val resultSet = statement.executeQuery("SELECT COUNT(*) AS num_rows from magic_set")
					resultSet.next()
					val numRows = resultSet.getInt("num_rows")
					assert.that(numRows, equalTo(1))
					resultSet.close()
					statement.close()
				}
			}
		}
	}

})
