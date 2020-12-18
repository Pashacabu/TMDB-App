package com.pashcabu.hw2.data

import android.os.Parcel
import android.os.Parcelable
import com.pashcabu.hw2.data.Actor
import com.pashcabu.hw2.data.Genre

data class Movie (
    val id: Int,
    val title: String,
    val overview: String,
    val poster: String,
    val backdrop: String,
    val ratings: Float,
    val numberOfRatings: Int,
    val minimumAge: Int,
    val runtime: Int,
    val genres: List<Genre>,
    val actors: List<Actor>
)