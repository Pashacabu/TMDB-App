package com.pashcabu.hw2.view_model


import androidx.lifecycle.*
import com.pashcabu.hw2.model.ClassConverter
import com.pashcabu.hw2.model.NetworkModule
import com.pashcabu.hw2.model.NetworkModule.Companion.api_key
import com.pashcabu.hw2.model.data_classes.*
import com.pashcabu.hw2.model.data_classes.networkResponses.GenresListItem
import com.pashcabu.hw2.model.data_classes.networkResponses.Movie
import kotlinx.coroutines.launch
import kotlin.Exception

class MoviesListViewModel(private val database: Database) : ViewModel() {

    private val mutableMoviesList = MutableLiveData<List<Movie?>>(emptyList())
    private val mutableAmountOfPages = MutableLiveData<Int>()
    private val mutableLoadingState: MutableLiveData<Boolean> = MutableLiveData(false)
    private val mutableErrorState: MutableLiveData<String> = MutableLiveData(NO_ERROR)


    val movieList: LiveData<List<Movie?>> get() = mutableMoviesList
    val amountOfPages: LiveData<Int> get() = mutableAmountOfPages
    val loadingState: LiveData<Boolean> get() = mutableLoadingState
    val errorState: LiveData<String> get() = mutableErrorState
    private var genresAll: MutableMap<Int?, String?> = mutableMapOf()
    private val loadedList: MutableList<Movie?> = mutableListOf()

    private val network: NetworkModule.TMDBInterface = NetworkModule().apiService
    private val converter = ClassConverter()


    fun loadLiveData(endpoint: String?, currentPage: Int) {
        if (mutableMoviesList.value.isNullOrEmpty() || genresAll.isNullOrEmpty()) {
            if (endpoint == FAVOURITE) {
                viewModelScope.launch {
                    loadFromDB(endpoint)
                }
            } else {
                viewModelScope.launch {
                    loadFromDB(endpoint)
                    loadFromAPI(endpoint, currentPage)
                }
            }
        }
    }

    private suspend fun loadFromDB(endpoint: String?) {
        genresAll = loadGenresFromDB()
        loadMoviesListFromDB(endpoint)
    }

    private suspend fun loadFromAPI(endpoint: String?, currentPage: Int) {
        loadGenresFromAPI()
        loadMoviesFromAPI(endpoint, currentPage)
    }

    fun addToFavourite(endpoint: String?, movie: Movie) {
        viewModelScope.launch {
            if (!movie.addedToFavourite) {
                movie.addedToFavourite = true
                database.movieDAO().addToFavourite(converter.movieToEntityItem(movie))
                movie.id?.let { database.movieDAO().updateDB(it, movie.addedToFavourite) }
                loadedList.forEach {
                    if (it?.id == movie.id) {
                        it?.addedToFavourite = true
                    }
                    mutableMoviesList.value = loadedList
                }
            } else {
                if (endpoint == FAVOURITE) {
                    movie.id?.let {
                        database.movieDAO().deleteFromFavourite(it)
                        loadFromDB(endpoint)
                    }
                }
                movie.id?.let { database.movieDAO().deleteFromFavourite(it) }
                movie.id?.let { database.movieDAO().updateDB(it, !movie.addedToFavourite) }
                loadedList.forEach {
                    if (it?.id == movie.id) {
                        it?.addedToFavourite = false
                    }
                }
            }
        }

    }

    private suspend fun loadMoviesListFromDB(endpoint: String?) {
        val listOfMovies: List<Movie?>?
        mutableLoadingState.value = true
        when (endpoint) {
            NOW_PLAYING -> {
                val listOfEntities = database.movieDAO().getNowPlaying()
                listOfMovies = converter.entityItemsListToMovieList(listOfEntities)
                mutableMoviesList.value = listOfMovies
            }
            POPULAR -> {
                val listOfEntities = database.movieDAO().getPopular()
                listOfMovies = converter.entityItemsListToMovieList(listOfEntities)
                mutableMoviesList.value = listOfMovies
            }
            TOP_RATED -> {
                val listOfEntities = database.movieDAO().getTopRated()
                listOfMovies = converter.entityItemsListToMovieList(listOfEntities)
                mutableMoviesList.value = listOfMovies
            }
            UPCOMING -> {
                val listOfEntities = database.movieDAO().getUpcoming()
                listOfMovies = converter.entityItemsListToMovieList(listOfEntities)
                mutableMoviesList.value = listOfMovies
            }
            FAVOURITE -> {
                val listOfEntities = database.movieDAO().getListOfFavourite()
                listOfMovies = converter.entityItemsListToMovieList(listOfEntities)
                mutableMoviesList.value = listOfMovies
            }
        }
        mutableLoadingState.value = false
    }

    private suspend fun saveMoviesListToDB(endpoint: String?, list: List<Movie?>?) {
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

    private suspend fun clearDBTable(endpoint: String?) {
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
        database.genresDAO().deleteGenres()
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
                try {
                    loadedList.clear()
                    clearDBTable(endpoint)
                    loadFromAPI(endpoint, currentPage)
                } catch (e: Exception) {
                    showError()
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
            saveGenresToDB(list)
            genresAll.putAll(list.associateBy({ it?.id }, { it?.name }))
            saveGenresToDB(list)
            hideError()
            mutableLoadingState.value = false
        } catch (e: Exception) {
            e.printStackTrace()
            mutableLoadingState.value = false
            showError()
        }
    }

    private suspend fun saveGenresToDB(list: List<GenresListItem?>) {
        val listToSave = converter.genresListRespToEntity(list)
        database.genresDAO().insertGenres(listToSave)
    }

    private suspend fun loadGenresFromDB(): MutableMap<Int?, String?> {
        val output: MutableMap<Int?, String?> = mutableMapOf()
        mutableLoadingState.value = true
        val listOfGenres = database.genresDAO().getGenres()
        output.putAll(listOfGenres.associateBy({ it.id }, { it.name }))
        mutableLoadingState.value = false
        return output
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
            val newListWithGenres =
                newListOfMovies.results?.map {
                    genresIntToStrings(
                        it,
                        genresAll
                    )
                }!!
            val listOfFavourite =
                converter.entityItemsListToMovieList(
                    database.movieDAO().getListOfFavourite()
                )
            newListWithGenres.forEach { checkIfInFavourite(it, listOfFavourite) }
            saveMoviesListToDB(endpoint, newListWithGenres)
            loadedList.addAll(newListWithGenres)
            hideError()
            mutableMoviesList.postValue(loadedList)
            mutableLoadingState.postValue(false)
        } catch (e: Exception) {
            e.printStackTrace()
            mutableLoadingState.postValue(false)
            showError()
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

    fun loadMore(endpoint: String?, pageToLoad: Int) {
        if (endpoint != FAVOURITE) {
            viewModelScope.launch {
                loadMoviesFromAPI(endpoint, pageToLoad)
            }
        }
    }

    private fun genresIntToStrings(item: Movie?, genresMap: Map<Int?, String?>?): Movie? {
        val ids = item?.genreIds
        val genresBasedOnIds = ids?.map { genresMap?.get(it) }
        item?.genres = genresBasedOnIds
        return item
    }

}


const val NOW_PLAYING = "now_playing"
const val POPULAR = "popular"
const val TOP_RATED = "top_rated"
const val UPCOMING = "upcoming"
const val FAVOURITE = "favourite"
const val NO_ERROR = "No Errors"

