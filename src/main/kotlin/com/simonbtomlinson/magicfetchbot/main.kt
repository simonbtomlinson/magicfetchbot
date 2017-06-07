package com.simonbtomlinson.magicfetchbot

import com.simonbtomlinson.magicfetchbot.dagger.BotModule
import com.simonbtomlinson.magicfetchbot.dagger.CommonModule
import com.simonbtomlinson.magicfetchbot.dagger.DaggerBotComponent
import com.simonbtomlinson.magicfetchbot.dagger.DaggerCommonComponent
import com.simonbtomlinson.telegram.api.types.method.GetMeMethod
import spark.Spark.*

private fun findConfigurationVariable(name: String): String {
	val variable: String = System.getProperty(name) ?: System.getenv(name) ?: throw IllegalStateException("Property $name is not set!")
	return variable
}

fun main(args: Array<String>) {
	staticFiles.location("public")
	ipAddress(args[0])
	port(args[1].toInt())
	val apiKey = findConfigurationVariable("TELEGRAM_API_KEY")
	val commonComponent = DaggerCommonComponent.builder().commonModule(CommonModule()).build()
	val botComponent = DaggerBotComponent.builder()
			.commonComponent(commonComponent).
			botModule(BotModule(apiKey)).
			build()
	val client = botComponent.telegramClient()

	get("/test") { _, _ ->
		client.getMe(GetMeMethod())
	}
}