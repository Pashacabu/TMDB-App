package com.pashcabu.hw2.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashcabu.hw2.model.ClassConverter
import com.pashcabu.hw2.model.NetworkModule
import com.pashcabu.hw2.model.data_classes.networkResponses.CastResponse
import com.pashcabu.hw2.model.data_classes.Database
import com.pashcabu.hw2.model.data_classes.networkResponses.MovieDetailsResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MovieDetailsViewModel(private val database: Database) : ViewModel() {
    private val mutableMovieDetails: MutableLiveData<MovieDetailsResponse> = MutableLiveData()
    private val mutableCastData: MutableLiveData<CastResponse> = MutableLiveData(CastResponse())
    private val mutableLoadingState: MutableLiveData<Boolean> = MutableLiveData(false)
    private val mutableErrorState: MutableLiveData<String> = MutableLiveData(NO_ERROR)
    private val mutableConnectionState = MutableLiveData<Boolean>()

    val movieDetailsData: LiveData<MovieDetailsResponse> get() = mutableMovieDetails
    val castData: LiveData<CastResponse> get() = mutableCastData
    val loadingState: LiveData<Boolean> get() = mutableLoadingState
    val errorState: LiveData<String> get() = mutableErrorState
    private val network: NetworkModule.TMDBInterface = NetworkModule().apiService
    private val converter = ClassConverter()
    private var cast = CastResponse()

    private fun showError() {
        mutableErrorState.value = "Connection error!"
    }

    fun loadData(id: Int) {
        viewModelScope.launch {
            if (mutableMovieDetails.value == null || mutableMovieDetails.value == MovieDetailsResponse()) {
                var time = 0
                while (mutableConnectionState.value == null) {
                    delay(10) //mutableConnectionState.value changes to actual with a delay, waiting for network status
                    time += 10
                    if (time >= 30) break
                }
                if (mutableConnectionState.value == true) {
                    loadMovieDataFromAPI(id)
                } else {
                    loadMovieDataFromDB(id)
                    mutableErrorState.value="Offline data"
                }
            }
        }
    }

    private suspend fun loadMovieDataFromDB(id: Int): Boolean {
        mutableLoadingState.value = true
        var loaded = false
        val movie = converter.movieDetailsEntityToResponse(database.detailsDAO().getMovieDetails(id))
        if (movie != MovieDetailsResponse()) {
            loaded = true
            cast.castList = converter.castEntityListToResponseList(database.detailsDAO().getCast(id))
            cast.crew = converter.crewEntityListToResponseList(database.detailsDAO().getCrew(id))
        }
        if (loaded) {
            mutableMovieDetails.value = movie
            mutableCastData.value = cast
        } else {
            mutableMovieDetails.value = MovieDetailsResponse()
            mutableErrorState.value = "No data in DB!"
        }
        mutableLoadingState.value = false
        return loaded
    }

    private suspend fun loadMovieDataFromAPI(id: Int) {
        try {
            mutableLoadingState.value = true
            val movie = network.getMovieDetails(id, NetworkModule.api_key)
            mutableMovieDetails.value = movie
            val cast = network.getActors(id, NetworkModule.api_key)
            mutableCastData.value = cast
            val castForDB = cast.castList?.let { converter.castResponseListToEntityList(it) }
            val crewForDB = cast.crew?.let { converter.crewResponseListToEntityList(it) }
            castForDB?.forEach { it.movieId = id }
            crewForDB?.forEach { it.movieId = id }
            database.detailsDAO().insertMovieDetails(converter.movieDetailsResponseToEntity(movie))
            if (castForDB != null) {
                database.detailsDAO().insertCast(castForDB)
            }
            if (crewForDB != null) {
                database.detailsDAO().insertCrew(crewForDB)
            }
            mutableLoadingState.value = false
        } catch (e: Exception) {
            e.printStackTrace()
            loadMovieDataFromDB(id)
            mutableLoadingState.postValue(false)
            showError()
        }
    }

    fun refreshMovieDetailsData(id: Int) {
        viewModelScope.launch {
            mutableLoadingState.value = true
            mutableMovieDetails.value = MovieDetailsResponse()
            mutableCastData.value = CastResponse()
            loadData(id)
            mutableLoadingState.value = false
        }
    }

    fun setConnectionState(connected: Boolean) {
        mutableConnectionState.value = connected
    }
}