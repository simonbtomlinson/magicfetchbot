package com.simonbtomlinson.magicfetchbot.bot

import com.simonbtomlinson.magicfetchbot.dagger.BotScope
import com.simonbtomlinson.telegram.api.types.Update
import org.slf4j.LoggerFactory
import javax.inject.Inject

@BotScope
class UpdateHandler @Inject constructor(
		private val inlineQueryHandler: InlineQueryHandler,
		private val messageHandler: MessageHandler
) {
	private val logger = LoggerFactory.getLogger(UpdateHandler::class.java)

	fun handleUpdate(update: Update) {
		when(update.type) {
			Update.Type.INLINE_QUERY -> inlineQueryHandler.handleInlineQuery(update.inlineQuery!!)
			Update.Type.MESSAGE -> messageHandler.handleMessage(update.message!!)
			else -> {
				logger.info("Unhandled update of type ${update.type}")
			}
		}
	}
}