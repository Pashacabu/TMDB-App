package com.pashcabu.hw2.moviesListRecyclerView

import com.pashcabu.hw2.movieDetailsRecyclerView.Actor

data class Movie (
        val title:Int,
        val pgRating:Int,
        val rating:Int,
        val tags:Int,
        val reviews:Int,
        val duration:Int,
        val poster:Int,
        val bigPoster:Int,
        val story:Int,
        val cast:List<Actor>
)


