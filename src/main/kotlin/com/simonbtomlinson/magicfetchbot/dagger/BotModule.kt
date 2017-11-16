package com.simonbtomlinson.magicfetchbot.dagger

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.simonbtomlinson.magicfetchbot.cardloading.CardLoadingScope
import com.simonbtomlinson.magicfetchbot.cardloading.scryfall.ScryfallApi
import com.simonbtomlinson.telegram.api.client.RetrofitTelegramClient
import com.simonbtomlinson.telegram.api.client.TelegramClient
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Named


@Module
class BotModule(private val apiKey: String, private val ownerTelegramId: Int) {

	@Provides @BotScope @Named("apiKey")
	fun provideApiKey(): String = apiKey

	@Provides @BotScope @Named("OWNER_TELEGRAM_ID")
	fun provideOwnerTelegramID(): Int = ownerTelegramId

	@Provides @BotScope @Named("scryfallUrl")
	fun provideScryfallUrl() = "https://api.scryfall.com/"


	@Provides @BotScope
	fun provideObjectMapper(): ObjectMapper {
		return jacksonObjectMapper()
				.findAndRegisterModules()
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
	}

	@Provides @BotScope
	fun provideScryfallApi(@Named("scryfallUrl") scryfallUrl: String, mapper: ObjectMapper): ScryfallApi {
		val retrofit = Retrofit.Builder()
				.baseUrl(scryfallUrl)
				.addConverterFactory(JacksonConverterFactory.create(mapper))
				.build()
		return retrofit.create(ScryfallApi::class.java)
	}

	@Provides @BotScope
	fun provideTelegramClient(mapper: ObjectMapper, @Named("apiKey") apiKey: String): TelegramClient {
		return RetrofitTelegramClient(apiKey, mapper)
	}
}