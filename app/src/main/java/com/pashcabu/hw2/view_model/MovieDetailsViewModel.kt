package com.pashcabu.hw2.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashcabu.hw2.model.ClassConverter
import com.pashcabu.hw2.model.DBHandler
import com.pashcabu.hw2.model.NetworkModule
import com.pashcabu.hw2.model.data_classes.networkResponses.CastResponse
import com.pashcabu.hw2.model.data_classes.Database
import com.pashcabu.hw2.model.data_classes.networkResponses.MovieDetailsResponse
import com.pashcabu.hw2.model.SingleNetwork
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
    private val network = SingleNetwork.service
    private val converter = ClassConverter()
    private var cast = CastResponse()
    private val dbHandler = DBHandler(database)

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
                    when (id) {
                        0 -> getLatest()
                        else -> loadMovieDataFromAPI(id)
                    }
//                    loadMovieDataFromAPI(id)
                } else {
                    loadMovieDataFromDB(id)
                    mutableErrorState.value = "Offline data"
                }
            }
        }
    }

    private suspend fun getLatest() {
        if (mutableLoadingState.value == false) {
            mutableLoadingState.postValue(true)
        }
        when (mutableConnectionState.value) {
            true -> {
                val movie = network.getLatest(NetworkModule.api_key)
                val id = movie.movieId ?: 0
                val cast = movie.movieId?.let { network.getActors(it, NetworkModule.api_key) }
                mutableMovieDetails.postValue(movie)
                mutableCastData.postValue(cast)
                val castForDB = cast?.castList?.let { converter.castResponseListToEntityList(it) }
                val crewForDB = cast?.crew?.let { converter.crewResponseListToEntityList(it) }
                castForDB?.forEach { it.movieId = id }
                crewForDB?.forEach { it.movieId = id }
                val latestIDs = database.detailsDAO().getLatestID()
                var latestID = 0
                if (latestIDs.isNotEmpty()) {
                    latestID = latestIDs[0]
                }
                database.detailsDAO().deleteLatestCast(latestID)
                database.detailsDAO().deleteLatestCrew(latestID)
                database.detailsDAO().deleteLatestMovie()
                database.detailsDAO().saveLatestMovie(converter.movieDetailsResponseToEntity(movie))
                if (castForDB != null) {
                    database.detailsDAO().insertCast(castForDB)
                }
                if (crewForDB != null) {
                    database.detailsDAO().insertCrew(crewForDB)
                }
                mutableLoadingState.postValue(false)
            }
            else -> {
                val id = database.detailsDAO().getLatestID()[0]
                loadMovieDataFromDB(id)
            }
        }

    }

    private suspend fun loadMovieDataFromDB(id: Int) {
        mutableLoadingState.value = true
        var loaded = false
        val movie = dbHandler.loadMovieDataFromDB(id)
        val dbCast = dbHandler.loadCastDataFromDB(id)
        val liked = dbHandler.checkIfInFavourite(id)
        movie.liked = liked
        if (movie != MovieDetailsResponse()) {
            loaded = true
            cast.castList = dbCast.castList
            cast.crew = dbCast.crew
        }
        if (loaded) {
            mutableMovieDetails.value = movie
            mutableCastData.value = cast
        } else {
            mutableMovieDetails.value = MovieDetailsResponse()
            mutableErrorState.value = "No data in DB!"
        }
        mutableLoadingState.value = false
    }

    fun likeMovie() {
        val movie = movieDetailsData.value
        viewModelScope.launch {
            when (movie?.liked) {
                true -> {
                    dbHandler.deleteFromFavourite(movie)
                    movie.liked = false
                }
                false -> {
                    dbHandler.addToFavourite(movie)
                    movie.liked = true
                }
            }
            mutableMovieDetails.postValue(movie)
        }
    }

    private suspend fun loadMovieDataFromAPI(id: Int) {
        try {
            mutableLoadingState.value = true
            val movie = network.getMovieDetails(id, NetworkModule.api_key)
            val liked = dbHandler.checkIfInFavourite(id)
            movie.liked = liked
            mutableMovieDetails.value = movie
            val cast = network.getActors(id, NetworkModule.api_key)
            mutableCastData.value = cast
            dbHandler.saveMovieDetails(movie)
            movie.movieId?.let { dbHandler.saveCastDetails(cast, it) }
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