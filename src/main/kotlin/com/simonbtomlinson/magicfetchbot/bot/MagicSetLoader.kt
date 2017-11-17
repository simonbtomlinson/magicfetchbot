package com.simonbtomlinson.magicfetchbot.bot

import com.simonbtomlinson.magicfetchbot.cardloading.scryfall.ScryfallRetriever
import com.simonbtomlinson.magicfetchbot.dagger.BotScope
import com.simonbtomlinson.magicfetchbot.database.*
import org.slf4j.LoggerFactory
import javax.inject.Inject
import kotlin.concurrent.thread

@BotScope
class MagicSetLoader @Inject constructor(
		private val scryfallRetriever: ScryfallRetriever,
		private val magicSetDAO: MagicSetDAO,
		private val magicCardDAO: MagicCardDAO,
		private val magicPrintingDAO: MagicPrintingDAO
	) {

	private val logger = LoggerFactory.getLogger(MagicSetLoader::class.java)

	private fun getAllSets(): List<MagicSet> {
		return scryfallRetriever.retrieveSets().map { scryfallSet ->
			MagicSet(name=scryfallSet.name, code=scryfallSet.code, releaseDate = scryfallSet.release_timestamp)
		}
	}

	private fun getSetFromCode(setCode: String): MagicSet {
		return getAllSets().filter { set -> set.code.toLowerCase() == setCode.toLowerCase() }[0]
	}

	private fun loadSet(set: MagicSet) {
		logger.info("Inserting set ${set.name}")
		magicSetDAO.insertMagicSets(listOf(set))
		val rawPrintings = scryfallRetriever.retrieveCardsForSet(set.code)
		val cards = rawPrintings.map { MagicCard(it.name) }.distinct()

		logger.info("Loading ${cards.size} cards")
		magicCardDAO.insertMagicCards(cards)

		val printings = rawPrintings
				.filter { it.imageUris?.bestUri() != null }
				.map { MagicPrinting(it.name, it.setCode, it.imageUris!!.bestUri()!!) }
		logger.info("Loading ${printings.size} printings")
		magicPrintingDAO.insertMagicPrintings(printings)
	}

	fun loadSetFromCode(setCode: String) {
		return loadSet(getSetFromCode(setCode))
	}

	fun loadAllSets() {
		// run in a new thread so the connection can return first.
		thread { getAllSets().forEach(this::loadSet) }
	}
}