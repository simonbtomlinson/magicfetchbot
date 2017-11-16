package com.simonbtomlinson.magicfetchbot

import com.fasterxml.jackson.module.kotlin.readValue
import com.simonbtomlinson.magicfetchbot.cardloading.CardLoadingModule
import com.simonbtomlinson.magicfetchbot.cardloading.DaggerCardLoadingComponent
import com.simonbtomlinson.magicfetchbot.dagger.BotModule
import com.simonbtomlinson.magicfetchbot.dagger.CommonModule
import com.simonbtomlinson.magicfetchbot.dagger.DaggerBotComponent
import com.simonbtomlinson.magicfetchbot.dagger.DaggerCommonComponent
import com.simonbtomlinson.magicfetchbot.database.*
import com.simonbtomlinson.telegram.api.types.Update
import com.simonbtomlinson.telegram.api.types.inline.result.InlineQueryResultPhoto
import com.simonbtomlinson.telegram.api.types.method.AnswerInlineQueryMethod
import com.simonbtomlinson.telegram.api.types.method.SendMessageMethod
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
	port(findConfigurationVariable("PORT").toInt())
	val commonComponent = DaggerCommonComponent.builder().commonModule(CommonModule()).build()
	val apiKey = findConfigurationVariable("TELEGRAM_API_KEY")
	val botComponent = DaggerBotComponent.builder()
			.commonComponent(commonComponent)
			.botModule(BotModule(apiKey))
			.build()
	val cardLoadingComponent = DaggerCardLoadingComponent.builder().commonComponent(commonComponent)
			.cardLoadingModule(CardLoadingModule("https://api.scryfall.com/"))
			.build()
	val databaseComponent = DaggerDatabaseComponent.builder()
			.databaseModule(DatabaseModule(
				databaseUser = findConfigurationVariable("POSTGRESQL_USERNAME"),
				databasePassword = findConfigurationVariable("POSTGRESQL_PASSWORD"),
				databaseName = findConfigurationVariable("POSTGRES_FETCHBOT_DB"),
				serverName = findConfigurationVariable("POSTGRESQL_HOST"),
				portNumber = findConfigurationVariable("POSTGRESQL_PORT").toInt(),
				maxPoolSize = findConfigurationVariable("POOL_SIZE").toInt()
			))
			.build()

	val ownerTelegramId = findConfigurationVariable("OWNER_TELEGRAM_ID").toInt()

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

	val tgClient = botComponent.telegramClient()

	fun handleUpdate(update: Update) {
		if (update.type == Update.Type.INLINE_QUERY) {
			val inlineQuery = update.inlineQuery!!
			logger.info("Update from user ${inlineQuery.sender.id} with text ${inlineQuery.query}")
			val queryParts = inlineQuery.query.split("|")
			val cardName = queryParts.getOrNull(0)
			val setCode = queryParts.getOrNull(1)
			val searchCriteria = SearchCriteria(nameStartsWith = cardName, setCode = setCode)
			val imageURIs = databaseComponent.searchProvider().searchForCards(searchCriteria)
			tgClient.answerInlineQuery(AnswerInlineQueryMethod(
					inlineQueryId = inlineQuery.id,
					results = imageURIs.map { InlineQueryResultPhoto(id = it, photoUrl = it, thumbUrl = it) }.toTypedArray()
			))
		} else if (update.type == Update.Type.MESSAGE) {
			val message = update.message!!
			logger.info("Message from user ${message.from?.id}")
			if (message.from?.id == ownerTelegramId) {
				logger.info("User ${message.from?.id} is the owner, replying to their message")
				tgClient.sendMessage(SendMessageMethod(
						chatId = message.chat.id,
						text = "Response!"
				))
			} else {
				logger.info("User ${message.from?.id} is not the owner, ignoring their message")
			}
		}
	}

	post("/webhook") { req, res ->
		val update = commonComponent.objectMapper().readValue<Update>(req.body())
		try {
			handleUpdate(update)
		} catch (e: Exception) {
			logger.error("Error handling update", e)
			res.status(500)
			return@post "Error"
		}
	}

	get("/") { req, res ->
		"Index page!"
	}

}
