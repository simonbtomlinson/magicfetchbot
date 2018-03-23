package com.simonbtomlinson.magicfetchbot.cardloading

import com.fasterxml.jackson.databind.ObjectMapper
import com.simonbtomlinson.magicfetchbot.cardloading.scryfall.ScryfallApi
import com.simonbtomlinson.magicfetchbot.cardloading.scryfall.ScryfallRetriever
import com.simonbtomlinson.magicfetchbot.dagger.CommonComponent
import dagger.Component
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Named
import javax.inject.Scope

@Scope
annotation class CardLoadingScope

@CardLoadingScope
@Component(modules = arrayOf(CardLoadingModule::class), dependencies = arrayOf(CommonComponent::class))
interface CardLoadingComponent {
	fun scryfallRetriever(): ScryfallRetriever
}

@Module
class CardLoadingModule(private val scryfallUrl: String) {

	@Provides @CardLoadingScope @Named("imageUri")
	fun provideScryfallUrl() = scryfallUrl

	@Provides @CardLoadingScope
	fun provideScryfallApi(@Named("imageUri") scryfallUrl: String, mapper: ObjectMapper): ScryfallApi {
		val retrofit = Retrofit.Builder()
				.baseUrl("https://api.scryfall.com")
				.addConverterFactory(JacksonConverterFactory.create(mapper))
				.build()
		return retrofit.create(ScryfallApi::class.java)
	}

	@Provides @CardLoadingScope
	fun provideScryfallRetriever(scryfallApi: ScryfallApi): ScryfallRetriever = ScryfallRetriever(scryfallApi)

}