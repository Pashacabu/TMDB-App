package com.pashacabu.tmdb_app.model.data_classes.networkResponses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImagesResponse(
    @SerialName("profiles")
    val profiles: List<ProfilesItem?>? = null,
)

@Serializable
data class ProfilesItem(
    @SerialName("aspect_ratio")
    val aspectRatio: Double? = null,
    @SerialName("file_path")
    val filePath: String? = null,
    @SerialName("vote_average")
    val voteAverage: Double? = null,
    @SerialName("width")
    val width: Int? = null,
    @SerialName("iso_639_1")
    val iso6391: String? = null,
    @SerialName("vote_count")
    val voteCount: Int? = null,
    @SerialName("height")
    val height: Int? = null
)

