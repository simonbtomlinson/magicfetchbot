package com.simonbtomlinson.magicfetchbot.dagger

import com.fasterxml.jackson.databind.ObjectMapper
import com.simonbtomlinson.telegram.api.client.RetrofitTelegramClient
import com.simonbtomlinson.telegram.api.client.TelegramClient
import dagger.Module
import dagger.Provides
import javax.inject.Named


@Module
class BotModule(private val apiKey: String) {

	@Provides @BotScope @Named("apiKey")
	fun provideApiKey(): String = apiKey

	@Provides @BotScope
	fun provideTelegramClient(mapper: ObjectMapper, @Named("apiKey") apiKey: String): TelegramClient {
		return RetrofitTelegramClient(apiKey, mapper)
	}
}