package com.pashcabu.hw2.model


import android.util.Log
import com.pashcabu.hw2.model.data_classes.Database
import com.pashcabu.hw2.model.data_classes.networkResponses.CastResponse
import com.pashcabu.hw2.model.data_classes.networkResponses.GenresListItem
import com.pashcabu.hw2.model.data_classes.networkResponses.Movie
import com.pashcabu.hw2.model.data_classes.networkResponses.MovieDetailsResponse
import com.pashcabu.hw2.view_model.*


class DBHandler(private val database: Database) {
    private val converter = ClassConverter()

    suspend fun loadGenresFromDB(): MutableMap<Int?, String?> {
        val output: MutableMap<Int?, String?> = mutableMapOf()
        val listOfGenres = database.genresDAO().getGenres()
        output.putAll(listOfGenres.associateBy({ it.id }, { it.name }))
        return output
    }

    suspend fun loadMoviesListFromDB(endpoint: String?): List<Movie?>? {
        var listOfMovies: List<Movie?>? = null
        when (endpoint) {
            NOW_PLAYING -> {
                val listOfEntities = database.movieDAO().getNowPlaying()
                listOfMovies = converter.entityItemsListToMovieList(listOfEntities)
            }
            POPULAR -> {
                val listOfEntities = database.movieDAO().getPopular()
                listOfMovies = converter.entityItemsListToMovieList(listOfEntities)
            }
            TOP_RATED -> {
                val listOfEntities = database.movieDAO().getTopRated()
                listOfMovies = converter.entityItemsListToMovieList(listOfEntities)
            }
            UPCOMING -> {
                val listOfEntities = database.movieDAO().getUpcoming()
                listOfMovies = converter.entityItemsListToMovieList(listOfEntities)
            }
            FAVOURITE -> {
                val listOfEntities = database.movieDAO().getListOfFavourite()
                listOfMovies = converter.entityItemsListToMovieList(listOfEntities)
            }
        }
        return listOfMovies
    }

    suspend fun addToFavourite(movie: Movie) {
        database.movieDAO().addToFavourite(converter.movieToEntityItem(movie))
    }

    suspend fun addToFavourite(_movie: MovieDetailsResponse) {
        val movie = converter.movieToEntityItem(converter.detailsResponseToMovie(_movie))
        Log.d("DBH", "Movie poster path is ${movie.posterPath}")
        database.movieDAO().addToFavourite(movie)
    }

    suspend fun updateTable(endpoint: String?, movie: Movie) {
        when (endpoint) {
            NOW_PLAYING -> movie.id?.let {
                database.movieDAO().updateNowPlaying(it, movie.addedToFavourite)
            }
            POPULAR -> movie.id?.let {
                database.movieDAO().updatePopular(it, movie.addedToFavourite)
            }
            TOP_RATED -> movie.id?.let {
                database.movieDAO().updateTopRated(it, movie.addedToFavourite)
            }
            UPCOMING -> movie.id?.let {
                database.movieDAO().updateUpcoming(it, movie.addedToFavourite)
            }
        }
    }

    suspend fun deleteFromFavourite(movie: Movie) {
        movie.id?.let { database.movieDAO().deleteFromFavourite(it) }
    }

    suspend fun deleteFromFavourite(_movie: MovieDetailsResponse) {
        val movie = converter.movieDetailsResponseToEntity(_movie)
        movie.movieId?.let { database.movieDAO().deleteFromFavourite(it) }
    }

    suspend fun saveMoviesListToDB(endpoint: String?, list: List<Movie?>?) {
        val listOfEntities = list?.let { converter.movieListToEntityList(it) }
        if (listOfEntities != null) {
            when (endpoint) {
                NOW_PLAYING -> {
                    database.movieDAO().insertNowPlaying(listOfEntities)
                }
                POPULAR -> {
                    database.movieDAO().insertPopular(listOfEntities)
                }
                TOP_RATED -> {
                    database.movieDAO().insertTopRated(listOfEntities)
                }
                UPCOMING -> {
                    database.movieDAO().insertUpcoming(listOfEntities)
                }
            }
        }
    }

    suspend fun clearDBTable(endpoint: String?) {
        Log.d("DBHandler", "ClearDBTable")
        when (endpoint) {
            NOW_PLAYING -> {
                database.movieDAO().deleteNowPlaying()
            }
            POPULAR -> {
                database.movieDAO().deletePopular()
            }
            UPCOMING -> {
                database.movieDAO().deleteUpcoming()
            }
            TOP_RATED -> {
                database.movieDAO().deleteTopRated()
            }
        }
    }

    suspend fun saveGenresToDB(list: List<GenresListItem?>) {
        val listToSave = converter.genresListRespToEntity(list)
        database.genresDAO().insertGenres(listToSave)
    }

    suspend fun loadMovieDataFromDB(id: Int): MovieDetailsResponse {
        return converter.movieDetailsEntityToResponse(database.detailsDAO().getMovieDetails(id))
    }

    suspend fun loadCastDataFromDB(id: Int): CastResponse {
        val cast = CastResponse()
        cast.castList =
            converter.castEntityListToResponseList(database.detailsDAO().getCast(id))
        cast.crew = converter.crewEntityListToResponseList(database.detailsDAO().getCrew(id))
        return cast
    }

    suspend fun saveMovieDetails(movie: MovieDetailsResponse) {
        val movieToSave = converter.movieDetailsResponseToEntity(movie)
        database.detailsDAO().insertMovieDetails(movieToSave)
    }

    suspend fun saveCastDetails(cast: CastResponse, id: Int) {
        val castToSave = cast.castList?.let { converter.castResponseListToEntityList(it) }
        val crewToSave = cast.crew?.let { converter.crewResponseListToEntityList(it) }
        castToSave?.forEach { it.movieId = id }
        crewToSave?.forEach { it.movieId = id }
        if (castToSave != null) {
            database.detailsDAO().insertCast(castToSave)
        }
        if (crewToSave != null) {
            database.detailsDAO().insertCrew(crewToSave)
        }
    }

    suspend fun checkIfInFavourite(id: Int): Boolean {
        val list = database.movieDAO().getOneFromFavourite(id)
        return when {
            list.isNullOrEmpty() -> false
            else -> true
        }
    }

}