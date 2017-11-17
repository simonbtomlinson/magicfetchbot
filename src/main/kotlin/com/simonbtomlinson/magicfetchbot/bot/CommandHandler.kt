package com.simonbtomlinson.magicfetchbot.bot

import javax.inject.Inject


class CommandHandler @Inject constructor(private val setLoader: MagicSetLoader) {

	fun handleCommand(rawCommandText: String, updateSenderCallback: (String) -> Unit) {
		val commandParts = rawCommandText.split(Regex("\\s+"))
		val command = commandParts[0]
		val args = commandParts.subList(1, commandParts.size)

	}
}