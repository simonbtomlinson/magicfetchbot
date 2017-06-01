package com.simonbtomlinson.magicfetchbot.dagger

import com.simonbtomlinson.telegram.api.client.TelegramClient
import dagger.Component
import dagger.Subcomponent
import javax.inject.Singleton

@BotScope
@Component(modules = arrayOf(BotModule::class), dependencies = arrayOf(CommonComponent::class))
interface BotComponent {
	fun telegramClient(): TelegramClient
}