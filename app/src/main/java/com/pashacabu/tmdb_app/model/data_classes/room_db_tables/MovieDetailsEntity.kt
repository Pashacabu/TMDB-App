package com.pashacabu.tmdb_app.model.data_classes.room_db_tables

import androidx.room.*


@Entity(tableName = "MovieDetails")
data class DBMovieDetails(
    @PrimaryKey
    @ColumnInfo(name = "TMDB_ID")
    var movieId: Int? = null,
    @ColumnInfo(name = "originalLanguage")
    var originalLanguage: String? = null,
    @ColumnInfo(name = "IMDB_ID")
    var imdbId: String? = null,
    @ColumnInfo(name = "video")
    var video: Boolean? = null,
    @ColumnInfo(name = "title")
    var movieTitle: String? = null,
    @ColumnInfo(name = "backdrop")
    var backdropPath: String? = null,
    @ColumnInfo(name = "revenue")
    var revenue: Long? = null,
    @ColumnInfo(name = "genreName")
    var genreName: String? = null,
    @ColumnInfo(name = "genreID")
    var genreId: Int? = null,
    @ColumnInfo(name = "popularity")
    var popularity: Double? = null,
    @ColumnInfo(name = "reviews")
    var reviews: Int? = null,
    @ColumnInfo(name = "budget")
    var budget: Int? = null,
    @ColumnInfo(name = "overview")
    var overview: String? = null,
    @ColumnInfo(name = "origanalTitle")
    var originalTitle: String? = null,
    @ColumnInfo(name = "runtime")
    var runtime: Int? = null,
    @ColumnInfo(name = "poster")
    var posterPath: String? = null,
    @ColumnInfo(name = "releaseDate")
    var releaseDate: String? = null,
    @ColumnInfo(name = "voteAVR")
    var voteAverage: Double? = null,
    @ColumnInfo(name = "tagline")
    var tagline: String? = null,
    @ColumnInfo(name = "adult")
    var adult: Boolean? = null,
    @ColumnInfo(name = "homepage")
    var homepage: String? = null,
    @ColumnInfo(name = "status")
    var status: String? = null
)

@Entity(
    tableName = "CastDetails",
    primaryKeys = ["movieID", "id"]
)
data class DBCastItem(
    @ColumnInfo(name = "movieID")
    var movieId: Int = null ?: 0,
    @ColumnInfo(name = "castID")
    var castId: Int? = null,
    @ColumnInfo(name = "character")
    var character: String? = null,
    @ColumnInfo(name = "gender")
    var gender: Int? = null,
    @ColumnInfo(name = "creditID")
    var creditId: String? = null,
    @ColumnInfo(name = "knownForDepartment")
    var knownForDepartment: String? = null,
    @ColumnInfo(name = "originalName")
    var originalName: String? = null,
    @ColumnInfo(name = "popularity")
    var popularity: Double? = null,
    @ColumnInfo(name = "name")
    var actorName: String? = null,
    @ColumnInfo(name = "photo")
    var actorPhoto: String? = null,
    @ColumnInfo(name = "id")
    var id: Int = null ?: 0,
    @ColumnInfo(name = "adult")
    var adult: Boolean? = null,
    @ColumnInfo(name = "order")
    var order: Int? = null
)

@Entity(
    tableName = "CrewDetails",
    primaryKeys = ["movieID", "id"]
)
data class DBCrewItem(
    @ColumnInfo(name = "movieID")
    var movieId: Int = 0,
    @ColumnInfo(name = "gender")
    var gender: Int? = null,
    @ColumnInfo(name = "creditID")
    var creditId: String? = null,
    @ColumnInfo(name = "knownForDepartment")
    var knownForDepartment: String? = null,
    @ColumnInfo(name = "originalName")
    var originalName: String? = null,
    @ColumnInfo(name = "popularity")
    var popularity: Double? = null,
    @ColumnInfo(name = "name")
    var name: String? = null,
    @ColumnInfo(name = "profile")
    var profilePath: String? = null,
    @ColumnInfo(name = "id")
    var id: Int = 0,
    @ColumnInfo(name = "adult")
    var adult: Boolean? = null,
    @ColumnInfo(name = "department")
    var department: String? = null,
    @ColumnInfo(name = "job")
    var job: String? = null
)

@Entity(tableName = "LatestMovie")
data class DBLatestMovieDetails(
    @PrimaryKey
    @ColumnInfo(name = "TMDB_ID")
    var movieId: Int? = null,
    @ColumnInfo(name = "originalLanguage")
    var originalLanguage: String? = null,
    @ColumnInfo(name = "IMDB_ID")
    var imdbId: String? = null,
    @ColumnInfo(name = "video")
    var video: Boolean? = null,
    @ColumnInfo(name = "title")
    var movieTitle: String? = null,
    @ColumnInfo(name = "backdrop")
    var backdropPath: String? = null,
    @ColumnInfo(name = "revenue")
    var revenue: Int? = null,
    @ColumnInfo(name = "genreName")
    var genreName: String? = null,
    @ColumnInfo(name = "genreID")
    var genreId: Int? = null,
    @ColumnInfo(name = "popularity")
    var popularity: Double? = null,
    @ColumnInfo(name = "reviews")
    var reviews: Int? = null,
    @ColumnInfo(name = "budget")
    var budget: Int? = null,
    @ColumnInfo(name = "overview")
    var overview: String? = null,
    @ColumnInfo(name = "origanalTitle")
    var originalTitle: String? = null,
    @ColumnInfo(name = "runtime")
    var runtime: Int? = null,
    @ColumnInfo(name = "poster")
    var posterPath: String? = null,
    @ColumnInfo(name = "releaseDate")
    var releaseDate: String? = null,
    @ColumnInfo(name = "voteAVR")
    var voteAverage: Double? = null,
    @ColumnInfo(name = "tagline")
    var tagline: String? = null,
    @ColumnInfo(name = "adult")
    var adult: Boolean? = null,
    @ColumnInfo(name = "homepage")
    var homepage: String? = null,
    @ColumnInfo(name = "status")
    var status: String? = null
)


