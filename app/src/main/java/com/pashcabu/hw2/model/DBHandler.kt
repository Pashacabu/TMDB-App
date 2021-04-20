package com.pashcabu.hw2.model


import android.util.Log
import com.pashcabu.hw2.model.data_classes.Database
import com.pashcabu.hw2.model.data_classes.networkResponses.GenresListItem
import com.pashcabu.hw2.model.data_classes.networkResponses.Movie
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

    suspend fun addToFavourite(endpoint: String?, movie: Movie) {
        database.movieDAO().addToFavourite(converter.movieToEntityItem(movie))
        updateTable(endpoint, movie)
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

    suspend fun deleteFromFavourite(endpoint: String?, movie: Movie) {
        movie.id?.let { database.movieDAO().deleteFromFavourite(it) }
        updateTable(endpoint, movie)
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
}