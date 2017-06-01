package com.simonbtomlinson.magicfetchbot.dagger

import com.fasterxml.jackson.databind.ObjectMapper
import com.simonbtomlinson.magicfetchbot.cardloading.scryfall.ScryfallRetriever
import dagger.Module
import dagger.Provides


@Module
class CardLoadingModule {
	@Provides @CardLoadingScope
	fun provideScryfallRetriever(mapper: ObjectMapper): ScryfallRetriever {
		return ScryfallRetriever(mapper)
	}
}