package com.pashcabu.hw2.model.data_classes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenresResponse(
	@SerialName("genres")
	val genres: List<GenresItem?>? = null

)

@Serializable
data class GenresListItem(
	@SerialName("name")
	val name: String? = null,
	@SerialName("id")
	val id: Int? = null
)

