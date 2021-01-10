package com.pashcabu.hw2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Cast(
	@SerialName("cast")
	val castList: List<CastItem?>? = null,
//	val id: Int? = null,
//	val crew: List<CrewItem?>? = null
)

//data class CrewItem(
//	val gender: Int? = null,
//	val creditId: String? = null,
//	val knownForDepartment: String? = null,
//	val originalName: String? = null,
//	val popularity: Double? = null,
//	val name: String? = null,
//	val profilePath: Any? = null,
//	val id: Int? = null,
//	val adult: Boolean? = null,
//	val department: String? = null,
//	val job: String? = null
//)

@Serializable
data class CastItem(
	val castId: Int? = null,
	@SerialName("character")
	val character: String? = null,
//	val gender: Int? = null,
//	val creditId: String? = null,
//	val knownForDepartment: String? = null,
//	val originalName: String? = null,
//	val popularity: Double? = null,
	@SerialName("name")
	val actorName: String? = null,
	@SerialName("profile_path")
	val actorPhoto: String? = null,
//	val id: Int? = null,
//	val adult: Boolean? = null,
//	val order: Int? = null
)

