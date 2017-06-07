package com.simonbtomlinson.magicfetchbot.database

import java.util.*

data class MagicSet(
		val name: String,
        val code: String,
        val releaseDate: Date?
)