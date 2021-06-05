package com.pashcabu.hw2.view_model


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
    private val mutablePageStep = MutableLiveData(1)
    private val mutableConnectionState = MutableLiveData<Boolean>()

    val moviesList: LiveData<List<Movie?>> get() = mutableMoviesList
    val amountOfPages: LiveData<Int> get() = mutableAmountOfPages
    val loadingState: LiveData<Boolean> get() = mutableLoadingState
    val errorState: LiveData<String> get() = mutableErrorState
    val pageStep: LiveData<Int> get() = mutablePageStep

    private var genresAll: MutableMap<Int?, String?> = mutableMapOf()
    private val loadedList: MutableList<Movie?> = mutableListOf()
    private var lastSearch = String()
    private var debounceSearchQuery = String()
    private var lastPage = 0

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
        startWorkManager()
        if (mutableMoviesList.value.isNullOrEmpty()) {
            when (endpoint) {
                FAVOURITE -> {
                    viewModelScope.launch {
                        loadFromDB(endpoint)
                    }
                }
                SEARCH -> {
                }
                else -> {
                    viewModelScope.launch {
                        var time = 0
                        while (mutableConnectionState.value == null) {
                            delay(10) //mutableConnectionState.value changes to actual with a delay, waiting for network status
                            time += 10
                            if (time >= 30) break
                        }
                        if (mutableConnectionState.value == true) {
                            loadFromAPI(endpoint, currentPage, isLoadingMore = false)
                        } else {
                            loadFromDB(endpoint)
                        }
                    }
                }
            }
        }
    }


    fun search(query: String, pageToLoad: Int, isLoadingMore: Boolean) {
        when (query != lastSearch) {
            true -> {
                viewModelScope.launch {
                    if (query == "") {
                        mutableMoviesList.postValue(listOf())
                        delay(500)
                        loadedList.clear()
                        lastSearch = query
                        debounceSearchQuery = String()
                    } else {
                        debounceSearchQuery = query
                        delay(500) //Debounce delay
                        if (query == debounceSearchQuery) {
                            loadFromAPI(endpoint = SEARCH, pageToLoad, isLoadingMore, query)
                            lastSearch = query
                        }

                    }

                }
            }
            false -> {
                if (isLoadingMore) {
                    viewModelScope.launch {
                        loadFromAPI(endpoint = SEARCH, pageToLoad, isLoadingMore, lastSearch)
                    }
                }
            }
        }
    }

    private suspend fun loadFromDB(endpoint: String?): Boolean {
        if (mutableLoadingState.value == false) {
            mutableLoadingState.postValue(true)
        }
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
                mutableMoviesList.postValue(movies!!)
                result = true
            } else {
                mutableErrorState.value = "No data in movies DB!"
            }
        } else {
            mutableMoviesList.postValue(movies ?: listOf())
        }
        mutableLoadingState.postValue(false)
        return result
    }

    private suspend fun loadFromAPI(
        endpoint: String?,
        currentPage: Int,
        isLoadingMore: Boolean,
        query: String = ""
    ) {
        loadGenresFromAPI()
        loadMoviesFromAPI(endpoint, currentPage, isLoadingMore, query)
    }

    fun onLikedButtonPressed(endpoint: String?, movie: Movie) {
        viewModelScope.launch {
            if (!movie.addedToFavourite) {
                movie.addedToFavourite = true
                dbHandler.addToFavourite(endpoint, movie)
            } else {
                movie.addedToFavourite = false
                if (endpoint == FAVOURITE) {
                    dbHandler.deleteFromFavourite(endpoint, movie)
                    loadFromDB(endpoint)
                }
                dbHandler.deleteFromFavourite(endpoint, movie)
            }
        }

    }

    fun refreshMovieList(endpoint: String?, currentPage: Int) {
        when (endpoint) {
            FAVOURITE -> {
                viewModelScope.launch {
                    mutableLoadingState.postValue(true)
                    loadFromDB(endpoint)
                    mutableLoadingState.postValue(false)
                }
            }
            SEARCH -> {
                viewModelScope.launch {
                    if (mutableConnectionState.value == true) {
                        try {
                            loadedList.clear()
                            loadFromAPI(endpoint, currentPage, isLoadingMore = false, lastSearch)
                        } catch (e: Exception) {
                            showError()
                        }
                    }
                }
            }
            else -> {
                viewModelScope.launch {
                    if (mutableConnectionState.value == true) {
                        try {
                            loadedList.clear()
                            dbHandler.clearDBTable(endpoint)
                            loadFromAPI(endpoint, currentPage, isLoadingMore = false)
                        } catch (e: Exception) {
                            showError()
                        }
                    } else {
                        loadFromDB(endpoint)
                    }
                }
            }
        }
    }

    private fun showError() {
        mutableErrorState.postValue("Connection error!")
    }

    private fun hideError() {
        mutableErrorState.postValue(NO_ERROR)
    }

    private suspend fun loadGenresFromAPI() {
        val list: List<GenresListItem?>
        try {
            list = network.getGenres(api_key).genres ?: listOf()
            genresAll.putAll(list.associateBy({ it?.id }, { it?.name }))
            dbHandler.saveGenresToDB(list)
            hideError()
        } catch (e: Exception) {
            e.printStackTrace()
            mutableLoadingState.postValue(false)
            showError()
        }
    }

    private suspend fun loadMoviesFromAPI(
        endpoint: String?,
        pageToLoad: Int,
        isLoadingMore: Boolean,
        query: String = ""
    ) {
        try {
            if (mutableLoadingState.value == false) {
                mutableLoadingState.postValue(true)
            }
            val newListOfMovies = when (endpoint) {
                SEARCH -> network.search(api_key, query, pageToLoad)
                else -> network.getMoviesList(
                    endpoint,
                    api_key,
                    pageToLoad
                )
            }
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
            if (!isLoadingMore) {
                loadedList.clear()
            }
            loadedList.addAll(newListWithGenres)
            hideError()
            mutableLoadingState.postValue(false)
            val newList = mutableListOf<Movie?>()
            for (movie in loadedList) {
                newList.add(movie?.copy())
            }
            mutableMoviesList.postValue(newList)
            loadedList.clear()
            loadedList.addAll(newList)
        } catch (e: Exception) {
            e.printStackTrace()
            mutableLoadingState.postValue(false)
            showError()
        }
    }

    private fun updateBasedOnListOfFavourite(
        list: List<Movie?>,
        listOfFavourite: List<Movie?>
    ): List<Movie?> {
        list.forEach { movieInCurrent ->
            val found = listOfFavourite.filter { it?.id == movieInCurrent?.id }
            movieInCurrent?.addedToFavourite = !found.isNullOrEmpty()
        }
        return list
    }

    private fun checkIfInFavourite(movie: Movie?, listOfFavourite: List<Movie?>): Movie? {
        val id = movie?.id
        val found = listOfFavourite.filter { it?.id == id }
        movie?.addedToFavourite = !found.isNullOrEmpty()
        return movie
    }

    fun updateIfInFavourite(endpoint: String?) {
        viewModelScope.launch {
            val listOfFavourite = converter.entityItemsListToMovieList(
                database.movieDAO().getListOfFavourite()
            )
            val list = mutableListOf<Movie>()
            for (item in loadedList) {
                list.add(item?.copy() ?: Movie())
            }
            val updatedList = updateBasedOnListOfFavourite(list, listOfFavourite)
            updatedList.forEach {
                if (it != null) {
                    dbHandler.updateTable(endpoint, it)
                }
            }
            mutableMoviesList.postValue(updatedList)
            loadedList.clear()
            mutableMoviesList.value?.let { loadedList.addAll(it) }
        }
    }

    fun loadMore(endpoint: String?, pageToLoad: Int) {
        when (endpoint) {
            SEARCH -> {
                search(lastSearch, pageToLoad, true)
                if (mutableErrorState.value == NO_ERROR) {
                    mutablePageStep.value = pageToLoad
                } else {
                    mutablePageStep.value = pageToLoad - 1
                }
            }
            FAVOURITE -> {
            }
            else -> {
                viewModelScope.launch {
                    loadMoviesFromAPI(endpoint, pageToLoad, isLoadingMore = true)
                    if (mutableErrorState.value == NO_ERROR) {
                        mutablePageStep.value = pageToLoad
                    } else {
                        mutablePageStep.value = pageToLoad - 1
                    }
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
const val SEARCH = "search"
const val NO_ERROR = "No Errors"


