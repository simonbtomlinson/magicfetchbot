package com.simonbtomlinson.magicfetchbot.cardloading.scryfall

import com.fasterxml.jackson.annotation.JsonProperty

data class ImageUris(
		@JsonProperty("png") val png: String?,
		@JsonProperty("border_crop") val borderCrop: String?,
		@JsonProperty("art_crop") val artCrop: String?,
		@JsonProperty("large") val large: String?,
		@JsonProperty("normal") val normal: String?,
		@JsonProperty("small") val small: String?
) {
	fun bestUri(): String? {
		return png ?: large ?: normal ?: small
	}
}
data class ScryfallPrinting(
		@JsonProperty("name") val name: String,
        @JsonProperty("image_uris") val imageUris: ImageUris,
        @JsonProperty("set") val setCode: String
)