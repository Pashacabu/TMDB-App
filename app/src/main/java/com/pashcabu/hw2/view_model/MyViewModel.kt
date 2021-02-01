package com.pashcabu.hw2.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashcabu.hw2.model.NetworkModule
import com.pashcabu.hw2.model.NetworkModule.Companion.api_key
import com.pashcabu.hw2.model.data_classes.Cast
import com.pashcabu.hw2.model.data_classes.MovieDetailsResponse
import com.pashcabu.hw2.model.data_classes.ResultsItem
import kotlinx.coroutines.launch
import kotlin.Exception

class MyViewModel : ViewModel() {

    private val mutableMoviesList = MutableLiveData<List<ResultsItem?>>(emptyList())
    private val mutableAmountOfPages = MutableLiveData<Int>()
    private val mutableMovieDetails: MutableLiveData<MovieDetailsResponse> = MutableLiveData()
    private val mutableCastData: MutableLiveData<Cast> = MutableLiveData()
    private val mutableLoadingState: MutableLiveData<Boolean> = MutableLiveData(false)


    val movieList: LiveData<List<ResultsItem?>> get() = mutableMoviesList
    val amountOfPages: LiveData<Int> get() = mutableAmountOfPages
    val movieDetailsData: LiveData<MovieDetailsResponse> get() = mutableMovieDetails
    val castData: LiveData<Cast> get() = mutableCastData
    val loadingState: LiveData<Boolean> get() = mutableLoadingState
    private var pageNumber = 1
    private var _genresAll: MutableMap<Int?, String?> = mutableMapOf()
    private var loadedList: MutableList<ResultsItem?> = mutableListOf()
    private val network: NetworkModule.TMDBInterface = NetworkModule().apiService


    fun loadLiveData(endpoint: String?) {
        if (_genresAll.isNullOrEmpty()) {
            loadGenres()
        }
        if (mutableMoviesList.value.isNullOrEmpty()) {
            loadMovies(endpoint)
        }
    }

    fun refreshMovieList(endpoint: String?) {
        loadedList = mutableListOf()
        mutableMoviesList.value = loadedList
        loadLiveData(endpoint)
    }

    private fun loadGenres() {
        viewModelScope.launch {
            try {
                mutableLoadingState.value = true
                val genresMap: Map<Int?, String?>? =
                    network.getGenres(api_key).genres?.associateBy({ it?.genreId },
                        { it?.genreName })
                if (genresMap != null) {
                    _genresAll.putAll(genresMap)
                }
                mutableLoadingState.postValue(false)
            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }

    private fun loadMovies(endpoint: String?) {
        viewModelScope.launch {
            try {
                mutableLoadingState.value = true
                val moviesListResponse =
                    network.getMoviesList(endpoint, api_key, pageNumber)
                mutableAmountOfPages.value = moviesListResponse.totalPages ?: 0
                val output = moviesListResponse.results?.map { genresIntToStrings(it, _genresAll) }
                if (output != null) {
                    loadedList.addAll(output)
                }
                mutableMoviesList.value = loadedList
                mutableLoadingState.value = false

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    fun loadMoreMovies(endpoint: String?, pageToLoad: Int) {
        val genres = _genresAll
        viewModelScope.launch {
            try {
                mutableLoadingState.postValue(true)
                val newListOfMovies = network.getMoviesList(
                    endpoint,
                    api_key,
                    pageToLoad
                )
                val newListWithGenres =
                    newListOfMovies.results?.map { genresIntToStrings(it, genres) }
                if (newListWithGenres != null) {
                    loadedList.addAll(newListWithGenres)
                }
                mutableMoviesList.postValue(loadedList)
                mutableLoadingState.postValue(false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun genresIntToStrings(item: ResultsItem?, map: Map<Int?, String?>?): ResultsItem? {
        val ids = item?.genreIds
        val genresBasedOnIds = ids?.map { map?.get(it) }
        item?.genres = genresBasedOnIds
        return item
    }

    fun loadMovieDetailsToLiveData(id: Int) {
        if (mutableMovieDetails.value?.movieId == null) {
            viewModelScope.launch {
                try {
                    mutableLoadingState.value = true
                    val movie = network.getMovieDetails(id, api_key)
                    mutableMovieDetails.value = movie
                    val cast = network.getActors(id, api_key)
                    mutableCastData.value = cast
                    mutableLoadingState.value = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun refreshMovieDetailsData(id: Int) {
        mutableMovieDetails.value = MovieDetailsResponse()
        mutableCastData.value = Cast()
        loadMovieDetailsToLiveData(id)
    }
}

