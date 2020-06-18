package com.simonbtomlinson.magicfetchbot.bot

import com.simonbtomlinson.magicfetchbot.dagger.BotScope
import com.simonbtomlinson.telegram.api.client.TelegramClient
import com.simonbtomlinson.telegram.api.types.Message
import com.simonbtomlinson.telegram.api.types.method.SendMessageMethod
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Named

@BotScope
class MessageHandler @Inject constructor(
		private val telegramClient: TelegramClient,
		@Named("ADMIN_TELEGRAM_IDS") private val adminTelegramIDs: List<Int>,
		private val setLoader: MagicSetLoader
) {

	private val logger = LoggerFactory.getLogger(MessageHandler::class.java)

	internal fun senderIsAuthenticated(message: Message): Boolean {
		val sender = message.from
		return sender != null && adminTelegramIDs.contains(sender.id)
	}

	fun handleMessage(message: Message) {
		if (!senderIsAuthenticated(message)) {
			logger.info("Unauthenticated message sender: ${message.from?.id}")
			return
		}
		if (message.text!! == "all") {
			telegramClient.sendMessage(SendMessageMethod(message.chat.id, "Loading all sets"))
			setLoader.loadAllSets()
			telegramClient.sendMessage(SendMessageMethod(message.chat.id, "Loaded all sets"))
		} else {
			telegramClient.sendMessage(SendMessageMethod(message.chat.id, "Loading set ${message.text!!}"))
			try {
				setLoader.loadSetFromCode(message.text!!)
				telegramClient.sendMessage(SendMessageMethod(message.chat.id, "Loaded set ${message.text!!}"))
			} catch (e: Exception) {
				telegramClient.sendMessage(SendMessageMethod(message.chat.id, "Failed to load"))
			}

		}
	}
}