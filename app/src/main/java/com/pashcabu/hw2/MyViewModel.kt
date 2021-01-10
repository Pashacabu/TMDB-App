package com.pashcabu.hw2

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlin.Exception

class MyViewModel : ViewModel() {

    private val mutableMoviesListData: MutableLiveData<List<ResultsItem?>> =
        MutableLiveData(listOf())
    private val mutableMovieDetailsData: MutableLiveData<MovieDetailsResponse> = MutableLiveData()
    private val mutableCastData: MutableLiveData<Cast> = MutableLiveData()

    val liveMovieListData: LiveData<List<ResultsItem?>> get() = mutableMoviesListData
    val liveMovieDetailsData: LiveData<MovieDetailsResponse> get() = mutableMovieDetailsData
    val liveCastData: LiveData<Cast> get() = mutableCastData
    private var pageNumber = 1
    private var genresAll: Map<Int?, String?>? = null


    fun loadMoviesListToLiveData() {
        if (mutableMoviesListData.value.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    val genresMap: Map<Int?, String?>? =
                        NetworkModule().apiService.getGenres(api_key).genres?.associateBy({ it?.genreId },
                            { it?.genreName })
                    val listOfMovies = NetworkModule().apiService.getMoviesList(api_key, pageNumber)
                    val output = listOfMovies.results?.map { genresIntToStrings(it, genresMap) }
                    mutableMoviesListData.value = output
                    genresAll = genresMap
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
        if (mutableMovieDetailsData.value == null) {
            viewModelScope.launch {
                try {
                    val movie = NetworkModule().apiService.getMovieDetails(id, api_key)
                    mutableMovieDetailsData.value = movie
                    val cast = NetworkModule().apiService.getActors(id, api_key)
                    mutableCastData.value = cast
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}