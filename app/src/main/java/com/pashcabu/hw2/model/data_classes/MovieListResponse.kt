package com.pashcabu.hw2.model.data_classes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieListResponse(
//	val dates: Dates? = null,
//	@SerialName("page")
//	val page: Int? = null,
    @SerialName("total_pages")
    val totalPages: Int? = null,
    @SerialName("results")
    val results: List<ResultsItem?>? = null,
//	val totalResults: Int? = null
)

@Serializable
data class Dates(
    val maximum: String? = null,
    val minimum: String? = null
)

@Serializable
data class ResultsItem(
//	val overview: String? = null,
//	val originalLanguage: String? = null,
//	val originalTitle: String? = null,
//	val video: Boolean? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("genre_ids")
    val genreIds: List<Int?>? = null,
    var genres: List<String?>? = listOf(),
    @SerialName("poster_path")
    val posterPath: String? = null,
//	val backdropPath: String? = null,
//	val releaseDate: String? = null,
//	val popularity: Double? = null,
    @SerialName("vote_average")
    val voteAverage: Double? = null,
    @SerialName("id")
    val id: Int? = null,
    @SerialName("adult")
    val adult: Boolean? = null,
    @SerialName("vote_count")
    val voteCount: Int? = null,
    val addedToFavourite: Boolean = false
)

