package com.pashcabu.hw2

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

class NetworkModule {

    private val baseUrl = "https://api.themoviedb.org/3/"
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val contentType = "application/json".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()

    val apiService: TMDBInterface = retrofit.create()

    interface TMDBInterface {
        @GET("movie/{movieID}")
        suspend fun getMovieDetails(
            @Path("movieID") movieID: Int,
            @Query("api_key") api_key: String
        ): MovieDetailsResponse

        @GET("movie/now_playing")
        suspend fun getMoviesList(
            @Query("api_key") api_key: String,
            @Query("page") page: Int
        ): MovieListResponse

        @GET("genre/movie/list")
        suspend fun getGenres(@Query("api_key") api_key: String): GenresResponse

        @GET("movie/{movieID}/credits")
        suspend fun getActors(@Path("movieID") id: Int, @Query("api_key") api_key: String): Cast

    }
}

const val api_key = "690d3ea0f7ef1f69512be4c95fc7a886"
