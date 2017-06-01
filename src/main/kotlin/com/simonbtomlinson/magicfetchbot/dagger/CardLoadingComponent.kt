package com.simonbtomlinson.magicfetchbot.dagger

import com.simonbtomlinson.magicfetchbot.cardloading.scryfall.ScryfallRetriever
import dagger.Component


@CardLoadingScope
@Component(modules = arrayOf(CardLoadingModule::class), dependencies = arrayOf(CommonComponent::class))
interface CardLoadingComponent {
	fun scryfallRetriever(): ScryfallRetriever
}