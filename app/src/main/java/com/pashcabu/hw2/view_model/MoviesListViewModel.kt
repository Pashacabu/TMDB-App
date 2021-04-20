package com.pashcabu.hw2.view_model


import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.pashcabu.hw2.model.MyWorker
import com.pashcabu.hw2.model.ClassConverter
import com.pashcabu.hw2.model.DBHandler
import com.pashcabu.hw2.model.NetworkModule
import com.pashcabu.hw2.model.NetworkModule.Companion.api_key
import com.pashcabu.hw2.model.data_classes.*
import com.pashcabu.hw2.model.data_classes.networkResponses.GenresListItem
import com.pashcabu.hw2.model.data_classes.networkResponses.Movie
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.Exception

class MoviesListViewModel(private val database: Database, private val worker: WorkManager) :
    ViewModel() {

    private val mutableMoviesList = MutableLiveData<List<Movie?>>(emptyList())
    private val mutableAmountOfPages = MutableLiveData<Int>()
    private val mutableLoadingState: MutableLiveData<Boolean> = MutableLiveData(false)
    private val mutableErrorState: MutableLiveData<String> = MutableLiveData(NO_ERROR)
    private val mutablePageStepBack = MutableLiveData(0)
    private val mutableConnectionState = MutableLiveData<Boolean>()


    val moviesList: LiveData<List<Movie?>> get() = mutableMoviesList
    val amountOfPages: LiveData<Int> get() = mutableAmountOfPages
    val loadingState: LiveData<Boolean> get() = mutableLoadingState
    val errorState: LiveData<String> get() = mutableErrorState
    val pageStepBack: LiveData<Int> get() = mutablePageStepBack

    private var genresAll: MutableMap<Int?, String?> = mutableMapOf()
    private val loadedList: MutableList<Movie?> = mutableListOf()

    private val network: NetworkModule.TMDBInterface = NetworkModule().apiService
    private val converter = ClassConverter()
    private val dbHandler = DBHandler(database)

    private val constraints = Constraints.Builder()
        .setRequiresCharging(true)
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    private val constraints2 = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    private val periodicWorkRequest =
        PeriodicWorkRequestBuilder<MyWorker>(8, TimeUnit.HOURS, 30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

    private val singleWorkRequest = OneTimeWorkRequest.Builder(MyWorker::class.java)
        .setInitialDelay(5L, TimeUnit.SECONDS)
        .setConstraints(constraints2)
        .build()

    private fun startWorkManager() {
        worker.enqueueUniquePeriodicWork(
            "DB Background Periodic Update",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    fun loadLiveData(endpoint: String?, currentPage: Int) {
        if (endpoint == NOW_PLAYING) {
            startWorkManager()
        }

        if (mutableMoviesList.value.isNullOrEmpty()) {
            if (endpoint == FAVOURITE) {
                viewModelScope.launch {
                    loadFromDB(endpoint)
                }
            } else {
                viewModelScope.launch {
                    var time = 0
                    while (mutableConnectionState.value == null) {
                        delay(10) //mutableConnectionState.value changes to actual with a delay, waiting for network status
                        time += 10
                        if (time >= 30) break
                    }
                    if (mutableConnectionState.value == true) {
                        loadFromAPI(endpoint, currentPage)
                    } else {
                        loadFromDB(endpoint)
                    }
                }
            }
        }
    }

    private suspend fun loadFromDB(endpoint: String?): Boolean {
        mutableLoadingState.value = true
        var result = false
        genresAll = dbHandler.loadGenresFromDB()
        val movies = dbHandler.loadMoviesListFromDB(endpoint)
        val listOfFavourite = converter.entityItemsListToMovieList(
            database.movieDAO().getListOfFavourite()
        )
        movies?.forEach { checkIfInFavourite(it, listOfFavourite) }
        loadedList.addAll(movies ?: listOf())
        if (endpoint != FAVOURITE) {
            if (genresAll.isNotEmpty() && !movies.isNullOrEmpty()) {
                mutableMoviesList.value = movies!!
                result = true
            } else {
                mutableErrorState.value = "No data in movies DB!"
            }
        } else {
            mutableMoviesList.value = movies ?: listOf()
        }
        mutableLoadingState.value = false
        return result
    }

    private suspend fun loadFromAPI(endpoint: String?, currentPage: Int) {
        loadGenresFromAPI()
        loadMoviesFromAPI(endpoint, currentPage)
    }

    fun onLikedButtonPressed(endpoint: String?, movie: Movie) {
        viewModelScope.launch {
            if (!movie.addedToFavourite) {
                movie.addedToFavourite = true
                dbHandler.addToFavourite(endpoint, movie)
                loadedList.forEach {
                    if (it?.id == movie.id) {
                        it?.addedToFavourite = true
                    }
                    mutableMoviesList.value = loadedList
                }
            } else {
                movie.addedToFavourite = false
                if (endpoint == FAVOURITE) {
                    movie.id?.let {
                        dbHandler.deleteFromFavourite(endpoint, movie)
                        loadFromDB(endpoint)
                    }
                }
                dbHandler.deleteFromFavourite(endpoint, movie)
                loadedList.forEach {
                    if (it?.id == movie.id) {
                        it?.addedToFavourite = false
                    }
                }
            }
        }

    }

    fun refreshMovieList(endpoint: String?, currentPage: Int) {
        if (endpoint == FAVOURITE) {
            viewModelScope.launch {
                mutableLoadingState.value = true
                loadFromDB(endpoint)
                mutableLoadingState.value = false
            }
        } else {
            viewModelScope.launch {
                if (mutableConnectionState.value == true) {
                    try {
                        loadedList.clear()
                        dbHandler.clearDBTable(endpoint)
                        loadFromAPI(endpoint, currentPage)
                    } catch (e: Exception) {
                        showError()
                    }
                } else {
                    loadFromDB(endpoint)
                }
            }
        }
    }

    private fun showError() {
        mutableErrorState.value = "Connection error!"
    }

    private fun hideError() {
        mutableErrorState.value = NO_ERROR
    }

    private suspend fun loadGenresFromAPI() {
        val list: List<GenresListItem?>
        try {
            mutableLoadingState.value = true
            list = network.getGenres(api_key).genres ?: listOf()
            genresAll.putAll(list.associateBy({ it?.id }, { it?.name }))
            dbHandler.saveGenresToDB(list)
            hideError()
            mutableLoadingState.value = false
        } catch (e: Exception) {
            e.printStackTrace()
            mutableLoadingState.value = false
            showError()
        }
    }

    private suspend fun loadMoviesFromAPI(endpoint: String?, pageToLoad: Int) {
        try {
            mutableLoadingState.postValue(true)
            val newListOfMovies = network.getMoviesList(
                endpoint,
                api_key,
                pageToLoad
            )
            mutableAmountOfPages.value = newListOfMovies.totalPages ?: 0
            var newListWithGenres =
                newListOfMovies.results?.map {
                    converter.genresIntToStrings(
                        it,
                        genresAll
                    )
                }!!
            val listOfFavourite =
                converter.entityItemsListToMovieList(
                    database.movieDAO().getListOfFavourite()
                )
            newListWithGenres = updateBasedOnListOfFavourite(newListWithGenres, listOfFavourite)
            dbHandler.saveMoviesListToDB(endpoint, newListWithGenres)
            loadedList.addAll(newListWithGenres)
            hideError()
            mutableMoviesList.postValue(loadedList)
            mutableLoadingState.postValue(false)
            delay(7000)
            Log.d("VM", "changing title")
            loadedList[1]?.title = "????????????"
//            loadedList[0]?.id = loadedList[0]?.id?.plus(1)
            val newList = loadedList.subList(1,4)
            Log.d("VM", "changing list")
            mutableMoviesList.postValue(newList)
//            mutableMoviesList.postValue(listOf())
        } catch (e: Exception) {
            e.printStackTrace()
            mutableLoadingState.postValue(false)
            showError()
        }
    }

    private fun updateBasedOnListOfFavourite( list : List<Movie?>, listOfFavourite: List<Movie?>) : List<Movie?>{
        list.forEach { movieInCurrent ->
            val found = listOfFavourite.filter { it?.id == movieInCurrent?.id }
            movieInCurrent?.addedToFavourite = !found.isNullOrEmpty()
        }
        return list
    }

    private fun checkIfInFavourite(movie: Movie?, listOfFavourite: List<Movie?>): Movie? {
        val id = movie?.id
        val found = listOfFavourite.filter { it?.id == id }
        Log.d("checkIfInFav", found.size.toString())
        movie?.addedToFavourite = !found.isNullOrEmpty()
        return movie
    }

    fun updateIfInFavourite(endpoint: String?) {
        viewModelScope.launch {
            val listOfFavourite = converter.entityItemsListToMovieList(
                database.movieDAO().getListOfFavourite()
            )
//            loadedList.forEach { checkIfInFavourite(it, listOfFavourite)
//                if (it != null) {
//                    dbHandler.updateTable(endpoint, it)
//                }
//            }
////            Log.d("VM", listOfFavourite.size.toString())
//            if (loadedList.isNotEmpty()) {
//                loadedList.forEach {
//                    checkIfInFavourite(it, listOfFavourite)
//                    if (it != null) {
//                        dbHandler.updateTable(endpoint, it)
//                    }
//                }
//            }
            val list = updateBasedOnListOfFavourite(loadedList, listOfFavourite)
            list.forEach {
                if (it != null) {
                    dbHandler.updateTable(endpoint, it)
                }
            }
//            loadedList.clear()
//            loadedList.addAll(list)
            mutableMoviesList.postValue(list)
//            loadedList.clear()
//            loadedList.addAll(list)
        }
    }

    fun loadMore(endpoint: String?, pageToLoad: Int) {
        if (endpoint != FAVOURITE) {
            viewModelScope.launch {
                loadMoviesFromAPI(endpoint, pageToLoad)
                if (mutableErrorState.value != NO_ERROR) {
                    mutablePageStepBack.value = -1
                    mutablePageStepBack.value = 0
                }
            }
        }
    }

    fun setConnectionState(connected: Boolean) {
        mutableConnectionState.value = connected
    }
}


const val NOW_PLAYING = "now_playing"
const val POPULAR = "popular"
const val TOP_RATED = "top_rated"
const val UPCOMING = "upcoming"
const val FAVOURITE = "favourite"
const val NO_ERROR = "No Errors"


