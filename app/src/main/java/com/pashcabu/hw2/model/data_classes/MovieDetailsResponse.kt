package com.pashcabu.hw2.model.data_classes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailsResponse(
//	val originalLanguage: String? = null,
//	val imdbId: String? = null,
//	val video: Boolean? = null,
	@SerialName("title")
	val movieTitle: String? = null,
	@SerialName("backdrop_path")
	val backdropPath: String? = null,
//	val revenue: Int? = null,
	@SerialName("genres")
	val genres: List<GenresItem?>? = null,
//	val popularity: Double? = null,
//	val productionCountries: List<ProductionCountriesItem?>? = null,
	@SerialName("id")
	val movieId: Int? = null,
	@SerialName("vote_count")
	val reviews: Int? = null,
//	val budget: Int? = null,
	@SerialName("overview")
	val overview: String? = null,
//	val originalTitle: String? = null,
	@SerialName("runtime")
	val runtime: Int? = null,
	@SerialName("posterPath")
	val posterPath: String? = null,
//	val spokenLanguages: List<SpokenLanguagesItem?>? = null,
//	val productionCompanies: List<ProductionCompaniesItem?>? = null,
//	val releaseDate: String? = null,
	@SerialName("vote_average")
	val voteAverage: Double? = null,
//	val belongsToCollection: Any? = null,
//	val tagline: String? = null,
	@SerialName("adult")
	val adult: Boolean? = null,
//	val homepage: String? = null,
//	val status: String? = null
)

data class SpokenLanguagesItem(
	val name: String? = null,
	val iso6391: String? = null,
	val englishName: String? = null
)

data class ProductionCountriesItem(
	val iso31661: String? = null,
	val name: String? = null
)

data class ProductionCompaniesItem(
	val logoPath: String? = null,
	val name: String? = null,
	val id: Int? = null,
	val originCountry: String? = null
)

@Serializable
data class GenresItem(
	@SerialName("name")
	val genreName: String? = null,
	@SerialName("id")
	val genreId: Int? = null
)

