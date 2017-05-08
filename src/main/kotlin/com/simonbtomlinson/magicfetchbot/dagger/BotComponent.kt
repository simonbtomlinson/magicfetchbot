package com.simonbtomlinson.magicfetchbot.dagger

import com.simonbtomlinson.telegram.api.client.TelegramClient
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(BotModule::class))
interface BotComponent {
	fun telegramClient(): TelegramClient
}