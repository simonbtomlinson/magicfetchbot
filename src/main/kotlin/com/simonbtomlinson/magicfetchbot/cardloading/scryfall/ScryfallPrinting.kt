package com.simonbtomlinson.magicfetchbot.cardloading.scryfall

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class ImageUris(
		@JsonProperty("png") val png: String?,
		@JsonProperty("border_crop") val borderCrop: String?,
		@JsonProperty("art_crop") val artCrop: String?,
		@JsonProperty("large") val large: String?,
		@JsonProperty("normal") val normal: String?,
		@JsonProperty("small") val small: String?
) {
	fun bestUri(): String? {
		return large ?: normal ?: small // Telegram wants a jpeg. large, normal, and small are jpegs.
	}
}

data class ScryfallCardFace(
	@JsonProperty("name") val name: String,
	@JsonProperty("image_uris") val imageUris: ImageUris?
)

data class ScryfallPrinting(
		@JsonProperty("id") val scryfallID: UUID,
		@JsonProperty("name") val name: String,
        @JsonProperty("image_uris") val imageUris: ImageUris?,
        @JsonProperty("set") val setCode: String,
		@JsonProperty("card_faces") val cardFaces: List<ScryfallCardFace>?
) {
	// The scryfall api specifies that a card is double sided if and only if it has a card_faces list that has faces
	// with associated image_uris.
	fun isDoubleFace(): Boolean = cardFaces?.firstOrNull()?.imageUris != null

	fun bestImageUri(): String? = when {
			isDoubleFace() -> cardFaces!![0].imageUris!!.bestUri()
			else -> imageUris!!.bestUri()
	}
}