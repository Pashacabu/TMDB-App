package com.pashcabu.hw2.model.data_classes.networkResponses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenresResponse(
	@SerialName("genres")
	val genres: List<GenresListItem?>? = null

)

@Serializable
data class GenresListItem(
	@SerialName("name")
	var name: String? = null,
	@SerialName("id")
	var id: Int? = null
)

