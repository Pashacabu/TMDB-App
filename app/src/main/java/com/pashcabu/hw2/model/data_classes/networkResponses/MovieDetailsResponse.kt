package com.pashcabu.hw2.model.data_classes.networkResponses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailsResponse(
    var originalLanguage: String? = null,
    var imdbId: String? = null,
    var video: Boolean? = null,
    @SerialName("title")
    var movieTitle: String? = null,
    @SerialName("backdrop_path")
    var backdropPath: String? = null,
    var revenue: Int? = null,
    @SerialName("genres")
    var genres: List<GenresItem?>? = null,
    var popularity: Double? = null,
    var productionCountries: List<ProductionCountriesItem?>? = null,
    @SerialName("id")
    var movieId: Int? = null,
    @SerialName("vote_count")
    var reviews: Int? = null,
    var budget: Int? = null,
    @SerialName("overview")
    var overview: String? = null,
    var originalTitle: String? = null,
    @SerialName("runtime")
    var runtime: Int? = null,
    @SerialName("posterPath")
    var posterPath: String? = null,
    var spokenLanguages: List<SpokenLanguagesItem?>? = null,
    var productionCompanies: List<ProductionCompaniesItem?>? = null,
    var releaseDate: String? = null,
    @SerialName("vote_average")
    var voteAverage: Double? = null,
    var tagline: String? = null,
    @SerialName("adult")
    var adult: Boolean? = null,
    var homepage: String? = null,
    var status: String? = null
)

@Serializable
data class SpokenLanguagesItem(
    var name: String? = null,
    var iso6391: String? = null,
    var englishName: String? = null
)

@Serializable
data class ProductionCountriesItem(
    var iso31661: String? = null,
    var name: String? = null
)

@Serializable
data class ProductionCompaniesItem(
    var logoPath: String? = null,
    var name: String? = null,
    var id: Int? = null,
    var originCountry: String? = null
)

@Serializable
data class GenresItem(
    @SerialName("name")
    var genreName: String? = null,
    @SerialName("id")
    var genreId: Int? = null
)

