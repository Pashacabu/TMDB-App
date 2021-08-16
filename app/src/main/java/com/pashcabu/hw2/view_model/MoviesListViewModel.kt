package com.pashcabu.hw2.view_model


import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.pashcabu.hw2.model.*
import com.pashcabu.hw2.model.NetworkModule.Companion.api_key
import com.pashcabu.hw2.model.data_classes.*
import com.pashcabu.hw2.model.data_classes.networkResponses.GenresListItem
import com.pashcabu.hw2.model.data_classes.networkResponses.Movie
import com.pashcabu.hw2.model.data_classes.networkResponses.MovieListResponse
import com.pashcabu.hw2.views.PersonFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.Exception

class MoviesListViewModel(private val database: Database, private val worker: WorkManager) :
    ViewModel() {

    private val mutableMoviesList = MutableLiveData<List<Movie?>?>(emptyList())
    private val mutableAmountOfPages = MutableLiveData<Int>()
    private val mutableLoadingState: MutableLiveData<Boolean> = MutableLiveData(false)
    private val mutableErrorState: MutableLiveData<String> = MutableLiveData(NO_ERROR)
    private val mutablePageStep = MutableLiveData(1)
    private val mutableConnectionState = MutableLiveData<Boolean>()

    val moviesList: LiveData<List<Movie?>?> get() = mutableMoviesList
    val amountOfPages: LiveData<Int> get() = mutableAmountOfPages
    val loadingState: LiveData<Boolean> get() = mutableLoadingState
    val errorState: LiveData<String> get() = mutableErrorState
    val pageStep: LiveData<Int> get() = mutablePageStep

    private var genresAll: MutableMap<Int?, String?> = mutableMapOf()
    private val loadedList: MutableList<Movie?> = mutableListOf()
    private var lastSearch = String()
    private var debounceSearchQuery = String()
    private var personID = 0
    private var endpoint = ""
    private var sorting = SortingsList.POP_Q_DW
    private val network = SingleNetwork.service
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

    fun setSorting(_sorting: String) {
        sorting = _sorting
        loadedList.clear()
        refreshMovieList(endpoint, currentPage = 1)
    }

    private suspend fun getConnectionState() {
        var time = 0
        while (mutableConnectionState.value == null) {
            Log.d("getting connection", "time is $time")
            delay(10) //mutableConnectionState.value changes to actual with a delay, waiting for network status
            time += 10
            if (time >= 100) break
        }
    }

    fun loadLiveData(
        _endpoint: String?,
        currentPage: Int,
        _personID: Int = 0
    ) {
        startWorkManager()
        endpoint = _endpoint ?: ""
        if (mutableMoviesList.value.isNullOrEmpty()) {
            when (_endpoint) {
                FAVOURITE -> {
                    viewModelScope.launch {
                        loadFromDB(_endpoint)
                    }
                }
                SEARCH -> {

                }
                PersonFragment.LIST -> {
                    personID = _personID
                    viewModelScope.launch {
                        getConnectionState()
                        if (mutableConnectionState.value == true) {
                            loadFromAPI(
                                _endpoint,
                                currentPage,
                                isLoadingMore = false,
                                personID = _personID
                            )
                        } else {
                            showError()
                        }
                    }


                }
                else -> {
                    viewModelScope.launch {
                        getConnectionState()
                        if (mutableConnectionState.value == true) {
                            loadFromAPI(_endpoint, currentPage, isLoadingMore = false)
                        } else {
                            loadFromDB(_endpoint)
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
                    when (query) {
                        "" -> {
                            debounceSearchQuery = String()
                            mutableMoviesList.postValue(listOf())
                            delay(500)
                            loadedList.clear()
                            lastSearch = query

                        }
                        else -> {
                            debounceSearchQuery = query
                            delay(500) //Debounce delay
                            if (query == debounceSearchQuery) {
                                loadFromAPI(endpoint = SEARCH, pageToLoad, isLoadingMore, query)
                                lastSearch = query
                            }
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
        query: String = "",
        personID: Int = 0
    ) {
        loadGenresFromAPI()
        loadMoviesFromAPI(endpoint, currentPage, isLoadingMore, query, personID)
    }

    fun onLikedButtonPressed(endpoint: String?, movie: Movie) {
        viewModelScope.launch {
            if (!movie.addedToFavourite) {
                movie.addedToFavourite = true
                dbHandler.addToFavourite(movie)
            } else {
                movie.addedToFavourite = false
                if (endpoint == FAVOURITE) {
                    dbHandler.deleteFromFavourite(movie)
                    loadFromDB(endpoint)
                }
                dbHandler.deleteFromFavourite(movie)
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
                            loadFromAPI(
                                endpoint,
                                currentPage,
                                isLoadingMore = false,
                                personID = personID
                            )
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
        when (mutableConnectionState.value) {
            true -> {
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
            false -> {
                showError()
            }
        }

    }

    private suspend fun loadMoviesFromAPI(
        endpoint: String?,
        pageToLoad: Int,
        isLoadingMore: Boolean,
        query: String = "",
        personID: Int = 0
    ) {
        try {
            if (mutableLoadingState.value == false) {
                mutableLoadingState.postValue(true)
            }
            var newListOfMovies = MovieListResponse()
            when (endpoint) {
                SEARCH -> {
                    newListOfMovies = network.search(api_key, query, pageToLoad)
                }
                PersonFragment.LIST -> {
                    when (mutableConnectionState.value) {
                        true -> {
                            val personMovies =
                                network.getPersonMovies(
                                    api_key,
                                    personID = personID,
                                    page = pageToLoad,
                                    sortBy = sorting
                                )
                            newListOfMovies.results = personMovies.results
                            newListOfMovies.page = personMovies.page
                            newListOfMovies.totalPages = personMovies.totalPages
                            newListOfMovies.totalResults = personMovies.totalResults
                            newListOfMovies.dates = null
                        }
                        false -> {
                            showError()
                        }
                    }

                }
                else -> {
                    newListOfMovies = network.getMoviesList(
                        endpoint,
                        api_key,
                        pageToLoad
                    )
                }
            }
            mutableAmountOfPages.value = newListOfMovies.totalPages ?: 0
            var newListWithGenres =
                newListOfMovies.results?.map {
                    converter.genresIntToStrings(
                        it,
                        genresAll
                    )
                }
            val listOfFavourite =
                converter.entityItemsListToMovieList(
                    database.movieDAO().getListOfFavourite()
                )
            newListWithGenres = updateBasedOnListOfFavourite(newListWithGenres, listOfFavourite)
            when (isLoadingMore) {
                true -> {
                    dbHandler.saveMoviesListToDB(endpoint, newListWithGenres)
                }
                false -> {
                    dbHandler.clearDBTable(endpoint)
                    dbHandler.saveMoviesListToDB(endpoint, newListWithGenres)
                    loadedList.clear()
                }
            }
            if (newListWithGenres != null) {
                loadedList.addAll(newListWithGenres)
            }
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
        list: List<Movie?>?,
        listOfFavourite: List<Movie?>
    ): List<Movie?>? {
        list?.forEach { movieInCurrent ->
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
        if (!loadedList.isNullOrEmpty()) {
            viewModelScope.launch {
                val listOfFavourite = converter.entityItemsListToMovieList(
                    database.movieDAO().getListOfFavourite()
                )
                val list = mutableListOf<Movie>()
                for (item in loadedList) {
                    list.add(item?.copy() ?: Movie())
                }
                val updatedList = updateBasedOnListOfFavourite(list, listOfFavourite)
                updatedList?.forEach {
                    if (it != null) {
                        dbHandler.updateTable(endpoint, it)
                    }
                }
                mutableMoviesList.postValue(updatedList)
                loadedList.clear()
                mutableMoviesList.value?.let { loadedList.addAll(it) }
            }
        }

    }

    fun loadMore(endpoint: String?, pageToLoad: Int) {
        if (loadingState.value != true) {
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
                PersonFragment.LIST -> {
                    viewModelScope.launch {
                        loadMoviesFromAPI(
                            endpoint,
                            pageToLoad,
                            isLoadingMore = true,
                            personID = personID
                        )
                        if (mutableErrorState.value == NO_ERROR) {
                            mutablePageStep.value = pageToLoad
                        } else {
                            mutablePageStep.value = pageToLoad - 1
                        }
                    }

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

    }

    fun setConnectionState(connected: Boolean) {
        if (mutableConnectionState.value == false && connected) {
            mutableConnectionState.value = connected
            refreshMovieList(endpoint, 1)
        } else {
            mutableConnectionState.value = connected
        }

    }

}


const val NOW_PLAYING = "now_playing"
const val POPULAR = "popular"
const val TOP_RATED = "top_rated"
const val UPCOMING = "upcoming"
const val FAVOURITE = "favourite"
const val SEARCH = "search"
const val NO_ERROR = "No Errors"


