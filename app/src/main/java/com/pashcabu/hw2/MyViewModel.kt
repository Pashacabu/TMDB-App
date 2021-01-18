package com.pashcabu.hw2

import android.hardware.usb.UsbEndpoint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashcabu.hw2.recyclerAdapters.NewMoviesListAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.Exception

class MyViewModel : ViewModel() {

    private val _mutableMoviesList = MutableLiveData<List<ResultsItem?>>(emptyList())
    private val mutableMovieDetails: MutableLiveData<MovieDetailsResponse> = MutableLiveData()
    private val mutableCastData: MutableLiveData<Cast> = MutableLiveData()
    private val mutableLoadingState: MutableLiveData<Boolean> = MutableLiveData(false)

    val movieList: LiveData<List<ResultsItem?>> get() = _mutableMoviesList
    val movieDetailsData: LiveData<MovieDetailsResponse> get() = mutableMovieDetails
    val castData: LiveData<Cast> get() = mutableCastData
    val loadingState: LiveData<Boolean> get() = mutableLoadingState
    private var pageNumber = 1
    private var _genresAll: MutableMap<Int?, String?> = mutableMapOf()
    private var loadedList: MutableList<ResultsItem?> = mutableListOf()


    fun loadLiveData(endpoint: String?) {
        if (_genresAll.isNullOrEmpty()) {
            Log.d("ViewModel", "genres are needed to load")
            loadGenres()
        }
        if (_mutableMoviesList.value.isNullOrEmpty()) {
            loadMovies(endpoint)
            pageNumber += 1
        }
    }

    private fun loadGenres() {
        Log.d("ViewModel", "loading genres")
        try {
            viewModelScope.launch {
                mutableLoadingState.value = true
                val genresMap: Map<Int?, String?>? =
                    NetworkModule().apiService.getGenres(api_key).genres?.associateBy({ it?.genreId },
                        { it?.genreName })
                if (genresMap != null) {
                    _genresAll.putAll(genresMap)
                }
                mutableLoadingState.postValue(false)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun loadMovies(endpoint: String?) {
        Log.d("ViewModel", "movies are needed to load")
        viewModelScope.launch {
            try {
                Log.d("ViewModel", "loading movies")
                mutableLoadingState.value = true
                val listOfMovies =
                    NetworkModule().apiService.getMoviesList(endpoint, api_key, pageNumber)
                val output = listOfMovies.results?.map { genresIntToStrings(it, _genresAll) }
                if (output != null) {
                    loadedList.addAll(output)
                }
                _mutableMoviesList.value = loadedList
                mutableLoadingState.value = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun showLoading() {
        viewModelScope.launch {
            mutableLoadingState.value = true
            Log.d("ViewModel", "${mutableLoadingState.value}")
            delay(4000)
            mutableLoadingState.value = false
            Log.d("ViewModel", "${mutableLoadingState.value}")
        }
    }

    fun loadMoreMovies() {
        pageNumber += 1
        Log.d("ViewModel", "page number is $pageNumber")
//        loadMovies()

//        val list = loadedList
//        Log.d("ViewModel", "${list?.size}")
//        val genres = _genresAll
//        Log.d("ViewModel", "${_genresAll?.size}")
//        Log.d("ViewModel", "loading more movies")
//        pageNumber += 1
//        viewModelScope.launch {
//            try {
//                mutableLoadingState.postValue(true)
//                Log.d("ViewModel", "$pageNumber")
//                val newListOfMovies = NetworkModule().apiService.getMoviesList(
//                    api_key,
//                    pageNumber
//                )
//                val newListWithGenres =
//                    newListOfMovies.results?.map { genresIntToStrings(it, genres) }
//                val output = combineLists(list, newListWithGenres)
//                _mutableMoviesList.postValue(output)
//                if (output != null) {
//                    loadedList?.addAll(output)
//                }
//                mutableLoadingState.postValue(false)
//                Log.d("ViewModel", "loadaded ${output?.size}")
//                MoviesListFragment().notifyAdapter()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }

    }


    private fun combineLists(
        list1: List<ResultsItem?>?,
        list2: List<ResultsItem?>?
    ): List<ResultsItem?>? {
        Log.d("ViewModel", "combining lists")
        var result = list1
        if (list2 != null) {
            for (item in list2) {
                result = result?.plus(item)
            }
        }
        Log.d("ViewModel", "total combined ${result?.size}")
        return result
    }

    private fun genresIntToStrings(item: ResultsItem?, map: Map<Int?, String?>?): ResultsItem? {
        val ids = item?.genreIds
        val genresBasedOnIds = ids?.map { map?.get(it) }
        item?.genres = genresBasedOnIds
        return item
    }

    fun loadMovieDetailsToLiveData(id: Int) {
        if (mutableMovieDetails.value == null) {
            viewModelScope.launch {
                try {
                    mutableLoadingState.value = true
                    if (mutableMovieDetails.value == null) {
                        val movie = NetworkModule().apiService.getMovieDetails(id, api_key)
//                        Log.d("Movie Details", "is loading")
                        mutableMovieDetails.value = movie
                        val cast = NetworkModule().apiService.getActors(id, api_key)
                        mutableCastData.value = cast
                    }
                    mutableLoadingState.value = false

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

