package com.simonbtomlinson.magicfetchbot.database

import java.util.*


data class MagicPrinting(
		val scryfallID: UUID,
		val cardName: String,
        val setCode: String,
        val imageURI: String
)