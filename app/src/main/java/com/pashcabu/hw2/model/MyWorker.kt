package com.pashcabu.hw2.model

import android.content.Context
import android.util.Log
import androidx.work.*
import com.pashcabu.hw2.model.data_classes.Database
import com.pashcabu.hw2.model.data_classes.networkResponses.GenresListItem
import com.pashcabu.hw2.model.data_classes.networkResponses.Movie
import com.pashcabu.hw2.view_model.*
import kotlinx.coroutines.*

class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val db = Database.createDB(context)
    private val network = NetworkModule().apiService
    private val converter = ClassConverter()
    private val dbHandler = DBHandler(db)
    private val array = arrayOf(NOW_PLAYING, POPULAR, TOP_RATED, UPCOMING)


    override fun doWork(): Result {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            Log.d("WORKER", "updating")
            refreshDB()
        }
        return Result.success()
    }


    private suspend fun refreshDB() {
        for (endpoint in array) {
            Log.d("VM", "Clear $endpoint")
            dbHandler.clearDBTable(endpoint)
            Log.d("VM", "load $endpoint")
            loadFromAPI(endpoint, 1)
        }
    }

    private suspend fun loadFromAPI(endpoint: String?, currentPage: Int) {
        loadGenresFromAPI()
        loadMoviesFromAPI(endpoint, currentPage)
    }

    private suspend fun loadGenresFromAPI() {
        val list: List<GenresListItem?>
        try {
            list = network.getGenres(NetworkModule.api_key).genres ?: listOf()
            dbHandler.saveGenresToDB(list)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadMoviesFromAPI(endpoint: String?, pageToLoad: Int) {
        try {
            val newListOfMovies = network.getMoviesList(
                endpoint,
                NetworkModule.api_key,
                pageToLoad
            )
            val mapOfGenres = dbHandler.loadGenresFromDB()
            val newListWithGenres =
                newListOfMovies.results?.map { movie ->
                    converter.genresIntToStrings(
                        movie,
                        mapOfGenres
                    )
                }
            val listOfFavourite =
                converter.entityItemsListToMovieList(
                    db.movieDAO().getListOfFavourite()
                )
            newListWithGenres?.forEach { checkIfInFavourite(it, listOfFavourite) }
            dbHandler.saveMoviesListToDB(endpoint, newListWithGenres)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkIfInFavourite(movie: Movie?, listOfFavourite: List<Movie?>): Movie? {
        val id = movie?.id
        for (item in listOfFavourite) {
            if (item?.id == id) {
                movie?.addedToFavourite = true
            }
        }
        return movie
    }
}
