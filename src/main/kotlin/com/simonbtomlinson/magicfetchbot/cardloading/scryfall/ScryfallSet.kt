package com.simonbtomlinson.magicfetchbot.cardloading.scryfall

import com.fasterxml.jackson.annotation.JsonProperty


data class ScryfallSet(
		@JsonProperty("name") val name: String,
        @JsonProperty("code") val code: String
)