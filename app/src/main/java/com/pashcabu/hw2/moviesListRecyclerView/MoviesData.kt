package com.pashcabu.hw2.moviesListRecyclerView


import android.app.Activity
import android.app.Application
import android.widget.Toast
import com.pashcabu.hw2.movieDetailsRecyclerView.ActorData
import com.pashcabu.hw2.R

class MoviesData {
    fun getMovies():List<Movie>{
        return listOf(
                Movie(R.string.avengers, R.string.pg13, 4, R.string.tags_avengers, 125, 137, R.drawable.poster_avengers, R.drawable.background, R.string.avengers_storyline, ActorData().avengers()),
                Movie(R.string.tenet, R.string.pg16, 5, R.string.tags_tenet, 98, 97, R.drawable.poster_tenet, R.drawable.tenet_big_poster, R.string.tenet_storyline, ActorData().tenet()),
                Movie(R.string.black_widow, R.string.pg13, 4, R.string.tags_black_widow, 38, 102, R.drawable.poster_black_widow, R.drawable.black_widow_big_poster, R.string.black_widow_storyline, ActorData().blackWidow()),
                Movie(R.string.wonder_woman_1984, R.string.pg13, 5, R.string.tags_wonder_woman, 74, 120, R.drawable.poster_wonder_woman_1984, R.drawable.wonderwoman_1984_big_poster, R.string.wonder_woman_storyline, ActorData().wonderWoman())
        )
    }


    fun findMovieByTitle(search_title : Int?):Movie {
        val movies:List<Movie> = getMovies()
        return movies.find { it.title== search_title} as Movie
    }



}