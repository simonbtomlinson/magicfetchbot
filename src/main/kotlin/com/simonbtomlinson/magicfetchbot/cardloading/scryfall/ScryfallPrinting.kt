package com.simonbtomlinson.magicfetchbot.cardloading.scryfall

import com.fasterxml.jackson.annotation.JsonProperty


data class ScryfallPrinting(
		@JsonProperty("name") val name: String,
        @JsonProperty("image_uri") val imageUri: String,
        @JsonProperty("set") val setCode: String
)