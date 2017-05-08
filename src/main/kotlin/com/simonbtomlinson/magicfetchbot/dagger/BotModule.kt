package com.simonbtomlinson.magicfetchbot.dagger

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.simonbtomlinson.telegram.api.client.RetrofitTelegramClient
import com.simonbtomlinson.telegram.api.client.TelegramClient
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton


@Module
class BotModule(private val apiKey: String) {

	@Provides @Singleton @Named("apiKey")
	fun provideApiKey(): String = apiKey

	@Provides @Singleton
	fun provideObjectMapper(): ObjectMapper {
		return jacksonObjectMapper()
				.findAndRegisterModules()
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
	}

	@Provides @Singleton
	fun provideTelegramClient(mapper: ObjectMapper, @Named("apiKey") apiKey: String): TelegramClient {
		return RetrofitTelegramClient(apiKey, mapper)
	}
}