package com.simonbtomlinson.magicfetchbot.bot

import com.fasterxml.jackson.databind.ObjectMapper
import com.simonbtomlinson.magicfetchbot.dagger.BotScope
import com.simonbtomlinson.magicfetchbot.database.SearchCriteria
import com.simonbtomlinson.magicfetchbot.database.SearchProvider
import com.simonbtomlinson.telegram.api.client.TelegramClient
import com.simonbtomlinson.telegram.api.types.InlineQuery
import com.simonbtomlinson.telegram.api.types.inline.result.InlineQueryResultPhoto
import com.simonbtomlinson.telegram.api.types.method.AnswerInlineQueryMethod
import org.slf4j.LoggerFactory
import javax.inject.Inject

@BotScope
class InlineQueryHandler @Inject constructor(
		private val tgClient: TelegramClient,
		private val searchProvider: SearchProvider,
		private val objectMapper: ObjectMapper
) {
	private val logger = LoggerFactory.getLogger(InlineQueryHandler::class.java)

	fun handleInlineQuery(inlineQuery: InlineQuery) {
		logger.info("Update from user ${inlineQuery.sender.id} with text ${inlineQuery.query}")
		val queryParts = inlineQuery.query.split("|")
		val cardName = queryParts.getOrNull(0)
		val setCode = queryParts.getOrNull(1)
		val searchCriteria = SearchCriteria(nameStartsWith = cardName, setCode = setCode)
		val imageURIs = searchProvider.searchForCards(searchCriteria)
		logger.info("Responding with ${imageURIs.size} uris: \n " + imageURIs.joinToString("\n"))

		val answerInlineQueryObject = AnswerInlineQueryMethod(
				inlineQueryId = inlineQuery.id,
				results = imageURIs.map { InlineQueryResultPhoto(id = it.scryfallID.toString(), photoUrl = it.imageUri, thumbUrl = it.imageUri) }.toTypedArray()
		)
		logger.info("Prepared response: " + objectMapper.writeValueAsString(answerInlineQueryObject))
		tgClient.answerInlineQuery(answerInlineQueryObject)
	}
}