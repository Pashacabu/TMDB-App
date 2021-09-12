package com.pashacabu.tmdb_app.model.data_classes

import com.pashacabu.tmdb_app.model.data_classes.networkResponses.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataClassSupplier @Inject constructor() {

    fun supplyCast() : CastResponse = CastResponse()

    fun supplyGenres() : GenresResponse = GenresResponse()

    fun supplyImages() : ImagesResponse = ImagesResponse()

    fun supplyDetails() : MovieDetailsResponse = MovieDetailsResponse()

    fun supplyList() : MovieListResponse = MovieListResponse()

    fun supplyPerson() : PersonResponse = PersonResponse()
}