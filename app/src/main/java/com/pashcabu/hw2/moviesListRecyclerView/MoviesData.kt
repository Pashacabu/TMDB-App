package com.pashcabu.hw2.moviesListRecyclerView

import com.pashcabu.hw2.movieDetailsRecyclerView.ActorData
import com.pashcabu.hw2.R

class MoviesData {
    fun getMovies():List<Movie>{
        return listOf(
                Movie("Avengers: End Game", "13+", 4, "Action, Adventure, Drama", 125, 137, R.drawable.poster_avengers, R.drawable.background, R.string.avengers_storyline, ActorData().avengers()),
                Movie("Tenet", "16+", 5, "Action, Sci-Fi, Thriller", 98, 97, R.drawable.poster_tenet, R.drawable.tenet_big_poster, R.string.tenet_storyline, ActorData().avengers()),
                Movie("Black Widow", "13+", 4, "Action, Adventure, Sci-Fi", 38, 102, R.drawable.poster_black_widow, R.drawable.black_widow_big_poster, R.string.black_widow_storyline, ActorData().avengers()),
                Movie("Wonder Woman 1984", "13+", 5, "Action, Adventure, Fantasy", 74, 120, R.drawable.poster_wonder_woman_1984, R.drawable.wonderwoman_1984_big_poster, R.string.wonder_woman_storyline, ActorData().avengers())
        )
    }

    fun findMovieByName(title : String?):Movie{
        var number = 0
        for (item in getMovies()){
            if (item.title == title) number=getMovies().indexOf(item)
        }
        return getMovies()[number]
    }
}