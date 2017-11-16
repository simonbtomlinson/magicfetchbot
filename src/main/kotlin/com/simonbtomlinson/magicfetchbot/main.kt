package com.simonbtomlinson.magicfetchbot

import com.fasterxml.jackson.module.kotlin.readValue
import com.simonbtomlinson.magicfetchbot.dagger.BotModule
import com.simonbtomlinson.magicfetchbot.dagger.CommonModule
import com.simonbtomlinson.magicfetchbot.dagger.DaggerBotComponent
import com.simonbtomlinson.magicfetchbot.dagger.DaggerCommonComponent
import com.simonbtomlinson.magicfetchbot.database.DaggerDatabaseComponent
import com.simonbtomlinson.magicfetchbot.database.DatabaseModule
import com.simonbtomlinson.telegram.api.types.Update
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
	val ownerTelegramId = findConfigurationVariable("OWNER_TELEGRAM_ID").toInt()

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
	val botComponent = DaggerBotComponent.builder()
			.databaseComponent(databaseComponent)
			.botModule(BotModule(apiKey, ownerTelegramId))
			.build()


	get("/createSchema") { _, _ ->
		databaseComponent.databaseManager().createSchema()
	}

	post("/webhook") { req, res ->
		val update = commonComponent.objectMapper().readValue<Update>(req.body())
		try {
			botComponent.updateHandler().handleUpdate(update)
		} catch (e: Exception) {
			logger.error("Error handling update", e)
			res.status(500)
			return@post "Error"
		}
	}

	get("/") { _, _ ->
		"Index page!"
	}

}
