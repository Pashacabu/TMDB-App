package com.pashcabu.hw2.model.data_classes.networkResponses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CastResponse(
    @SerialName("cast")
    var castList: MutableList<CastItem?>? = null,
    @SerialName("id")
    val id: Int? = null,
    @SerialName("crew")
    var crew: MutableList<CrewItem?>? = null
)

@Serializable
data class CrewItem(
    @SerialName("gender")
    var gender: Int? = null,
    @SerialName("credit_id")
    var creditId: String? = null,
    @SerialName("known_for_department")
    var knownForDepartment: String? = null,
    @SerialName("original_name")
    var originalName: String? = null,
    @SerialName("popularity")
    var popularity: Double? = null,
    @SerialName("name")
    var name: String? = null,
    @SerialName("profile_path")
    var profilePath: String? = null,
    @SerialName("id")
    var id: Int? = null,
    @SerialName("adult")
    var adult: Boolean? = null,
    @SerialName("department")
    var department: String? = null,
    @SerialName("job")
    var job: String? = null
)

@Serializable
data class CastItem(
    var castId: Int? = null,
    @SerialName("character")
    var character: String? = null,
    @SerialName("gender")
    var gender: Int? = null,
    @SerialName("credit_id")
    var creditId: String? = null,
    @SerialName("known_for_department")
    var knownForDepartment: String? = null,
    @SerialName("original_name")
    var originalName: String? = null,
    @SerialName("popularity")
    var popularity: Double? = null,
    @SerialName("name")
    var actorName: String? = null,
    @SerialName("profile_path")
    var actorPhoto: String? = null,
    @SerialName("id")
    var id: Int? = null,
    @SerialName("adult")
    var adult: Boolean? = null,
    @SerialName("order")
    var order: Int? = null
)

