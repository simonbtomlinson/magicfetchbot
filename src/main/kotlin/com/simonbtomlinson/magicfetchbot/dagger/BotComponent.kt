package com.simonbtomlinson.magicfetchbot.dagger

import com.simonbtomlinson.magicfetchbot.bot.UpdateHandler
import com.simonbtomlinson.magicfetchbot.database.DatabaseComponent
import com.simonbtomlinson.telegram.api.client.TelegramClient
import dagger.Component

@BotScope
@Component(
		modules = arrayOf(BotModule::class),
		dependencies = arrayOf(DatabaseComponent::class)
)
interface BotComponent {
	fun telegramClient(): TelegramClient

	fun updateHandler(): UpdateHandler
}