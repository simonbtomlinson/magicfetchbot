package com.simonbtomlinson.magicfetchbot.cardloading.scryfall

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*


data class ScryfallSet(
		@JsonProperty("name") val name: String,
        @JsonProperty("code") val code: String,
        @JsonProperty("released_at") val release_timestamp: Date? = null
)