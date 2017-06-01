package com.simonbtomlinson.magicfetchbot.cardloading.scryfall

import com.fasterxml.jackson.annotation.JsonProperty


data class ScryfallList<out T>(
		@JsonProperty("data") val data: List<T>,
		@JsonProperty("has_more") val hasMore: Boolean,
        @JsonProperty("next_page") val nextPage: String?
)