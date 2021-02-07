package com.pashcabu.hw2.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashcabu.hw2.model.ClassConverter
import com.pashcabu.hw2.model.NetworkModule
import com.pashcabu.hw2.model.data_classes.networkResponses.CastResponse
import com.pashcabu.hw2.model.data_classes.Database
import com.pashcabu.hw2.model.data_classes.networkResponses.MovieDetailsResponse
import kotlinx.coroutines.launch

class MovieDetailsViewModel(private val database: Database) : ViewModel() {
    private val mutableMovieDetails: MutableLiveData<MovieDetailsResponse> = MutableLiveData(
        MovieDetailsResponse()
    )
    private val mutableCastData: MutableLiveData<CastResponse> = MutableLiveData(CastResponse())
    private val mutableLoadingState: MutableLiveData<Boolean> = MutableLiveData(false)
    private val mutableErrorState: MutableLiveData<String> = MutableLiveData(NO_ERROR)

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
            if (mutableMovieDetails.value == MovieDetailsResponse()) {
                loadMovieDataFromDB(id)
                loadMovieDataFromAPI(id)
            }
        }
    }

    private suspend fun loadMovieDataFromDB(id: Int) {
        mutableLoadingState.value = true
        mutableMovieDetails.value =
            converter.movieDetailsEntityToResponse(database.detailsDAO().getMovieDetails(id))
        cast.castList = converter.castEntityListToResponseList(database.detailsDAO().getCast(id))
        cast.crew = converter.crewEntityListToResponseList(database.detailsDAO().getCrew(id))
        mutableCastData.value = cast
        mutableLoadingState.value = false
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
            mutableLoadingState.postValue(false)
            showError()
        }
    }

    fun refreshMovieDetailsData(id: Int) {
        viewModelScope.launch {
            mutableLoadingState.value = true
            mutableMovieDetails.value = MovieDetailsResponse()
            mutableCastData.value = CastResponse()
            loadMovieDataFromAPI(id)
            mutableLoadingState.value = false
        }

    }
}