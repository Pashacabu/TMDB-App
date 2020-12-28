package com.pashcabu.hw2

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashcabu.hw2.data.Movie
import com.pashcabu.hw2.data.loadMovie
import com.pashcabu.hw2.data.loadMovies
import kotlinx.coroutines.launch

class MyViewModel : ViewModel() {

    private val mutableMoviesListData: MutableLiveData<List<Movie>> = MutableLiveData(emptyList())
    private val mutableMovieDetailsData: MutableLiveData<Movie> = MutableLiveData()

    val liveMovieListData: LiveData<List<Movie>> get() = mutableMoviesListData
    val liveMovieDetailsData: LiveData<Movie> get() = mutableMovieDetailsData

    fun loadMoviesListToLiveData(context: Context) {
        viewModelScope.launch {
            val list = loadMovies(context)
            mutableMoviesListData.value = list
        }

    }

    fun loadMovieDetailsToLiveData(context: Context, id: Int) {
        viewModelScope.launch {
            val movie = loadMovie(context, id)
            mutableMovieDetailsData.value = movie
        }
    }
}