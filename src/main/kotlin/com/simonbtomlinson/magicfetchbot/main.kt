package com.simonbtomlinson.magicfetchbot

import com.simonbtomlinson.magicfetchbot.cardloading.CardLoadingModule
import com.simonbtomlinson.magicfetchbot.cardloading.DaggerCardLoadingComponent
import com.simonbtomlinson.magicfetchbot.dagger.BotModule
import com.simonbtomlinson.magicfetchbot.dagger.CommonModule
import com.simonbtomlinson.magicfetchbot.dagger.DaggerBotComponent
import com.simonbtomlinson.magicfetchbot.dagger.DaggerCommonComponent
import com.simonbtomlinson.magicfetchbot.database.*
import com.simonbtomlinson.telegram.api.types.method.GetMeMethod
import org.slf4j.LoggerFactory
import spark.Spark.*

private fun findConfigurationVariable(name: String): String {
	val variable: String = System.getProperty(name) ?: System.getenv(name) ?: throw IllegalStateException("Property $name is not set!")
	return variable
}

class Main

fun main(args: Array<String>) {
	val logger = LoggerFactory.getLogger(Main::class.java)
	staticFiles.location("public")
	ipAddress(args[0])
	port(args[1].toInt())
	val commonComponent = DaggerCommonComponent.builder().commonModule(CommonModule()).build()
	val apiKey = findConfigurationVariable("TELEGRAM_API_KEY")
	val botComponent = DaggerBotComponent.builder()
			.commonComponent(commonComponent).
			botModule(BotModule(apiKey)).
			build()
	val cardLoadingComponent = DaggerCardLoadingComponent.builder().commonComponent(commonComponent)
			.cardLoadingModule(CardLoadingModule("https://api.scryfall.com/"))
			.build()
	val databaseComponent = DaggerDatabaseComponent.builder()
			.databaseModule(DatabaseModule(
				databaseUser = "postgres",
				databasePassword = "guest",
				databaseName = "postgres",
				serverName = "localhost",
				portNumber = 5432
			))
			.build()

	get("/createSchema") { _, _ ->
		databaseComponent.databaseManager().createSchema()
	}

	get("/import") { _, _ ->
		val scryfallRetriever = cardLoadingComponent.scryfallRetriever()
		val setsToInsert = scryfallRetriever.retrieveSets().map { scryfallSet ->
			MagicSet(name=scryfallSet.name, code=scryfallSet.code, releaseDate = scryfallSet.release_timestamp)
		}
		logger.info("Loading ${setsToInsert.size} sets")
		databaseComponent.magicSetDAO().insertMagicSets(setsToInsert)
		for (set in setsToInsert) {
			logger.info("Loading cards from ${set.name}")
			val rawPrintings = scryfallRetriever.retrieveCardsForSet(set.code)
			val cards = rawPrintings.map { MagicCard(it.name) }.distinct()
			logger.info("Loading ${cards.size} cards")
			databaseComponent.magicCardDAO().insertMagicCards(cards)
			val printings = rawPrintings.map { MagicPrinting(it.name, it.setCode, it.imageUri) }
			logger.info("Loading ${printings.size} printings")
			databaseComponent.magicPrintingDAO().insertMagicPrintings(printings)
		}
	}
}